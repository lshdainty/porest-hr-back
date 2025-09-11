package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.repository.VacationHistoryRepositoryImpl;
import com.lshdainty.myhr.repository.VacationRepositoryImpl;
import com.lshdainty.myhr.service.dto.VacationServiceDto;
import com.lshdainty.myhr.service.vacation.*;
import com.lshdainty.myhr.type.HolidayType;
import com.lshdainty.myhr.type.VacationTimeType;
import com.lshdainty.myhr.util.PorestTime;
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
    private final VacationRepositoryImpl vacationRepositoryImpl;
    private final VacationHistoryRepositoryImpl vacationHistoryRepositoryImpl;
    private final UserRepositoryImpl userRepositoryImpl;
    private final HolidayRepositoryImpl holidayRepositoryImpl;
    private final UserService userService;

    @Transactional
    public Long registVacation(VacationServiceDto data, String addUserId, String clientIP) {
        VacationService vacationService = switch (data.getType()) {
            case ANNUAL ->
                    new Annual(ms, vacationRepositoryImpl, vacationHistoryRepositoryImpl, userRepositoryImpl, holidayRepositoryImpl, userService);
            case MATERNITY ->
                    new Maternity(ms, vacationRepositoryImpl, vacationHistoryRepositoryImpl, userRepositoryImpl, holidayRepositoryImpl, userService);
            case WEDDING ->
                    new Wedding(ms, vacationRepositoryImpl, vacationHistoryRepositoryImpl, userRepositoryImpl, holidayRepositoryImpl, userService);
            case BEREAVEMENT ->
                    new Bereavement(ms, vacationRepositoryImpl, vacationHistoryRepositoryImpl, userRepositoryImpl, holidayRepositoryImpl, userService);
            case OVERTIME ->
                    new Overtime(ms, vacationRepositoryImpl, vacationHistoryRepositoryImpl, userRepositoryImpl, holidayRepositoryImpl, userService);
        };

        return vacationService.registVacation(data, addUserId, clientIP);
    }

    @Transactional
    public Long useVacation(VacationServiceDto data, String crtUserId, String clientIP) {
        User user = userService.checkUserExist(data.getUserId());
        Vacation vacation = checkVacationExist(data.getId());

        // 시작, 종료시간 시간 비교
        if (PorestTime.isAfterThanEndDate(data.getStartDate(), data.getEndDate())) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.startIsAfterThanEnd", null, null));
        }

        // 연차가 아닌 시간단위 휴가인 경우 유연근무제 시간 체크
        if (!data.getTimeType().equals(VacationTimeType.DAYOFF)) {
            if (!user.isBetweenWorkTime(data.getStartDate().toLocalTime(), data.getEndDate().toLocalTime())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.worktime.startEndTime", null, null));
            }
        }

        // 주말 리스트 조회
        List<LocalDate> weekDays = PorestTime.getBetweenDatesByDayOfWeek(data.getStartDate(), data.getEndDate(), new int[]{6, 7}, ms);

        // 공휴일 리스트 조회
        List<LocalDate> holidays = holidayRepositoryImpl.findHolidaysByStartEndDateWithType(
                data.getStartDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                data.getEndDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                HolidayType.PUBLIC
        ).stream()
                .map(h -> LocalDate.parse(h.getDate(), DateTimeFormatter.BASIC_ISO_DATE))
                .toList();

        weekDays = PorestTime.addAllDates(weekDays, holidays);

        // 두 날짜 간 모든 날짜 가져오기
        List<LocalDate> betweenDates = PorestTime.getBetweenDates(data.getStartDate(), data.getEndDate(), ms);
        log.info("betweenDates : {}, weekDays : {}", betweenDates, weekDays);
        // 사용자가 캘린더에서 선택한 날짜 중 휴일, 공휴일 제거
        betweenDates = PorestTime.removeAllDates(betweenDates, weekDays);
        log.info("remainDays : {}", betweenDates);

        // 등록하려는 총 사용시간 계산
        BigDecimal useTime = new BigDecimal("0.0000").add(data.getTimeType().convertToValue(betweenDates.size()));
        if (vacation.getRemainTime().compareTo(useTime) < 0) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.notEnoughRemainTime", null, null));
        }

        // 휴가 사용 내역 등록
        for (LocalDate betweenDate : betweenDates) {
            VacationHistory history = VacationHistory.createUseVacationHistory(
                    vacation,
                    data.getDesc(),
                    data.getTimeType(),
                    LocalDateTime.of(betweenDate, LocalTime.of(data.getStartDate().toLocalTime().getHour(), data.getStartDate().toLocalTime().getMinute(), 0)),
                    crtUserId,
                    clientIP
            );
            vacationHistoryRepositoryImpl.save(history);
        }

        // 사용한 휴가 차감
        vacation.deductedVacation(useTime, crtUserId, clientIP);

        return vacation.getId();
    }

    public List<Vacation> getUserVacations(String userId) {
        return vacationRepositoryImpl.findVacationsByUserId(userId);
    }

    public List<User> getUserGroupVacations() {
        return userRepositoryImpl.findUsersWithVacations();
    }

    public List<Vacation> getAvailableVacations(String userId, LocalDateTime startDate) {
        // 유저 조회
        userService.checkUserExist(userId);

        // 시작 날짜를 기준으로 등록 가능한 휴가 목록 조회
        return vacationRepositoryImpl.findVacationsByBaseTime(userId, startDate);
    }

    @Transactional
    public void deleteVacationHistory(Long vacationHistoryId, String delUserId, String clientIP) {
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
            history.deleteRegistVacationHistory(vacation, delUserId, clientIP);
        } else {
            // 휴가 사용 내역
            if (PorestTime.isAfterThanEndDate(LocalDateTime.now(), history.getUsedDateTime())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.delete.isBeforeThanNow", null, null));
            }

            // 휴가 사용 내역은 삭제하고 차감된 휴가 추가
            history.deleteUseVacationHistory(vacation, delUserId, clientIP);
        }
    }

    public List<VacationServiceDto> getPeriodVacationUseHistories(LocalDateTime startDate, LocalDateTime endDate) {
        // 기간에 맞는 history 내역 가져오기
        List<VacationHistory> histories = vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(startDate, endDate);

        // 유저 정보 반환을 위해 vacation 정보 가져오기
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByIdsWithUser(histories.stream()
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

    public List<VacationServiceDto> getUserPeriodVacationUseHistories(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        // 기간에 맞는 유저 history 내역 가져오기
        List<VacationHistory> histories = vacationHistoryRepositoryImpl.findVacationUseHistorysByUserAndPeriod(userId, startDate, endDate);

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

    public List<VacationServiceDto> getUserMonthStatsVacationUseHistories(String userId, String year) {
        // 기간에 맞는 유저 history 내역 가져오기
        List<VacationHistory> histories = vacationHistoryRepositoryImpl.findVacationUseHistorysByUserAndPeriod(
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

    public VacationServiceDto getUserVacationUseStats(String userId, LocalDateTime baseTime) {
        // 기준 시점에 유효한 모든 휴가 & 전체 이력
        List<Vacation> curVacations = vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(userId, baseTime);
        List<Vacation> prevVacations = vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(userId, baseTime.minusMonths(1));

        // 삭제 안된 이력만 필터링
        List<VacationHistory> curHistories = curVacations.stream()
                .flatMap(v -> v.getHistorys().stream())
                .filter(vh -> "N".equals(vh.getDelYN()))
                .toList();
        List<VacationHistory> prevHistories = prevVacations.stream()
                .flatMap(v -> v.getHistorys().stream())
                .filter(vh -> "N".equals(vh.getDelYN()))
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
        Optional<Vacation> vacation = vacationRepositoryImpl.findById(vacationId);
        vacation.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation", null, null)));
        return vacation.get();
    }

    public VacationHistory checkVacationHistoryExist(Long vacationHistoryId) {
        Optional<VacationHistory> history = vacationHistoryRepositoryImpl.findById(vacationHistoryId);
        history.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation.history", null, null)));
        return history.get();
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
