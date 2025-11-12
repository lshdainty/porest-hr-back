package com.lshdainty.porest.vacation.service;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.holiday.repository.HolidayRepositoryImpl;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.repository.DepartmentCustomRepositoryImpl;
import com.lshdainty.porest.vacation.domain.*;
import com.lshdainty.porest.vacation.repository.*;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationApprovalServiceDto;
import com.lshdainty.porest.vacation.service.policy.VacationPolicyStrategy;
import com.lshdainty.porest.vacation.service.policy.RepeatGrant;
import com.lshdainty.porest.vacation.service.policy.OnRequest;
import com.lshdainty.porest.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.vacation.type.*;
import com.lshdainty.porest.common.util.PorestTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VacationService {
    private final MessageSource ms;
    private final VacationPolicyCustomRepositoryImpl vacationPolicyRepository;
    private final UserVacationPolicyCustomRepositoryImpl userVacationPolicyRepository;
    private final HolidayRepositoryImpl holidayRepository;
    private final UserService userService;
    private final VacationPolicyStrategyFactory vacationPolicyStrategyFactory;
    private final VacationGrantCustomRepositoryImpl vacationGrantRepository;
    private final VacationUsageCustomRepositoryImpl vacationUsageRepository;
    private final VacationUsageDeductionCustomRepositoryImpl vacationUsageDeductionRepository;
    private final VacationApprovalCustomRepositoryImpl vacationApprovalRepository;
    private final DepartmentCustomRepositoryImpl departmentRepository;

    @Transactional
    public Long useVacation(VacationServiceDto data) {
        // 1. 사용자 검증
        User user = userService.checkUserExist(data.getUserId());

        // 2. 시작, 종료시간 비교
        if (PorestTime.isAfterThanEndDate(data.getStartDate(), data.getEndDate())) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.startIsAfterThanEnd", null, null));
        }

        // 3. 연차가 아닌 시간단위 휴가인 경우 유연근무제 시간 체크
        if (!(data.getTimeType().equals(VacationTimeType.DAYOFF) || !data.getTimeType().equals(VacationTimeType.DEFENSE))) {
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
                data.getType(),
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
                grant.deduct(deductibleTime);

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

    /**
     * 유저의 휴가 부여 및 사용 내역 조회
     *
     * @param userId 유저 아이디
     * @return 부여받은 내역(VacationGrant)과 사용한 내역(VacationUsage)
     */
    public VacationServiceDto getUserVacationHistory(String userId) {
        // 유저 존재 확인
        userService.checkUserExist(userId);

        // 부여받은 내역 조회
        List<VacationGrant> grants = vacationGrantRepository.findByUserId(userId);

        // 사용한 내역 조회
        List<VacationUsage> usages = vacationUsageRepository.findByUserId(userId);

        return VacationServiceDto.builder()
                .grants(grants)
                .usages(usages)
                .build();
    }

    /**
     * 모든 유저의 휴가 부여 및 사용 내역 조회
     *
     * @return 모든 유저별 부여받은 내역(VacationGrant)과 사용한 내역(VacationUsage)
     */
    public List<VacationServiceDto> getAllUsersVacationHistory() {
        // 모든 부여 내역 조회
        List<VacationGrant> allGrants = vacationGrantRepository.findAllWithUser();

        // 모든 사용 내역 조회
        List<VacationUsage> allUsages = vacationUsageRepository.findAllWithUser();

        // User별로 Grant 그룹핑
        Map<String, List<VacationGrant>> grantsByUser = allGrants.stream()
                .collect(Collectors.groupingBy(g -> g.getUser().getId()));

        // User별로 Usage 그룹핑
        Map<String, List<VacationUsage>> usagesByUser = allUsages.stream()
                .collect(Collectors.groupingBy(u -> u.getUser().getId()));

        // 모든 userId 수집
        Set<String> allUserIds = new HashSet<>();
        allUserIds.addAll(grantsByUser.keySet());
        allUserIds.addAll(usagesByUser.keySet());

        // User별로 VacationServiceDto 생성
        return allUserIds.stream()
                .map(userId -> {
                    List<VacationGrant> grants = grantsByUser.getOrDefault(userId, new ArrayList<>());
                    List<VacationUsage> usages = usagesByUser.getOrDefault(userId, new ArrayList<>());

                    // User 객체는 grants나 usages에서 가져오기
                    User user = null;
                    if (!grants.isEmpty()) {
                        user = grants.get(0).getUser();
                    } else if (!usages.isEmpty()) {
                        user = usages.get(0).getUser();
                    }

                    return VacationServiceDto.builder()
                            .userId(userId)
                            .user(user)
                            .grants(grants)
                            .usages(usages)
                            .build();
                })
                .toList();
    }

    /**
     * 시작 날짜 기준으로 사용 가능한 휴가 조회 (VacationType별 그룹화)
     *
     * @param userId 유저 아이디
     * @param startDate 시작 날짜
     * @return VacationType별로 그룹화된 사용 가능한 휴가 내역
     */
    public List<VacationServiceDto> getAvailableVacations(String userId, LocalDateTime startDate) {
        // 유저 조회
        userService.checkUserExist(userId);

        // 시작 날짜를 기준으로 사용 가능한 휴가 부여 내역 조회
        List<VacationGrant> availableGrants = vacationGrantRepository.findAvailableGrantsByUserIdAndDate(userId, startDate);

        // VacationType별로 그룹화하고 remainTime 합산
        Map<VacationType, BigDecimal> remainTimeByType = availableGrants.stream()
                .collect(Collectors.groupingBy(
                        VacationGrant::getType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                VacationGrant::getRemainTime,
                                BigDecimal::add
                        )
                ));

        // VacationServiceDto 리스트로 변환
        return remainTimeByType.entrySet().stream()
                .map(entry -> VacationServiceDto.builder()
                        .type(entry.getKey())
                        .remainTime(entry.getValue())
                        .build())
                .toList();
    }

    /**
     * 휴가 사용 취소
     * - VacationUsage를 소프트 삭제
     * - VacationGrant의 remainTime 복구
     *
     * @param vacationUsageId 휴가 사용 내역 ID
     */
    @Transactional
    public void cancelVacationUsage(Long vacationUsageId) {
        // 1. VacationUsage 조회
        VacationUsage usage = vacationUsageRepository.findById(vacationUsageId)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation.usage", null, null)));

        // 2. 이미 삭제된 경우 예외 처리
        if (usage.getIsDeleted() == YNType.Y) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.already.deleted.vacation.usage", null, null));
        }

        // 3. 삭제 가능 시점 체크 (현재 시간이 사용 시작일 이전인지 확인)
        if (PorestTime.isAfterThanEndDate(LocalDateTime.now(), usage.getStartDate())) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.delete.isBeforeThanNow", null, null));
        }

        // 4. VacationUsageDeduction 조회 (차감 내역들)
        List<VacationUsageDeduction> deductions = vacationUsageDeductionRepository.findByUsageId(vacationUsageId);

        // 5. 각 차감 내역에서 차감했던 시간을 VacationGrant에 복구
        for (VacationUsageDeduction deduction : deductions) {
            VacationGrant grant = deduction.getGrant();
            grant.restore(deduction.getDeductedTime());
            log.info("VacationGrant {} 복구: {} 추가", grant.getId(), deduction.getDeductedTime());
        }

        // 6. VacationUsage 소프트 삭제
        usage.deleteVacationUsage();

        log.info("휴가 사용 내역 삭제 완료 - VacationUsage ID: {}, 복구된 차감 내역 수: {}", vacationUsageId, deductions.size());
    }

    /**
     * 기간별 휴가 사용 내역 조회
     *
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 기간 내 모든 사용자의 휴가 사용 내역
     */
    public List<VacationServiceDto> getVacationUsagesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        // 기간에 맞는 휴가 사용 내역 조회 (startDate 기준)
        List<VacationUsage> usages = vacationUsageRepository.findByPeriodWithUser(startDate, endDate);

        // VacationServiceDto로 변환
        return usages.stream()
                .map(usage -> VacationServiceDto.builder()
                        .id(usage.getId())
                        .user(usage.getUser())
                        .desc(usage.getDesc())
                        .timeType(usage.getType())
                        .startDate(usage.getStartDate())
                        .endDate(usage.getEndDate())
                        .usedTime(usage.getUsedTime())
                        .build())
                .toList();
    }

    /**
     * 유저별 기간별 휴가 사용 내역 조회
     *
     * @param userId 유저 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 유저의 기간 내 휴가 사용 내역
     */
    public List<VacationServiceDto> getUserVacationUsagesByPeriod(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 유저 존재 확인
        userService.checkUserExist(userId);

        // 유저의 기간에 맞는 휴가 사용 내역 조회 (startDate 기준)
        List<VacationUsage> usages = vacationUsageRepository.findByUserIdAndPeriodWithUser(userId, startDate, endDate);

        // VacationServiceDto로 변환
        return usages.stream()
                .map(usage -> VacationServiceDto.builder()
                        .id(usage.getId())
                        .desc(usage.getDesc())
                        .timeType(usage.getType())
                        .startDate(usage.getStartDate())
                        .endDate(usage.getEndDate())
                        .usedTime(usage.getUsedTime())
                        .build())
                .toList();
    }

    /**
     * 유저의 월별 휴가 사용 통계 조회
     *
     * @param userId 유저 ID
     * @param year 년도
     * @return 월별 휴가 사용 통계 (1~12월)
     */
    public List<VacationServiceDto> getUserMonthlyVacationStats(String userId, String year) {
        // 유저 존재 확인
        userService.checkUserExist(userId);

        // 해당 년도의 1월 1일 ~ 12월 31일 사이의 휴가 사용 내역 조회
        LocalDateTime startDate = LocalDateTime.of(Integer.parseInt(year), 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(Integer.parseInt(year), 12, 31, 23, 59, 59);

        List<VacationUsage> usages = vacationUsageRepository.findByUserIdAndPeriodWithUser(userId, startDate, endDate);

        // 월별 사용량 Map 생성 및 0 초기화 (순서 보장위해 LinkedHashMap 사용)
        Map<Integer, BigDecimal> monthlyMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyMap.put(i, BigDecimal.ZERO);
        }

        // 월별 사용량 집계 (startDate의 월 기준)
        for (VacationUsage usage : usages) {
            int month = usage.getStartDate().getMonthValue();
            monthlyMap.merge(month, usage.getUsedTime(), BigDecimal::add);
        }

        return monthlyMap.entrySet().stream()
                .map(e -> VacationServiceDto.builder()
                            .month(e.getKey())
                            .usedTime(e.getValue())
                            .build()
                )
                .toList();
    }

    /**
     * 유저의 휴가 사용 통계 조회 (현재/이전달)
     *
     * @param userId 유저 ID
     * @param baseTime 기준 시간
     * @return 현재 및 이전달 휴가 통계
     */
    public VacationServiceDto getUserVacationStats(String userId, LocalDateTime baseTime) {
        // 유저 존재 확인
        userService.checkUserExist(userId);

        // 현재 통계 계산
        VacationServiceDto curStats = calculateStatsForBaseTime(userId, baseTime);

        // 이전 달 통계 계산
        VacationServiceDto prevStats = calculateStatsForBaseTime(userId, baseTime.minusMonths(1));

        return VacationServiceDto.builder()
                .remainTime(curStats.getRemainTime())
                .usedTime(curStats.getUsedTime())
                .expectUsedTime(curStats.getExpectUsedTime())
                .prevRemainTime(prevStats.getRemainTime())
                .prevUsedTime(prevStats.getUsedTime())
                .prevExpectUsedTime(prevStats.getExpectUsedTime())
                .build();
    }

    /**
     * baseTime 기준 휴가 통계 계산 헬퍼 메서드
     */
    private VacationServiceDto calculateStatsForBaseTime(String userId, LocalDateTime baseTime) {
        // baseTime 기준으로 유효한 VacationGrant 조회
        List<VacationGrant> validGrants = vacationGrantRepository.findValidGrantsByUserIdAndBaseTime(userId, baseTime);

        // 총 부여 시간 계산
        BigDecimal totalGranted = validGrants.stream()
                .map(VacationGrant::getGrantTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // baseTime 이전에 사용한 VacationUsage 조회 및 합산
        List<VacationUsage> usedUsages = vacationUsageRepository.findUsedByUserIdAndBaseTime(userId, baseTime);
        BigDecimal totalUsedTime = usedUsages.stream()
                .map(VacationUsage::getUsedTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // baseTime 이후 사용 예정인 VacationUsage 조회 및 합산
        List<VacationUsage> expectedUsages = vacationUsageRepository.findExpectedByUserIdAndBaseTime(userId, baseTime);
        BigDecimal totalExpectUsedTime = expectedUsages.stream()
                .map(VacationUsage::getUsedTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 잔여 시간 계산
        BigDecimal totalRemainTime = totalGranted.subtract(totalUsedTime);

        return VacationServiceDto.builder()
                .remainTime(totalRemainTime)
                .usedTime(totalUsedTime)
                .expectUsedTime(totalExpectUsedTime)
                .build();
    }

    public VacationUsage validateAndGetVacationUsage(Long vacationUsageId) {
        Optional<VacationUsage> usage = vacationUsageRepository.findById(vacationUsageId);
        usage.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation.usage", null, null)));
        return usage.get();
    }

    @Transactional
    public Long createVacationPolicy(VacationPolicyServiceDto data) {
        VacationPolicyStrategy strategy = vacationPolicyStrategyFactory.getStrategy(data.getGrantMethod());
        return strategy.registVacationPolicy(data);
    }

    public VacationPolicyServiceDto getVacationPolicy(Long vacationPolicyId) {
        VacationPolicy policy = validateAndGetVacationPolicy(vacationPolicyId);

        // 반복 부여 정책일 경우 한국어 설명 생성
        String repeatGrantDescription = null;
        if (policy.getGrantMethod() == GrantMethod.REPEAT_GRANT) {
            repeatGrantDescription = RepeatGrant.generateRepeatGrantDescription(policy);
        }

        return VacationPolicyServiceDto.builder()
                .id(policy.getId())
                .name(policy.getName())
                .desc(policy.getDesc())
                .vacationType(policy.getVacationType())
                .grantMethod(policy.getGrantMethod())
                .isFlexibleGrant(policy.getIsFlexibleGrant())
                .grantTime(policy.getGrantTime())
                .minuteGrantYn(policy.getMinuteGrantYn())
                .repeatUnit(policy.getRepeatUnit())
                .repeatInterval(policy.getRepeatInterval())
                .specificMonths(policy.getSpecificMonths())
                .specificDays(policy.getSpecificDays())
                .effectiveType(policy.getEffectiveType())
                .expirationType(policy.getExpirationType())
                .repeatGrantDescription(repeatGrantDescription)
                .build();
    }

    public List<VacationPolicyServiceDto> getVacationPolicies() {
        List<VacationPolicy> policies = vacationPolicyRepository.findVacationPolicies();
        return policies.stream()
                .map(p -> {
                    // 반복 부여 정책일 경우 한국어 설명 생성
                    String repeatGrantDescription = null;
                    if (p.getGrantMethod() == GrantMethod.REPEAT_GRANT) {
                        repeatGrantDescription = RepeatGrant.generateRepeatGrantDescription(p);
                    }

                    return VacationPolicyServiceDto.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .desc(p.getDesc())
                            .vacationType(p.getVacationType())
                            .grantMethod(p.getGrantMethod())
                            .isFlexibleGrant(p.getIsFlexibleGrant())
                            .grantTime(p.getGrantTime())
                            .minuteGrantYn(p.getMinuteGrantYn())
                            .repeatUnit(p.getRepeatUnit())
                            .repeatInterval(p.getRepeatInterval())
                            .specificMonths(p.getSpecificMonths())
                            .specificDays(p.getSpecificDays())
                            .effectiveType(p.getEffectiveType())
                            .expirationType(p.getExpirationType())
                            .repeatGrantDescription(repeatGrantDescription)
                            .build();
                })
                .toList();
    }

    public VacationPolicy validateAndGetVacationPolicy(Long vacationPolicyId) {
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
        VacationPolicy vacationPolicy = validateAndGetVacationPolicy(vacationPolicyId);

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

        // 6. 해당 휴가 정책으로 부여된 모든 VacationGrant 회수 처리
        List<VacationGrant> grants = vacationGrantRepository.findByPolicyId(vacationPolicyId);
        int revokedGrantCount = 0;

        for (VacationGrant grant : grants) {
            // ACTIVE 상태인 grant만 회수 처리
            // EXHAUSTED(소진), EXPIRED(만료), REVOKED(이미 회수됨)는 스킵
            if (grant.getStatus() == GrantStatus.ACTIVE) {
                grant.revoke();
                revokedGrantCount++;

                log.info("Revoked vacation grant {} from user {} (remainTime: {})",
                        grant.getId(), grant.getUser().getId(), grant.getRemainTime());
            }
        }

        log.info("Revoked {} active vacation grants for vacation policy {}",
                revokedGrantCount, vacationPolicyId);
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
            VacationPolicy policy = validateAndGetVacationPolicy(policyId);
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
     * @param grantMethod 부여 방법 필터 (Optional)
     * @return 유저에게 할당된 휴가 정책 리스트
     */
    public List<VacationPolicyServiceDto> getUserAssignedVacationPolicies(String userId, GrantMethod grantMethod) {
        // 유저 존재 확인
        userService.checkUserExist(userId);

        // 유저에게 할당된 휴가 정책 조회
        List<UserVacationPolicy> userVacationPolicies = userVacationPolicyRepository.findByUserId(userId);

        return userVacationPolicies.stream()
                // grantMethod 필터링 (null이면 모두 반환)
                .filter(uvp -> grantMethod == null || uvp.getVacationPolicy().getGrantMethod() == grantMethod)
                .map(uvp -> {
                    VacationPolicy policy = uvp.getVacationPolicy();

                    // 반복 부여 정책일 경우 한국어 설명 생성
                    String repeatGrantDescription = null;
                    if (policy.getGrantMethod() == GrantMethod.REPEAT_GRANT) {
                        repeatGrantDescription = RepeatGrant.generateRepeatGrantDescription(policy);
                    }

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
                            .firstGrantDate(policy.getFirstGrantDate())
                            .isRecurring(policy.getIsRecurring())
                            .maxGrantCount(policy.getMaxGrantCount())
                            .approvalRequiredCount(policy.getApprovalRequiredCount())
                            .effectiveType(policy.getEffectiveType())
                            .expirationType(policy.getExpirationType())
                            .isFlexibleGrant(policy.getIsFlexibleGrant())
                            .minuteGrantYn(policy.getMinuteGrantYn())
                            .repeatGrantDescription(repeatGrantDescription)
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
        validateAndGetVacationPolicy(vacationPolicyId);

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

        // 6. 해당 유저에게 해당 정책으로 부여된 VacationGrant 회수 처리
        List<VacationGrant> userGrants = vacationGrantRepository.findByUserId(userId);
        int revokedGrantCount = 0;

        for (VacationGrant grant : userGrants) {
            // 해당 정책으로 부여된 grant인지 확인
            if (grant.getPolicy().getId().equals(vacationPolicyId)) {
                // ACTIVE 상태인 grant만 회수 처리
                if (grant.getStatus() == GrantStatus.ACTIVE) {
                    grant.revoke();
                    revokedGrantCount++;

                    log.info("Revoked vacation grant {} from user {} (remainTime: {})", grant.getId(), userId, grant.getRemainTime());
                }
            }
        }

        log.info("Revoked vacation policy {} from user {} ({} grants revoked)", vacationPolicyId, userId, revokedGrantCount);

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
        int totalRevokedGrants = 0;

        // 2. 각 휴가 정책에 대해 회수 처리
        for (Long policyId : vacationPolicyIds) {
            try {
                // 휴가 정책 존재 확인
                validateAndGetVacationPolicy(policyId);

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

                // 해당 유저에게 해당 정책으로 부여된 VacationGrant 회수 처리
                List<VacationGrant> userGrants = vacationGrantRepository.findByUserId(userId);
                int revokedGrantCount = 0;

                for (VacationGrant grant : userGrants) {
                    // 해당 정책으로 부여된 grant인지 확인
                    if (grant.getPolicy().getId().equals(policyId)) {
                        // ACTIVE 상태인 grant만 회수 처리
                        if (grant.getStatus() == GrantStatus.ACTIVE) {
                            grant.revoke();
                            revokedGrantCount++;
                        }
                    }
                }

                totalRevokedGrants += revokedGrantCount;
                revokedIds.add(policyId);

                log.info("Revoked vacation policy {} from user {} ({} grants revoked)", policyId, userId, revokedGrantCount);

            } catch (Exception e) {
                log.error("Failed to revoke vacation policy {} from user {}: {}", policyId, userId, e.getMessage());
                // 일괄 처리 중 개별 에러는 스킵하고 계속 진행
            }
        }

        log.info("Revoked {} vacation policies from user {} ({} total grants revoked)", revokedIds.size(), userId, totalRevokedGrants);

        return revokedIds;
    }

    /**
     * 관리자가 특정 사용자에게 휴가를 직접 부여
     *
     * @param userId 유저 ID
     * @param data 휴가 부여 정보 (정책 ID, 부여 시간, 부여일, 만료일, 사유)
     * @return 생성된 VacationGrant
     */
    @Transactional
    public VacationGrant manualGrantVacation(String userId, VacationServiceDto data) {
        // 1. 사용자 존재 확인
        User user = userService.checkUserExist(userId);

        // 2. 휴가 정책 존재 확인
        VacationPolicy policy = validateAndGetVacationPolicy(data.getPolicyId());

        // 3. 휴가 정책의 부여 방식이 MANUAL_GRANT인지 확인
        if (policy.getGrantMethod() != GrantMethod.MANUAL_GRANT) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.notManualGrantPolicy", null, null)
            );
        }

        // 4. 부여 시간 결정 (가변 부여 여부에 따라 분기)
        BigDecimal grantTime;
        if (YNType.isN(policy.getIsFlexibleGrant())) {
            // 가변 부여 여부가 N인 경우: 정책에 정의된 grantTime 사용
            grantTime = policy.getGrantTime();
            if (grantTime == null || grantTime.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.policyGrantTimeInvalid", null, null)
                );
            }
        } else {
            // 가변 부여 여부가 Y인 경우: 사용자가 DTO로 넘긴 grantTime 사용
            grantTime = data.getGrantTime();
            if (grantTime == null || grantTime.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.grantTimeRequired", null, null)
                );
            }
        }

        // 5. grantDate와 expiryDate 결정 (사용자 입력 우선, 없으면 정책 기반 계산)
        LocalDateTime grantDate;
        LocalDateTime expiryDate;

        if (data.getGrantDate() != null && data.getExpiryDate() != null) {
            // 사용자가 입력한 부여일과 만료일 사용
            grantDate = data.getGrantDate();
            expiryDate = data.getExpiryDate();
            log.info("Using user-provided dates - grantDate: {}, expiryDate: {}", grantDate, expiryDate);
        } else {
            // 정책의 effectiveType, expirationType을 사용하여 계산
            LocalDateTime now = LocalDateTime.now();
            grantDate = policy.getEffectiveType().calculateDate(now);
            expiryDate = policy.getExpirationType().calculateDate(grantDate);
            log.info("Calculated dates using policy types - grantDate: {}, expiryDate: {}", grantDate, expiryDate);
        }

        // 6. 부여일과 만료일 검증 (부여일 < 만료일)
        if (PorestTime.isAfterThanEndDate(grantDate, expiryDate)) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.grantDateAfterExpiryDate", null, null)
            );
        }

        // 7. VacationGrant 생성
        VacationGrant vacationGrant = VacationGrant.createVacationGrant(
                user,
                policy,
                data.getDesc() != null ? data.getDesc() : "관리자 직접 부여",
                policy.getVacationType(),
                grantTime,
                grantDate,
                expiryDate
        );

        // 8. 저장
        vacationGrantRepository.save(vacationGrant);

        log.info("Manually granted vacation: grantId={}, userId={}, policyId={}, grantTime={}, grantDate={}, expiryDate={}",
                vacationGrant.getId(), userId, policy.getId(), grantTime, grantDate, expiryDate);

        return vacationGrant;
    }


    /**
     * 특정 휴가 부여 회수 (관리자가 직접 부여한 휴가를 취소)
     *
     * @param vacationGrantId 휴가 부여 ID
     * @return 회수된 VacationGrant
     */
    @Transactional
    public VacationGrant revokeVacationGrant(Long vacationGrantId) {
        // 1. VacationGrant 존재 확인
        VacationGrant grant = vacationGrantRepository.findById(vacationGrantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        ms.getMessage("error.notFound.vacationGrant", null, null)
                ));

        // 2. 이미 삭제된 경우
        if (grant.getIsDeleted() == YNType.Y) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.alreadyDeleted", null, null)
            );
        }

        // 3. ACTIVE 상태인 경우에만 회수 가능
        if (grant.getStatus() != GrantStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.notActiveGrant", null, null)
            );
        }

        // 4. 일부라도 사용된 경우 회수 불가
        if (grant.getRemainTime().compareTo(grant.getGrantTime()) < 0) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.partiallyUsedGrant", null, null)
            );
        }

        // 5. 회수 처리
        grant.revoke();

        log.info("Revoked vacation grant: grantId={}, userId={}, policyId={}, grantTime={}",
                grant.getId(), grant.getUser().getId(), grant.getPolicy().getId(), grant.getGrantTime());

        return grant;
    }

    /**
     * 휴가 신청 (ON_REQUEST 방식)
     *
     * @param userId 신청자 ID
     * @param data 휴가 신청 정보 (정책 ID, 휴가 사유(desc), 승인자 ID 리스트)
     * @return 생성된 VacationGrant ID
     */
    @Transactional
    public Long requestVacation(String userId, VacationServiceDto data) {
        // 1. 사용자 검증
        User user = userService.checkUserExist(userId);

        // 2. 휴가 정책 검증
        VacationPolicy policy = validateAndGetVacationPolicy(data.getPolicyId());

        // 3. 정책이 ON_REQUEST 방식인지 확인
        if (policy.getGrantMethod() != GrantMethod.ON_REQUEST) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.notOnRequestPolicy", null, null)
            );
        }

        // 4. 사용자에게 해당 정책이 할당되어 있는지 확인
        boolean hasPolicy = userVacationPolicyRepository.existsByUserIdAndVacationPolicyId(userId, data.getPolicyId());
        if (!hasPolicy) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.policyNotAssigned", null, null)
            );
        }

        // 5. 신청 사유 필수 검증
        if (data.getDesc() == null || data.getDesc().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.requestReasonRequired", null, null)
            );
        }

        // 6. 승인자 목록 검증
        List<String> approverIds = data.getApproverIds();
        Integer requiredCount = policy.getApprovalRequiredCount();

        if (requiredCount != null && requiredCount > 0) {
            if (approverIds == null || approverIds.isEmpty()) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.approverRequired", null, null)
                );
            }

            if (approverIds.size() != requiredCount) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.approverCountMismatch",
                                new Object[]{requiredCount, approverIds.size()}, null)
                );
            }

            // 6-1. 승인자 중복 체크
            Set<String> uniqueApproverIds = new HashSet<>(approverIds);
            if (uniqueApproverIds.size() != approverIds.size()) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.duplicateApprover", null, null)
                );
            }

            // 6-2. 승인자 모두 존재하는지 확인
            for (String approverId : approverIds) {
                userService.checkUserExist(approverId);
            }

            // 6-3. 승인자가 부서장인지 확인 (headUserId로 검증)
            List<Department> departments = departmentRepository.findByUserIds(approverIds);

            // 부서장이 아닌 승인자 찾기
            Set<String> headUserIds = departments.stream()
                    .map(Department::getHeadUserId)
                    .collect(Collectors.toSet());

            List<String> nonHeadApprovers = approverIds.stream()
                    .filter(id -> !headUserIds.contains(id))
                    .collect(Collectors.toList());

            if (!nonHeadApprovers.isEmpty()) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.approverNotDepartmentHead",
                                new Object[]{String.join(", ", nonHeadApprovers)}, null)
                );
            }

            // 6-4. 승인자를 부서 레벨 순서로 정렬 (레벨 오름차순: 하위 부서장 먼저)
            Map<String, Department> approverDepartmentMap = departments.stream()
                    .collect(Collectors.toMap(Department::getHeadUserId, dept -> dept));

            approverIds.sort((id1, id2) -> {
                Department dept1 = approverDepartmentMap.get(id1);
                Department dept2 = approverDepartmentMap.get(id2);

                // 레벨이 작을수록(하위 부서) 먼저 승인
                return Long.compare(dept1.getLevel(), dept2.getLevel());
            });
        }

        // 7. 부여 시간 계산 (OnRequest 전략 사용)
        OnRequest onRequestStrategy = (OnRequest) vacationPolicyStrategyFactory.getStrategy(GrantMethod.ON_REQUEST);
        BigDecimal grantTime = onRequestStrategy.calculateGrantTime(policy, data.getGrantTime());

        // 8. VacationGrant 생성 (PENDING 상태)
        VacationGrant vacationGrant = VacationGrant.createPendingVacationGrant(
                user,
                policy,
                data.getDesc(),
                policy.getVacationType(),
                grantTime,
                data.getRequestStartTime(),
                data.getRequestEndTime(),
                data.getRequestDesc()
        );

        vacationGrantRepository.save(vacationGrant);

        // 9. 승인이 필요한 경우 VacationApproval 생성 (순서대로)
        if (requiredCount != null && requiredCount > 0 && approverIds != null) {
            List<VacationApproval> approvals = new ArrayList<>();
            int order = 1;
            for (String approverId : approverIds) {
                User approver = userService.checkUserExist(approverId);
                VacationApproval approval = VacationApproval.createVacationApproval(vacationGrant, approver, order);
                approvals.add(approval);
                order++;
            }
            vacationApprovalRepository.saveAll(approvals);

            log.info("휴가 신청 완료 - User: {}, Policy: {}, GrantId: {}, Approvers: {} (순서대로)",
                    userId, policy.getId(), vacationGrant.getId(), approverIds);
        } else {
            // 승인이 필요 없는 경우 즉시 ACTIVE 상태로 전환
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime grantDate = policy.getEffectiveType().calculateDate(now);
            LocalDateTime expiryDate = policy.getExpirationType().calculateDate(grantDate);
            vacationGrant.approve(grantDate, expiryDate);

            log.info("휴가 신청 완료 (즉시 승인) - User: {}, Policy: {}, GrantId: {}",
                    userId, policy.getId(), vacationGrant.getId());
        }

        return vacationGrant.getId();
    }

    /**
     * 휴가 승인 처리 (순차 승인)
     *
     * @param approvalId VacationApproval ID
     * @param approverId 승인자 ID
     * @return 처리된 VacationApproval ID
     */
    @Transactional
    public Long approveVacation(Long approvalId, String approverId) {
        // 1. VacationApproval 조회
        VacationApproval approval = vacationApprovalRepository.findByIdWithVacationGrantAndUser(approvalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        ms.getMessage("error.notfound.vacation.approval", null, null)
                ));

        // 2. 승인자 권한 검증
        if (!approval.getApprover().getId().equals(approverId)) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.notAuthorizedApprover", null, null)
            );
        }

        // 3. 이미 처리된 승인인지 확인
        if (!approval.isPending()) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.alreadyProcessed", null, null)
            );
        }

        // 4. 순차 승인 검증: 이전 순서의 승인자들이 모두 승인했는지 확인
        VacationGrant vacationGrant = approval.getVacationGrant();
        List<VacationApproval> allApprovals = vacationApprovalRepository.findByVacationGrantId(vacationGrant.getId());

        Integer currentOrder = approval.getApprovalOrder();

        // 현재 승인자보다 앞선 순서의 승인자 중 아직 승인하지 않은 사람이 있는지 확인
        boolean hasPendingPreviousApprovals = allApprovals.stream()
                .filter(a -> a.getApprovalOrder() < currentOrder)
                .anyMatch(a -> !a.isApproved());

        if (hasPendingPreviousApprovals) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.previousApprovalRequired", null, null)
            );
        }

        // 5. 승인 처리
        approval.approve();

        // 6. VacationGrant의 모든 승인이 완료되었는지 확인
        boolean allApproved = allApprovals.stream().allMatch(VacationApproval::isApproved);
        int totalApprovalCount = allApprovals.size();

        if (allApproved) {
            // 모든 승인이 완료되면 VacationGrant를 ACTIVE 상태로 전환
            VacationPolicy policy = vacationGrant.getPolicy();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime grantDate = policy.getEffectiveType().calculateDate(now);
            LocalDateTime expiryDate = policy.getExpirationType().calculateDate(grantDate);

            vacationGrant.approve(grantDate, expiryDate);

            log.info("휴가 전체 승인 완료 - VacationGrant ID: {}, Final Approver: {}, Status: ACTIVE",
                    vacationGrant.getId(), approverId);
        } else {
            // 승인자가 2명 이상이고 1명 이상이 승인한 경우 PROGRESS 상태로 전환
            if (totalApprovalCount >= 2) {
                vacationGrant.updateToProgress();
                log.info("휴가 승인 진행 중 - VacationGrant ID: {}, Approver: {} (순서: {}), Status: PROGRESS",
                        vacationGrant.getId(), approverId, currentOrder);
            }

            long pendingCount = allApprovals.stream().filter(VacationApproval::isPending).count();
            log.info("휴가 부분 승인 완료 - VacationGrant ID: {}, Approver: {} (순서: {}), 남은 승인: {}",
                    vacationGrant.getId(), approverId, currentOrder, pendingCount);
        }

        return approval.getId();
    }

    /**
     * 휴가 거부 처리
     *
     * @param approvalId VacationApproval ID
     * @param approverId 승인자 ID
     * @param data 거부 사유
     * @return 처리된 VacationApproval ID
     */
    @Transactional
    public Long rejectVacation(Long approvalId, String approverId, VacationApprovalServiceDto data) {
        // 1. VacationApproval 조회
        VacationApproval approval = vacationApprovalRepository.findByIdWithVacationGrantAndUser(approvalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        ms.getMessage("error.notfound.vacation.approval", null, null)
                ));

        // 2. 승인자 권한 검증
        if (!approval.getApprover().getId().equals(approverId)) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.notAuthorizedApprover", null, null)
            );
        }

        // 3. 이미 처리된 승인인지 확인
        if (!approval.isPending()) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.alreadyProcessed", null, null)
            );
        }

        // 4. 거부 사유 필수 검증
        if (data.getRejectionReason() == null || data.getRejectionReason().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.rejectionReasonRequired", null, null)
            );
        }

        // 5. 거부 처리
        approval.reject(data.getRejectionReason());

        // 6. VacationGrant를 REJECTED 상태로 전환
        VacationGrant vacationGrant = approval.getVacationGrant();
        vacationGrant.reject();

        log.info("휴가 거부 완료 - VacationGrant ID: {}, Approver: {}, Reason: {}",
                vacationGrant.getId(), approverId, data.getRejectionReason());

        return approval.getId();
    }

    /**
     * 휴가 신청 취소 처리
     *
     * @param vacationGrantId 휴가 부여 ID
     * @param userId 신청자 ID
     * @return 취소된 VacationGrant ID
     */
    @Transactional
    public Long cancelVacationRequest(Long vacationGrantId, String userId) {
        // 1. VacationGrant 조회
        VacationGrant vacationGrant = vacationGrantRepository.findById(vacationGrantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        ms.getMessage("error.notFound.vacationGrant", null, null)
                ));

        // 2. 신청자 권한 검증 (신청자만 취소 가능)
        if (!vacationGrant.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.notAuthorizedRequester", null, null)
            );
        }

        // 3. PENDING 상태인지 확인 (한 명도 승인하지 않은 상태에서만 취소 가능)
        if (vacationGrant.getStatus() != GrantStatus.PENDING) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.cannotCancelAfterApproval", null, null)
            );
        }

        // 4. 취소 처리
        vacationGrant.cancel();

        log.info("휴가 신청 취소 완료 - VacationGrant ID: {}, User: {}",
                vacationGrantId, userId);

        return vacationGrant.getId();
    }

    /**
     * 승인자에게 할당된 모든 휴가 신청 내역 조회 (상태 필터 옵션)
     * - 승인자가 처리해야 하는/처리한 모든 휴가 신청 내역 조회
     * - getUserRequestedVacations와 동일한 응답 형식
     *
     * @param approverId 승인자 ID
     * @param status 휴가 부여 상태 필터 (Optional)
     * @return 승인자에게 할당된 휴가 신청 내역 리스트
     */
    public List<VacationServiceDto> getAllVacationsByApprover(String approverId, GrantStatus status) {
        // 승인자 존재 확인
        userService.checkUserExist(approverId);

        // 승인자가 포함된 모든 VacationGrant ID 조회
        List<Long> vacationGrantIds = vacationApprovalRepository.findAllVacationGrantIdsByApproverId(approverId);

        if (vacationGrantIds.isEmpty()) {
            return List.of();
        }

        // VacationGrant 조회 (User, Policy 포함)
        List<VacationGrant> grants = vacationGrantRepository.findByIdsWithUserAndPolicy(vacationGrantIds);

        // 상태 필터링 (status가 null이 아닌 경우)
        if (status != null) {
            grants = grants.stream()
                    .filter(grant -> grant.getStatus() == status)
                    .toList();
        }

        // VacationServiceDto로 변환
        return grants.stream()
                .map(grant -> {
                    // 현재 승인 대기 중인 승인자 조회
                    User currentApprover = grant.getCurrentPendingApprover();

                    // 승인자 목록 조회 (approvalOrder 순서대로 정렬됨)
                    List<VacationApproval> approvals = vacationApprovalRepository.findByVacationGrantId(grant.getId());
                    List<VacationApprovalServiceDto> approvers = approvals.stream()
                            .sorted((a1, a2) -> Integer.compare(a1.getApprovalOrder(), a2.getApprovalOrder()))
                            .map(approval -> VacationApprovalServiceDto.builder()
                                    .id(approval.getId())
                                    .approverId(approval.getApprover().getId())
                                    .approverName(approval.getApprover().getName())
                                    .approvalOrder(approval.getApprovalOrder())
                                    .approvalStatus(approval.getApprovalStatus())
                                    .approvalDate(approval.getApprovalDate())
                                    .rejectionReason(approval.getRejectionReason())
                                    .build())
                            .toList();

                    return VacationServiceDto.builder()
                            .id(grant.getId())
                            .userId(grant.getUser().getId())
                            .user(grant.getUser())
                            .policyId(grant.getPolicy().getId())
                            .policyName(grant.getPolicy().getName())
                            .type(grant.getType())
                            .desc(grant.getDesc())
                            .grantTime(grant.getGrantTime())
                            .policyGrantTime(grant.getPolicy().getGrantTime())
                            .remainTime(grant.getRemainTime())
                            .grantDate(grant.getGrantDate())
                            .expiryDate(grant.getExpiryDate())
                            .requestStartTime(grant.getRequestStartTime())
                            .requestEndTime(grant.getRequestEndTime())
                            .requestDesc(grant.getRequestDesc())
                            .grantStatus(grant.getStatus())
                            .createDate(grant.getCreateDate())
                            .currentApproverId(currentApprover != null ? currentApprover.getId() : null)
                            .currentApproverName(currentApprover != null ? currentApprover.getName() : null)
                            .approvers(approvers)
                            .build();
                })
                .toList();
    }

    /**
     * 사용자 ID로 ON_REQUEST 방식의 모든 휴가 신청 내역 조회 (모든 상태 포함)
     * - 승인대기, 승인완료, 거부, 회수, 만료, 소진 등 모든 상태를 포함
     * - 신청일시 최신순으로 반환
     *
     * @param userId 사용자 ID
     * @return ON_REQUEST 방식의 모든 휴가 신청 내역
     */
    public List<VacationServiceDto> getAllRequestedVacationsByUserId(String userId) {
        // 사용자 존재 확인
        userService.checkUserExist(userId);

        // ON_REQUEST 방식의 모든 휴가 신청 내역 조회
        List<VacationGrant> grants = vacationGrantRepository.findAllRequestedVacationsByUserId(userId);

        return grants.stream()
                .map(grant -> {
                    // 현재 승인 대기 중인 승인자 조회
                    User currentApprover = grant.getCurrentPendingApprover();

                    // 승인자 목록 조회 (approvalOrder 순서대로 정렬됨)
                    List<VacationApproval> approvals = vacationApprovalRepository.findByVacationGrantId(grant.getId());
                    List<VacationApprovalServiceDto> approvers = approvals.stream()
                            .sorted((a1, a2) -> Integer.compare(a1.getApprovalOrder(), a2.getApprovalOrder()))
                            .map(approval -> VacationApprovalServiceDto.builder()
                                    .id(approval.getId())
                                    .approverId(approval.getApprover().getId())
                                    .approverName(approval.getApprover().getName())
                                    .approvalOrder(approval.getApprovalOrder())
                                    .approvalStatus(approval.getApprovalStatus())
                                    .approvalDate(approval.getApprovalDate())
                                    .rejectionReason(approval.getRejectionReason())
                                    .build())
                            .toList();

                    return VacationServiceDto.builder()
                            .id(grant.getId())
                            .policyId(grant.getPolicy().getId())
                            .policyName(grant.getPolicy().getName())
                            .type(grant.getType())
                            .desc(grant.getDesc())
                            .grantTime(grant.getGrantTime())
                            .policyGrantTime(grant.getPolicy().getGrantTime())
                            .remainTime(grant.getRemainTime())
                            .grantDate(grant.getGrantDate())
                            .expiryDate(grant.getExpiryDate())
                            .requestStartTime(grant.getRequestStartTime())
                            .requestEndTime(grant.getRequestEndTime())
                            .requestDesc(grant.getRequestDesc())
                            .grantStatus(grant.getStatus())
                            .createDate(grant.getCreateDate())
                            .currentApproverId(currentApprover != null ? currentApprover.getId() : null)
                            .currentApproverName(currentApprover != null ? currentApprover.getName() : null)
                            .approvers(approvers)
                            .build();
                })
                .toList();
    }

    /**
     * 사용자 ID로 ON_REQUEST 방식의 휴가 신청 통계 조회
     *
     * @param userId 사용자 ID
     * @return 휴가 신청 통계 정보
     */
    public VacationServiceDto getRequestedVacationStatsByUserId(String userId) {
        // 사용자 존재 확인
        userService.checkUserExist(userId);

        // ON_REQUEST 방식의 모든 휴가 신청 내역 조회
        List<VacationGrant> allGrants = vacationGrantRepository.findAllRequestedVacationsByUserId(userId);

        // 현재 날짜 기준
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfCurrentMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);

        // 1. 전체 신청 건수
        long totalRequestCount = allGrants.size();

        // 2. 이번 달 신청 건수
        long currentMonthRequestCount = allGrants.stream()
                .filter(grant -> grant.getRequestStartTime() != null &&
                        grant.getRequestStartTime().isAfter(startOfCurrentMonth))
                .count();

        // 전월 신청 건수 (증감 비율 계산용)
        long previousMonthRequestCount = allGrants.stream()
                .filter(grant -> grant.getRequestStartTime() != null &&
                        grant.getRequestStartTime().isAfter(startOfPreviousMonth) &&
                        grant.getRequestStartTime().isBefore(startOfCurrentMonth))
                .count();

        // 3. 증감 비율 계산
        Double changeRate = 0.0;
        if (previousMonthRequestCount > 0) {
            changeRate = ((double) (currentMonthRequestCount - previousMonthRequestCount) / previousMonthRequestCount) * 100.0;
        } else if (currentMonthRequestCount > 0) {
            changeRate = 100.0; // 전월 0건, 이번달 1건 이상인 경우 100% 증가
        }

        // 4. 대기 건수
        long pendingCount = allGrants.stream()
                .filter(grant -> grant.getStatus() == GrantStatus.PENDING)
                .count();

        // 5. 평균 처리 기간 (일수) - ACTIVE 또는 REJECTED 상태만 계산
        List<VacationGrant> processedGrants = allGrants.stream()
                .filter(grant -> grant.getStatus() == GrantStatus.ACTIVE || grant.getStatus() == GrantStatus.REJECTED)
                .filter(grant -> grant.getRequestStartTime() != null && grant.getModifyDate() != null)
                .toList();

        Double averageProcessingDays = 0.0;
        if (!processedGrants.isEmpty()) {
            long totalProcessingSeconds = processedGrants.stream()
                    .mapToLong(grant -> {
                        java.time.Duration duration = java.time.Duration.between(
                                grant.getRequestStartTime(),
                                grant.getModifyDate()
                        );
                        return duration.toDays();
                    })
                    .sum();
            averageProcessingDays = (double) totalProcessingSeconds / processedGrants.size();
        }

        // 6. 진행 중 건수
        long progressCount = allGrants.stream()
                .filter(grant -> grant.getStatus() == GrantStatus.PROGRESS)
                .count();

        // 7. 승인 건수
        long approvedCount = allGrants.stream()
                .filter(grant -> grant.getStatus() == GrantStatus.ACTIVE)
                .count();

        // 8. 승인율 계산
        Double approvalRate = 0.0;
        if (totalRequestCount > 0) {
            approvalRate = ((double) approvedCount / totalRequestCount) * 100.0;
        }

        // 9. 반려 건수
        long rejectedCount = allGrants.stream()
                .filter(grant -> grant.getStatus() == GrantStatus.REJECTED)
                .count();

        // 10. 취소 건수
        long canceledCount = allGrants.stream()
                .filter(grant -> grant.getStatus() == GrantStatus.CANCELED)
                .count();

        // 11. 획득 휴가 시간 (승인된 건만)
        BigDecimal acquiredVacationTime = allGrants.stream()
                .filter(grant -> grant.getStatus() == GrantStatus.ACTIVE)
                .map(VacationGrant::getGrantTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 12. 획득 휴가 일수 문자열
        String acquiredVacationTimeStr = VacationTimeType.convertValueToDay(acquiredVacationTime);

        return VacationServiceDto.builder()
                .totalRequestCount(totalRequestCount)
                .currentMonthRequestCount(currentMonthRequestCount)
                .changeRate(changeRate)
                .pendingCount(pendingCount)
                .averageProcessingDays(averageProcessingDays)
                .progressCount(progressCount)
                .approvedCount(approvedCount)
                .approvalRate(approvalRate)
                .rejectedCount(rejectedCount)
                .canceledCount(canceledCount)
                .acquiredVacationTime(acquiredVacationTime)
                .acquiredVacationTimeStr(acquiredVacationTimeStr)
                .grantsList(allGrants)
                .build();
    }

    /**
     * 유저의 휴가 정책 할당 상태 조회
     * - 전체 휴가 정책 중 해당 유저에게 할당된 정책과 할당되지 않은 정책을 분리하여 반환
     *
     * @param userId 유저 ID
     * @return 할당된 정책 리스트와 할당되지 않은 정책 리스트
     */
    public VacationServiceDto getVacationPolicyAssignmentStatus(String userId) {
        // 1. 유저 존재 확인
        userService.checkUserExist(userId);

        // 2. 모든 휴가 정책 조회
        List<VacationPolicy> allPolicies = vacationPolicyRepository.findVacationPolicies();

        // 3. 유저에게 할당된 휴가 정책 조회
        List<UserVacationPolicy> userVacationPolicies = userVacationPolicyRepository.findByUserId(userId);

        // 4. 할당된 정책 ID Set 생성 (빠른 조회를 위해)
        Set<Long> assignedPolicyIds = userVacationPolicies.stream()
                .map(uvp -> uvp.getVacationPolicy().getId())
                .collect(Collectors.toSet());

        // 5. 할당된 정책과 할당되지 않은 정책 분리
        List<VacationPolicyServiceDto> assignedPolicies = allPolicies.stream()
                .filter(p -> assignedPolicyIds.contains(p.getId()))
                .map(p -> convertToPolicyServiceDto(p))
                .toList();

        List<VacationPolicyServiceDto> unassignedPolicies = allPolicies.stream()
                .filter(p -> !assignedPolicyIds.contains(p.getId()))
                .map(p -> convertToPolicyServiceDto(p))
                .toList();

        log.info("User {} vacation policy assignment status - assigned: {}, unassigned: {}",
                userId, assignedPolicies.size(), unassignedPolicies.size());

        return VacationServiceDto.builder()
                .assignedPolicies(assignedPolicies)
                .unassignedPolicies(unassignedPolicies)
                .build();
    }

    /**
     * VacationPolicy를 VacationPolicyServiceDto로 변환하는 헬퍼 메서드
     */
    private VacationPolicyServiceDto convertToPolicyServiceDto(VacationPolicy policy) {
        // 반복 부여 정책일 경우 한국어 설명 생성
        String repeatGrantDescription = null;
        if (policy.getGrantMethod() == GrantMethod.REPEAT_GRANT) {
            repeatGrantDescription = RepeatGrant.generateRepeatGrantDescription(policy);
        }

        return VacationPolicyServiceDto.builder()
                .id(policy.getId())
                .name(policy.getName())
                .desc(policy.getDesc())
                .vacationType(policy.getVacationType())
                .grantMethod(policy.getGrantMethod())
                .isFlexibleGrant(policy.getIsFlexibleGrant())
                .grantTime(policy.getGrantTime())
                .minuteGrantYn(policy.getMinuteGrantYn())
                .repeatUnit(policy.getRepeatUnit())
                .repeatInterval(policy.getRepeatInterval())
                .specificMonths(policy.getSpecificMonths())
                .specificDays(policy.getSpecificDays())
                .effectiveType(policy.getEffectiveType())
                .expirationType(policy.getExpirationType())
                .repeatGrantDescription(repeatGrantDescription)
                .build();
    }

    /**
     * 유저에게 부여된 휴가 정책 조회 (필터링 옵션 포함)
     * - 휴가 타입(vacationType)과 부여 방식(grantMethod)으로 필터링 가능
     *
     * @param userId 유저 ID
     * @param vacationType 휴가 타입 필터 (Optional)
     * @param grantMethod 부여 방식 필터 (Optional)
     * @return 필터링된 휴가 정책 리스트
     */
    public List<VacationPolicyServiceDto> getUserAssignedVacationPoliciesWithFilters(
            String userId, VacationType vacationType, GrantMethod grantMethod) {
        // 유저 존재 확인
        userService.checkUserExist(userId);

        // 필터링된 유저 휴가 정책 조회
        List<UserVacationPolicy> userVacationPolicies =
                userVacationPolicyRepository.findByUserIdWithFilters(userId, vacationType, grantMethod);

        return userVacationPolicies.stream()
                .map(uvp -> {
                    VacationPolicy policy = uvp.getVacationPolicy();

                    // 반복 부여 정책일 경우 한국어 설명 생성
                    String repeatGrantDescription = null;
                    if (policy.getGrantMethod() == GrantMethod.REPEAT_GRANT) {
                        repeatGrantDescription = RepeatGrant.generateRepeatGrantDescription(policy);
                    }

                    return VacationPolicyServiceDto.builder()
                            .id(policy.getId())
                            .name(policy.getName())
                            .desc(policy.getDesc())
                            .vacationType(policy.getVacationType())
                            .grantMethod(policy.getGrantMethod())
                            .grantTime(policy.getGrantTime())
                            .isFlexibleGrant(policy.getIsFlexibleGrant())
                            .minuteGrantYn(policy.getMinuteGrantYn())
                            .repeatUnit(policy.getRepeatUnit())
                            .repeatInterval(policy.getRepeatInterval())
                            .specificMonths(policy.getSpecificMonths())
                            .specificDays(policy.getSpecificDays())
                            .effectiveType(policy.getEffectiveType())
                            .expirationType(policy.getExpirationType())
                            .repeatGrantDescription(repeatGrantDescription)
                            .build();
                })
                .toList();
    }

}
