package com.lshdainty.porest.vacation.service;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.holiday.repository.HolidayRepositoryImpl;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepositoryImpl;
import com.lshdainty.porest.vacation.domain.*;
import com.lshdainty.porest.vacation.repository.*;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import com.lshdainty.porest.vacation.service.policy.VacationPolicyStrategy;
import com.lshdainty.porest.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import com.lshdainty.porest.vacation.service.type.VacationTypeStrategy;
import com.lshdainty.porest.vacation.service.type.factory.VacationTypeStrategyFactory;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.vacation.type.VacationTimeType;
import com.lshdainty.porest.common.util.PorestTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VacationService {
    private final MessageSource ms;
    private final VacationPolicyCustomRepositoryImpl vacationPolicyRepository;
    private final UserVacationPolicyCustomRepositoryImpl userVacationPolicyRepository;
    private final UserRepositoryImpl userRepository;
    private final HolidayRepositoryImpl holidayRepository;
    private final UserService userService;
    private final VacationPolicyStrategyFactory vacationPolicyStrategyFactory;
    private final VacationGrantCustomRepositoryImpl vacationGrantRepository;
    private final VacationUsageCustomRepositoryImpl vacationUsageRepository;
    private final VacationUsageDeductionCustomRepositoryImpl vacationUsageDeductionRepository;

    @Transactional
    public Long useVacation(VacationServiceDto data) {
        // 1. 사용자 검증
        User user = userService.checkUserExist(data.getUserId());

        // 2. 시작, 종료시간 비교
        if (PorestTime.isAfterThanEndDate(data.getStartDate(), data.getEndDate())) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.startIsAfterThanEnd", null, null));
        }

        // 3. 연차가 아닌 시간단위 휴가인 경우 유연근무제 시간 체크
        if (!data.getTimeType().equals(VacationTimeType.DAYOFF)) {
            if (!user.isBetweenWorkTime(data.getStartDate().toLocalTime(), data.getEndDate().toLocalTime())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.worktime.startEndTime", null, null));
            }
        }

        // 4. 주말 리스트 조회
        List<LocalDate> weekDays = PorestTime.getBetweenDatesByDayOfWeek(data.getStartDate(), data.getEndDate(), new int[]{6, 7}, ms);

        // 5. 공휴일 리스트 조회
        List<LocalDate> holidays = holidayRepository.findHolidaysByStartEndDateWithType(
                data.getStartDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                data.getEndDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                HolidayType.PUBLIC
        ).stream()
                .map(h -> LocalDate.parse(h.getDate(), DateTimeFormatter.BASIC_ISO_DATE))
                .toList();

        weekDays = PorestTime.addAllDates(weekDays, holidays);

        // 6. 두 날짜 간 모든 날짜 가져오기
        List<LocalDate> betweenDates = PorestTime.getBetweenDates(data.getStartDate(), data.getEndDate(), ms);
        log.info("betweenDates : {}, weekDays : {}", betweenDates, weekDays);

        // 7. 사용자가 캘린더에서 선택한 날짜 중 휴일, 공휴일 제거
        betweenDates = PorestTime.removeAllDates(betweenDates, weekDays);
        log.info("remainDays : {}", betweenDates);

        // 8. 등록하려는 총 사용시간 계산
        BigDecimal totalUseTime = new BigDecimal("0.0000").add(data.getTimeType().convertToValue(betweenDates.size()));

        // 9. 사용 가능한 VacationGrant 조회 (FIFO: VacationType 일치 + 휴가 시작일이 유효기간 내 + 만료일 가까운 순)
        List<VacationGrant> availableGrants = vacationGrantRepository.findAvailableGrantsByUserIdAndTypeAndDate(
                data.getUserId(),
                data.getVacationType(),
                data.getStartDate()  // 사용자가 사용하려는 휴가 시작일
        );

        // 10. 총 잔여 시간 계산 및 검증
        BigDecimal totalRemainTime = availableGrants.stream()
                .map(VacationGrant::getRemainTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRemainTime.compareTo(totalUseTime) < 0) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.notEnoughRemainTime", null, null));
        }

        // 11. 통합 기간 휴가 사용 내역 생성
        VacationUsage usage = VacationUsage.createVacationUsage(
                user,
                data.getDesc(),
                data.getTimeType(),
                data.getStartDate(),
                data.getEndDate(),
                totalUseTime
        );

        // 12. FIFO로 VacationGrant에서 차감
        List<VacationUsageDeduction> deductionsToSave = new ArrayList<>();
        BigDecimal remainingNeedTime = totalUseTime;

        for (VacationGrant grant : availableGrants) {
            if (remainingNeedTime.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // 이 grant에서 차감 가능한 시간
            BigDecimal deductibleTime = grant.getRemainTime().min(remainingNeedTime);

            if (deductibleTime.compareTo(BigDecimal.ZERO) > 0) {
                // VacationUsageDeduction 생성
                VacationUsageDeduction deduction = VacationUsageDeduction.createVacationUsageDeduction(
                        usage,
                        grant,
                        deductibleTime
                );
                deductionsToSave.add(deduction);

                // VacationGrant의 remainTime 차감
                grant.deductedVacation(deductibleTime);

                remainingNeedTime = remainingNeedTime.subtract(deductibleTime);
            }
        }

        // 차감이 완료되지 않았다면 예외 (이론적으로는 발생하지 않아야 함)
        if (remainingNeedTime.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.notEnoughRemainTime", null, null));
        }

        // 13. 저장
        vacationUsageRepository.save(usage);
        vacationUsageDeductionRepository.saveAll(deductionsToSave);

        log.info("휴가 사용 완료 - User: {}, Period: {} ~ {}, WorkingDays: {}, TotalUseTime: {}",
                user.getId(), data.getStartDate(), data.getEndDate(), betweenDates.size(), totalUseTime);

        return usage.getId();
    }

    public List<Vacation> searchUserVacations(String userId) {
        return vacationRepository.findVacationsByUserId(userId);
    }

    public List<User> searchUserGroupVacations() {
        return userRepository.findUsersWithVacations();
    }

    public List<Vacation> searcgAvailableVacations(String userId, LocalDateTime startDate) {
        // 유저 조회
        userService.checkUserExist(userId);

        // 시작 날짜를 기준으로 등록 가능한 휴가 목록 조회
        return vacationRepository.findVacationsByBaseTime(userId, startDate);
    }

    @Transactional
    public void deleteVacationHistory(Long vacationHistoryId) {
        VacationHistory history = checkVacationHistoryExist(vacationHistoryId);
        Vacation vacation = checkVacationExist(history.getVacation().getId());

        if (PorestTime.isAfterThanEndDate(LocalDateTime.now(), vacation.getExpiryDate())) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.expiry.isBeforeThanNow", null, null));
        }

        if (Objects.isNull(history.getType())) {
            // 휴가 추가 내역
            if (vacation.getRemainTime().compareTo(history.getGrantTime()) < 0) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.notEnoughRemainTime", null, null));
            }

            // 휴가 추가 내역은 삭제하고 추가된 휴가 차감
            history.deleteRegistVacationHistory(vacation);
        } else {
            // 휴가 사용 내역
            if (PorestTime.isAfterThanEndDate(LocalDateTime.now(), history.getUsedDateTime())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.delete.isBeforeThanNow", null, null));
            }

            // 휴가 사용 내역은 삭제하고 차감된 휴가 추가
            history.deleteUseVacationHistory(vacation);
        }
    }

    public List<VacationServiceDto> searchPeriodVacationUseHistories(LocalDateTime startDate, LocalDateTime endDate) {
        // 기간에 맞는 history 내역 가져오기
        List<VacationHistory> histories = vacationHistoryRepository.findVacationHistorysByPeriod(startDate, endDate);

        // 유저 정보 반환을 위해 vacation 정보 가져오기
        List<Vacation> vacations = vacationRepository.findVacationsByIdsWithUser(histories.stream()
                .map(vh -> vh.getVacation().getId())
                .distinct()
                .toList()
        );

        // vacation id에 따른 user 정보 mapping
        Map<Long, User> userMap = vacations.stream()
                .collect(Collectors.toMap(Vacation::getId, v -> v.getUser()));

        // 연차인 내역만 추출
        List<VacationHistory> dayHistories = histories.stream()
                .filter(vh -> vh.getType().equals(VacationTimeType.DAYOFF))
                .collect(Collectors.toList());

        // 시간단위 내역만 추출
        List<VacationHistory> hourHistories = histories.stream()
                .filter(vh -> !vh.getType().equals(VacationTimeType.DAYOFF))
                .collect(Collectors.toList());

        // 연차인 경우 따로 분리된 휴가를 start - end화 하여 serviceDto로 변환
        // 시간단위 휴가인 경우 단순 serviceDto로 변환
        // 반환된 두 배열을 하나의 List로 합침
        List<VacationServiceDto> result = Stream.of(makeDayGroupDto(dayHistories), makeHourGroupDto(hourHistories))
                .flatMap(Collection::stream)
                .toList();

        // controller에서 user정보를 사용할 수 있게 vacation id에 맞는 user정보 세팅
        for (VacationServiceDto dto : result) {
            dto.setUser(userMap.get(dto.getId()));
        }

        return result;
    }

    public List<VacationServiceDto> searchUserPeriodVacationUseHistories(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 기간에 맞는 유저 history 내역 가져오기
        List<VacationHistory> histories = vacationHistoryRepository.findVacationUseHistorysByUserAndPeriod(userId, startDate, endDate);

        return histories.stream()
                .map(vh -> VacationServiceDto.builder()
                        .id(vh.getVacation().getId())
                        .historyId(vh.getId())
                        .timeType(vh.getType())
                        .desc(vh.getDesc())
                        .startDate(vh.getUsedDateTime())
                        .endDate(vh.getUsedDateTime().plusSeconds(vh.getType().getSeconds()))
                        .build()
                )
                .toList();
    }

    public List<VacationServiceDto> searchUserMonthStatsVacationUseHistories(String userId, String year) {
        // 기간에 맞는 유저 history 내역 가져오기
        List<VacationHistory> histories = vacationHistoryRepository.findVacationUseHistorysByUserAndPeriod(
                userId,
                LocalDateTime.of(Integer.parseInt(year), 1, 1, 0, 0, 0),
                LocalDateTime.of(Integer.parseInt(year), 12, 31, 23, 59, 59)
        );

        // 월별 사용량 Map 생성 및 0 초기화 (순서 보장위해 LinkedHashMap 사용)
        Map<Integer, BigDecimal> monthlyMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyMap.put(i, BigDecimal.ZERO);
        }

        // 월별 사용량 집계
        for (VacationHistory history : histories) {
            int month = history.getUsedDateTime().getMonthValue();
            // DB Insert시 하루 기준으로 넣었음
            BigDecimal useValue = history.getType().convertToValue(1);
            monthlyMap.merge(month, useValue, BigDecimal::add);
        }

        return monthlyMap.entrySet().stream()
                .map(e -> VacationServiceDto.builder()
                            .month(e.getKey())
                            .usedTime(e.getValue())
                            .build()
                )
                .toList();
    }

    public VacationServiceDto searchUserVacationUseStats(String userId, LocalDateTime baseTime) {
        // 기준 시점에 유효한 모든 휴가 & 전체 이력
        List<Vacation> curVacations = vacationRepository.findVacationsByBaseTimeWithHistory(userId, baseTime);
        List<Vacation> prevVacations = vacationRepository.findVacationsByBaseTimeWithHistory(userId, baseTime.minusMonths(1));

        // 삭제 안된 이력만 필터링
        List<VacationHistory> curHistories = curVacations.stream()
                .flatMap(v -> v.getHistorys().stream())
                .filter(vh -> YNType.N.equals(vh.getIsDeleted()))
                .toList();
        List<VacationHistory> prevHistories = prevVacations.stream()
                .flatMap(v -> v.getHistorys().stream())
                .filter(vh -> YNType.N.equals(vh.getIsDeleted()))
                .toList();

        // 현재 및 이전 달 통계 계산
        VacationServiceDto curStats = calculateStatsForDate(curHistories, baseTime);
        VacationServiceDto prevStats = calculateStatsForDate(prevHistories, baseTime.minusMonths(1));

        return VacationServiceDto.builder()
                .remainTime(curStats.getRemainTime())
                .usedTime(curStats.getUsedTime())
                .expectUsedTime(curStats.getExpectUsedTime())
                .prevRemainTime(prevStats.getRemainTime())
                .prevUsedTime(prevStats.getUsedTime())
                .prevExpectUsedTime(prevStats.getExpectUsedTime())
                .build();
    }

    public Vacation checkVacationExist(Long vacationId) {
        Optional<Vacation> vacation = vacationRepository.findById(vacationId);
        vacation.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation", null, null)));
        return vacation.get();
    }

    public VacationHistory checkVacationHistoryExist(Long vacationHistoryId) {
        Optional<VacationHistory> history = vacationHistoryRepository.findById(vacationHistoryId);
        history.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation.history", null, null)));
        return history.get();
    }

    @Transactional
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
        VacationPolicyStrategy strategy = vacationPolicyStrategyFactory.getStrategy(data.getGrantMethod());
        return strategy.registVacationPolicy(data);
    }

    public VacationPolicyServiceDto searchVacationPolicy(Long vacationPolicyId) {
        VacationPolicy policy = checkVacationPolicyExist(vacationPolicyId);

        return VacationPolicyServiceDto.builder()
                .id(policy.getId())
                .name(policy.getName())
                .desc(policy.getDesc())
                .vacationType(policy.getVacationType())
                .grantMethod(policy.getGrantMethod())
                .grantTime(policy.getGrantTime())
                .repeatUnit(policy.getRepeatUnit())
                .repeatInterval(policy.getRepeatInterval())
                .specificMonths(policy.getSpecificMonths())
                .specificDays(policy.getSpecificDays())
                .build();
    }

    public List<VacationPolicyServiceDto> searchVacationPolicies() {
        List<VacationPolicy> policies = vacationPolicyRepository.findVacationPolicies();
        return policies.stream()
                .map(p -> VacationPolicyServiceDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .desc(p.getDesc())
                        .vacationType(p.getVacationType())
                        .grantMethod(p.getGrantMethod())
                        .grantTime(p.getGrantTime())
                        .repeatUnit(p.getRepeatUnit())
                        .repeatInterval(p.getRepeatInterval())
                        .specificMonths(p.getSpecificMonths())
                        .specificDays(p.getSpecificDays())
                        .build())
                .toList();
    }

    public VacationPolicy checkVacationPolicyExist(Long vacationPolicyId) {
        Optional<VacationPolicy> policy = vacationPolicyRepository.findVacationPolicyById(vacationPolicyId);
        policy.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation.policy", null, null)));
        return policy.get();
    }

    /**
     * 휴가 정책 삭제
     *
     * 휴가 정책을 소프트 삭제하고, 구성원에게 부여된 휴가 수량을 처리합니다.
     * - 보유한 휴가에만 영향을 주고, 이미 사용했던 혹은 사용 예정으로 신청해둔 휴가에는 영향을 주지 않습니다.
     *
     * @param vacationPolicyId 삭제할 휴가 정책 ID
     * @return 삭제된 휴가 정책 ID
     */
    @Transactional
    public Long deleteVacationPolicy(Long vacationPolicyId) {
        // 1. 휴가 정책 존재 확인
        VacationPolicy vacationPolicy = checkVacationPolicyExist(vacationPolicyId);

        // 2. 이미 삭제된 정책인지 확인
        if (vacationPolicy.getIsDeleted() == YNType.Y) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.already.deleted.vacation.policy", null, null));
        }

        // 3. 삭제 가능 여부 확인
        if (vacationPolicy.getCanDeleted() == YNType.N) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.cannot.delete.vacation.policy", null, null));
        }

        // 4. 휴가 정책 소프트 삭제
        vacationPolicy.deleteVacationPolicy();

        // 5. 해당 휴가 정책을 사용하는 모든 UserVacationPolicy를 소프트 삭제
        List<UserVacationPolicy> userVacationPolicies = userVacationPolicyRepository.findByVacationPolicyId(vacationPolicyId);
        int deletedUserVacationPolicyCount = 0;

        for (UserVacationPolicy uvp : userVacationPolicies) {
            // 이미 삭제된 경우 스킵
            if (uvp.getIsDeleted() == YNType.Y) {
                continue;
            }

            // UserVacationPolicy 소프트 삭제
            uvp.deleteUserVacationPolicy();
            deletedUserVacationPolicyCount++;
        }

        log.info("Deleted {} user vacation policy assignments for vacation policy {}",
                deletedUserVacationPolicyCount, vacationPolicyId);

        // TODO: 구성원에게 부여된 휴가 수량 처리
        // - 해당 휴가 정책으로 부여된 모든 Vacation 조회
        // - 각 Vacation의 remainTime에서 해당 정책의 grantTime만큼 차감
        // - 단, 이미 사용한 휴가(VacationHistory)에는 영향을 주지 않음
        // - 사용 예정으로 신청해둔 휴가(future VacationHistory)에도 영향을 주지 않음
        log.warn("TODO: Process vacation grant removal for policy {}", vacationPolicyId);

        log.info("Deleted vacation policy {}", vacationPolicyId);

        return vacationPolicyId;
    }

    /**
     * 유저에게 여러 휴가 정책을 일괄 할당
     *
     * @param userId 유저 ID
     * @param vacationPolicyIds 휴가 정책 ID 리스트
     * @return 할당된 휴가 정책 ID 리스트
     */
    @Transactional
    public List<Long> assignVacationPoliciesToUser(String userId, List<Long> vacationPolicyIds) {
        // 1. 유저 존재 확인
        User user = userService.checkUserExist(userId);

        // 2. 할당할 휴가 정책들의 유효성 검증
        List<VacationPolicy> vacationPolicies = new ArrayList<>();
        for (Long policyId : vacationPolicyIds) {
            VacationPolicy policy = checkVacationPolicyExist(policyId);
            vacationPolicies.add(policy);
        }

        // 3. 중복 할당 체크 및 필터링
        List<Long> assignedPolicyIds = new ArrayList<>();
        List<UserVacationPolicy> userVacationPolicies = new ArrayList<>();

        for (VacationPolicy policy : vacationPolicies) {
            // 이미 할당된 정책인지 확인
            boolean alreadyAssigned = userVacationPolicyRepository.existsByUserIdAndVacationPolicyId(userId, policy.getId());

            if (alreadyAssigned) {
                log.warn("User {} already has vacation policy {}, skipping", userId, policy.getId());
                continue;
            }

            // UserVacationPolicy 생성
            UserVacationPolicy userVacationPolicy = UserVacationPolicy.createUserVacationPolicy(user, policy);
            userVacationPolicies.add(userVacationPolicy);
            assignedPolicyIds.add(policy.getId());
        }

        // 4. 일괄 저장
        if (!userVacationPolicies.isEmpty()) {
            userVacationPolicyRepository.saveAll(userVacationPolicies);
            log.info("Assigned {} vacation policies to user {}", userVacationPolicies.size(), userId);
        }

        return assignedPolicyIds;
    }

    /**
     * 유저에게 할당된 휴가 정책 조회
     *
     * @param userId 유저 ID
     * @return 유저에게 할당된 휴가 정책 리스트
     */
    public List<VacationPolicyServiceDto> searchUserVacationPolicies(String userId) {
        // 유저 존재 확인
        userService.checkUserExist(userId);

        // 유저에게 할당된 휴가 정책 조회
        List<UserVacationPolicy> userVacationPolicies = userVacationPolicyRepository.findByUserId(userId);

        return userVacationPolicies.stream()
                .map(uvp -> {
                    VacationPolicy policy = uvp.getVacationPolicy();
                    return VacationPolicyServiceDto.builder()
                            .userVacationPolicyId(uvp.getId())
                            .id(policy.getId())
                            .name(policy.getName())
                            .desc(policy.getDesc())
                            .vacationType(policy.getVacationType())
                            .grantMethod(policy.getGrantMethod())
                            .grantTime(policy.getGrantTime())
                            .repeatUnit(policy.getRepeatUnit())
                            .repeatInterval(policy.getRepeatInterval())
                            .specificMonths(policy.getSpecificMonths())
                            .specificDays(policy.getSpecificDays())
                            .build();
                })
                .toList();
    }

    /**
     * 유저에게 부여된 휴가 정책 회수 (단일)
     *
     * @param userId 유저 ID
     * @param vacationPolicyId 휴가 정책 ID
     * @return 회수된 UserVacationPolicy ID
     */
    @Transactional
    public Long revokeVacationPolicyFromUser(String userId, Long vacationPolicyId) {
        // 1. 유저 존재 확인
        userService.checkUserExist(userId);

        // 2. 휴가 정책 존재 확인
        checkVacationPolicyExist(vacationPolicyId);

        // 3. UserVacationPolicy 조회
        UserVacationPolicy userVacationPolicy = userVacationPolicyRepository
                .findByUserIdAndVacationPolicyId(userId, vacationPolicyId)
                .orElseThrow(() -> new IllegalArgumentException(
                        ms.getMessage("error.notfound.user.vacation.policy", null, null)));

        // 4. 이미 삭제된 경우 예외 처리
        if (userVacationPolicy.getIsDeleted() == YNType.Y) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.already.deleted.user.vacation.policy", null, null));
        }

        // 5. 소프트 삭제 수행
        userVacationPolicy.deleteUserVacationPolicy();

        log.info("Revoked vacation policy {} from user {}", vacationPolicyId, userId);

        return userVacationPolicy.getId();
    }

    /**
     * 유저에게 부여된 여러 휴가 정책 일괄 회수
     *
     * @param userId 유저 ID
     * @param vacationPolicyIds 휴가 정책 ID 리스트
     * @return 회수된 UserVacationPolicy ID 리스트
     */
    @Transactional
    public List<Long> revokeVacationPoliciesFromUser(String userId, List<Long> vacationPolicyIds) {
        // 1. 유저 존재 확인
        userService.checkUserExist(userId);

        List<Long> revokedIds = new ArrayList<>();

        // 2. 각 휴가 정책에 대해 회수 처리
        for (Long policyId : vacationPolicyIds) {
            try {
                // 휴가 정책 존재 확인
                checkVacationPolicyExist(policyId);

                // UserVacationPolicy 조회
                Optional<UserVacationPolicy> optionalUvp = userVacationPolicyRepository
                        .findByUserIdAndVacationPolicyId(userId, policyId);

                if (optionalUvp.isEmpty()) {
                    log.warn("User {} does not have vacation policy {}, skipping", userId, policyId);
                    continue;
                }

                UserVacationPolicy userVacationPolicy = optionalUvp.get();

                // 이미 삭제된 경우 스킵
                if (userVacationPolicy.getIsDeleted() == YNType.Y) {
                    log.warn("Vacation policy {} already revoked from user {}, skipping", policyId, userId);
                    continue;
                }

                // 소프트 삭제 수행
                userVacationPolicy.deleteUserVacationPolicy();
                revokedIds.add(policyId);

            } catch (Exception e) {
                log.error("Failed to revoke vacation policy {} from user {}: {}", policyId, userId, e.getMessage());
                // 일괄 처리 중 개별 에러는 스킵하고 계속 진행
            }
        }

        log.info("Revoked {} vacation policies from user {}", revokedIds.size(), userId);

        return revokedIds;
    }

    private List<VacationServiceDto> makeDayGroupDto(List<VacationHistory> dayHistories) {
        List<VacationServiceDto> vacationDtos = new ArrayList<>();

        String previousGroupKey = null;
        VacationServiceDto vacationDto = null;
        LocalDateTime previousDate = null;
        List<Long> historyIds = null;

        for (VacationHistory history : dayHistories) {
            // 그룹 비교 키 생성
            String currentGroupKey = history.getVacation().getId() + "-" + history.getType().name();
            LocalDateTime currentDate = history.getUsedDateTime();

            // 첫 행 || 이전 그룹 키와 다른 경우
            if (previousGroupKey == null || !previousGroupKey.equals(currentGroupKey)) {
                // 이전 그룹이 있다면 결과에 추가
                if (previousGroupKey != null) {
                    vacationDtos.add(vacationDto);
                }

                // 새로운 그룹 시작
                previousGroupKey = currentGroupKey;
                previousDate = currentDate;
                historyIds = new ArrayList<>();
                historyIds.add(history.getId());
                vacationDto = VacationServiceDto.builder()
                        .id(history.getVacation().getId())
                        .historyIds(historyIds)
                        .timeType(history.getType())
                        .desc(history.getDesc())
                        .startDate(currentDate)
                        .endDate(currentDate.plusSeconds(history.getType().getSeconds()))
                        .build();
            } else {    // 이전 그룹 키와 같은 경우
                // 연속된 날짜인지 확인 (1일 차이)
                if (ChronoUnit.DAYS.between(previousDate, currentDate) == 1) {
                    historyIds.add(history.getId());
                    vacationDto.setHistoryIds(historyIds);
                    vacationDto.setEndDate(currentDate.plusSeconds(VacationTimeType.DAYOFF.getSeconds()));
                } else {
                    // 연속되지 않으므로 현재 그룹을 완료하고 새 그룹 시작
                    vacationDtos.add(vacationDto);

                    historyIds = new ArrayList<>();
                    historyIds.add(history.getId());
                    vacationDto = VacationServiceDto.builder()
                            .id(history.getVacation().getId())
                            .historyIds(historyIds)
                            .timeType(history.getType())
                            .desc(history.getDesc())
                            .startDate(currentDate)
                            .endDate(currentDate.plusSeconds(history.getType().getSeconds()))
                            .build();
                }
                previousDate = currentDate;
            }
        }

        // 마지막 그룹 추가
        if (previousGroupKey != null) {
            vacationDtos.add(vacationDto);
        }

        return vacationDtos;
    }

    private List<VacationServiceDto> makeHourGroupDto(List<VacationHistory> hourHistories) {
        List<VacationServiceDto> vacationDtos = new ArrayList<>();

        for (VacationHistory history : hourHistories) {
            vacationDtos.add(
                    VacationServiceDto.builder()
                            .id(history.getVacation().getId())
                            .historyIds(List.of(history.getId()))
                            .timeType(history.getType())
                            .desc(history.getDesc())
                            .startDate(history.getUsedDateTime())
                            .endDate(history.getUsedDateTime().plusSeconds(history.getType().getSeconds()))
                            .build()
            );
        }

        return vacationDtos;
    }

    private VacationServiceDto calculateStatsForDate(List<VacationHistory> histories, LocalDateTime baseDate) {
        // 기준일 이전에 부여된 총 휴가
        BigDecimal totalGranted = histories.stream()
                .filter(h -> h.getGrantTime() != null)
                .map(VacationHistory::getGrantTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 기준일 이전에 사용된 총 휴가
        BigDecimal totalUsedTime = histories.stream()
                .filter(h -> h.getType() != null && !h.getUsedDateTime().isAfter(baseDate))
                .map(h -> h.getType().convertToValue(1))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 기준일 이후에 사용될 총 휴가
        BigDecimal totalExpectUsedTime = histories.stream()
                .filter(h -> h.getType() != null && h.getUsedDateTime().isAfter(baseDate))
                .map(h -> h.getType().convertToValue(1))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemainTime = totalGranted.subtract(totalUsedTime);

        return VacationServiceDto.builder()
                .remainTime(totalRemainTime)
                .usedTime(totalUsedTime)
                .expectUsedTime(totalExpectUsedTime)
                .build();
    }
}
