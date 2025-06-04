package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.repository.VacationHistoryRepositoryImpl;
import com.lshdainty.myhr.repository.VacationRepositoryImpl;
import com.lshdainty.myhr.service.vacation.*;
import com.lshdainty.myhr.util.MyhrTime;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public Long registVacation(Long userNo, String desc, VacationType type, BigDecimal grantTime, LocalDateTime occurDate, LocalDateTime expiryDate, Long addUserNo, String clientIP) {
        VacationService vacationService = switch (type) {
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
            default -> throw new IllegalArgumentException("Invalid VacationType");
        };

        return vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, addUserNo, clientIP);
    }

    @Transactional
    public Long useVacation(Long userNo, Long vacatoinId, String desc, VacationTimeType type, LocalDateTime startDate, LocalDateTime endDate, Long crtUserNo, String clientIP) {
        User user = userService.checkUserExist(userNo);
        Vacation vacation = checkVacationExist(vacatoinId);

        // 시작, 종료시간 시간 비교
        if (MyhrTime.isAfterThanEndDate(startDate, endDate)) {
            LocalDateTime temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        // 연차가 아닌 시간단위 휴가인 경우 유연근무제 시간 체크
        if (!type.equals(VacationTimeType.DAYOFF)) {
            if (user.isBetweenWorkTime(startDate.toLocalTime())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.worktime.startEndTime", null, null));
            }
        }

        // 주말 리스트 조회
        List<LocalDate> weekDays = MyhrTime.getBetweenDatesByDayOfWeek(startDate, endDate, new int[]{6, 7});

        // 공휴일 리스트 조회
        List<LocalDate> holidays = holidayRepositoryImpl.findHolidaysByStartEndDateWithType(
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                HolidayType.PUBLIC
        ).stream()
                .map(h -> LocalDate.parse(h.getDate(), DateTimeFormatter.BASIC_ISO_DATE))
                .toList();

        weekDays = MyhrTime.addAllDates(weekDays, holidays);

        // 두 날짜 간 모든 날짜 가져오기
        List<LocalDate> betweenDates = MyhrTime.getBetweenDates(startDate, endDate);
        log.info("betweenDates : {}, weekDays : {}", betweenDates, weekDays);
        betweenDates = MyhrTime.removeAllDates(betweenDates, weekDays);
        log.info("remainDays : {}", betweenDates);

        BigDecimal useTime = new BigDecimal("0.0000").add(type.convertToValue(betweenDates.size()));
        if (vacation.getRemainTime().compareTo(useTime) < 0) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.notRemainTime", null, null));
        }

        for (LocalDate betweenDate : betweenDates) {
            VacationHistory history = VacationHistory.createUseVacationHistory(
                    vacation,
                    desc,
                    type,
                    LocalDateTime.of(betweenDate, LocalTime.of(startDate.toLocalTime().getHour(), startDate.toLocalTime().getMinute(), 0)),
                    crtUserNo,
                    clientIP
            );
            vacationHistoryRepositoryImpl.save(history);
        }

        vacation.deductedVacation(useTime, crtUserNo, clientIP);

        return vacation.getId();
    }

    public List<Vacation> findVacationsByUser(Long userNo) {
        return vacationRepositoryImpl.findVacationsByUserNo(userNo);
    }

    public List<User> findVacationsByUserGroup() {
        return userRepositoryImpl.findUsersWithVacations();
    }

    public List<Vacation> getAvailableVacation(Long userNo, LocalDateTime startDate) {
        // 유저 조회
        userService.checkUserExist(userNo);

        // 시작 날짜를 기준으로 등록 가능한 휴가 목록 조회
        return vacationRepositoryImpl.findVacationsByBaseTime(userNo, startDate);
    }

    @Transactional
    public void deleteVacationHistory(Long vacationHistoryId, Long delUserNo, String clientIP) {
        VacationHistory history = checkVacationHistoryExist(vacationHistoryId);
        Vacation vacation = checkVacationExist(history.getVacation().getId());

        if (MyhrTime.isAfterThanEndDate(LocalDateTime.now(), vacation.getExpiryDate())) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.expiryDateisAfterThanNow", null, null));
        }

        if (Objects.isNull(history.getType())) {
            // 휴가 추가 내역
            if (vacation.getRemainTime().compareTo(history.getGrantTime()) < 0) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.notEnoughRemainTime", null, null));
            }

            history.deleteRegistVacationHistory(vacation, delUserNo, clientIP);
        } else {
            // 휴가 사용 내역
            if (MyhrTime.isAfterThanEndDate(LocalDateTime.now(), history.getUsedDateTime())) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.usedDateisAfterThanNow", null, null));
            }

            history.deleteUseVacationHistory(vacation, delUserNo, clientIP);
        }
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
}
