package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.repository.VacationHistoryRepositoryImpl;
import com.lshdainty.myhr.repository.VacationRepositoryImpl;
import com.lshdainty.myhr.service.dto.VacationServiceDto;
import com.lshdainty.myhr.service.vacation.*;
import com.lshdainty.myhr.util.MyhrTime;
import lombok.NoArgsConstructor;
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
import java.util.ArrayList;
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
    public Long useVacation(Long userNo, Long vacatoinId, String desc, VacationTimeType type, LocalDateTime startDate, LocalDateTime endDate, Long addUserNo, String clientIP) {
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
                throw new IllegalArgumentException(ms.getMessage("error.vaildate.worktime.startendtime", null, null));
            }
        }

        // 주말 리스트 조회
        List<LocalDate> weekDays = MyhrTime.getBetweenDatesByDayOfWeek(startDate, endDate, new int[]{6, 7});

        // 공휴일 리스트 조회
        List<LocalDate> holidays = holidayRepositoryImpl.findHolidaysByStartEndDateWithType(
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                HolidayType.PUBLIC
        )
                .stream()
                .map(h -> LocalDate.parse(h.getDate(), DateTimeFormatter.BASIC_ISO_DATE))
                .toList();

        weekDays = MyhrTime.addAllDates(weekDays, holidays);
        log.info("weekDays : {}", weekDays);

        // 두 날짜 간 모든 날짜 가져오기
        List<LocalDate> betweenDates = MyhrTime.getBetweenDates(startDate, endDate);
        log.info("betweenDates1 : {}", betweenDates);
        betweenDates = MyhrTime.removeAllDates(betweenDates, weekDays);
        log.info("betweenDates2 : {}", betweenDates);

        BigDecimal useTime = new BigDecimal("0.0000").add(type.convertToValue(betweenDates.size()));
        if (vacation.getRemainTime().compareTo(useTime) < 0) {
            throw new IllegalArgumentException(ms.getMessage("error.vaildate.notremaintime", null, null));
        }

        for (LocalDate betweenDate : betweenDates) {
            VacationHistory history = VacationHistory.createUseVacationHistory(
                    vacation,
                    desc,
                    type,
                    LocalDateTime.of(betweenDate, LocalTime.of(startDate.toLocalTime().getHour(), startDate.toLocalTime().getMinute(), 0)),
                    addUserNo,
                    clientIP
            );
            vacationHistoryRepositoryImpl.save(history);
        }

        vacation.deductedVacation(useTime, addUserNo, clientIP);

        return vacation.getId();
    }

    public List<Vacation> findVacationsByUser(Long userNo) {
        return vacationRepositoryImpl.findVacationsByUserNo(userNo);
    }

    public List<User> findVacationsByUserGroup() {
        return userRepositoryImpl.findUsersWithVacations();
    }

    @Transactional
    public Vacation editVacation(Long vacationId, Long userNo, String reqName, String reqDesc, VacationType reqType, BigDecimal reqGrantTime, LocalDateTime reqOccurDate, LocalDateTime reqExpiryDate, Long addUserNo, String clientIP) {
        Vacation findVacation = checkVacationExist(vacationId);

        VacationType type = null;
        if (Objects.isNull(reqType)) { type = findVacation.getType(); } else { type = reqType; }

//        BigDecimal grantTime = new BigDecimal(0);
//        if (reqGrantTime.compareTo(grantTime) == 0) { grantTime = findVacation.getGrantTime(); } else { grantTime = reqGrantTime; }

        LocalDateTime occurDate = null;
        if (Objects.isNull(reqOccurDate)) { occurDate = findVacation.getOccurDate(); } else { occurDate = reqOccurDate; }

        LocalDateTime expiryDate = null;
        if (Objects.isNull(reqExpiryDate)) { expiryDate = findVacation.getExpiryDate(); } else { expiryDate = reqExpiryDate; }

        User user = userService.checkUserExist(userNo);

//        Vacation newVacation = Vacation.createVacation(user, name, desc, type, grantTime, occurDate, expiryDate, addUserNo, clientIP);

//        if (newVacation.isBeforeOccur()) { throw new IllegalArgumentException("the expiration date is earlier than the occurrence date"); }

//        findVacation.deleteVacation(addUserNo, clientIP);
//        vacationRepositoryImpl.save(newVacation);

//        return vacationRepositoryImpl.findById(newVacation.getId());
        return null;
    }

    @Transactional
    public void deleteVacation(Long vacationId, Long delUserNo, String clientIP) {
        Vacation vacation = checkVacationExist(vacationId);
//        vacation.deleteVacation(delUserNo, clientIP);
    }

    public List<VacationServiceDto> checkPossibleVacations(Long userNo, LocalDateTime standardTime) {
        // 유저 조회
        userService.checkUserExist(userNo);

        // 시작 날짜를 기준으로 등록 가능한 휴가날짜를 조회
        List<Vacation> vacations = vacationRepositoryImpl.findVacationsByParameterTimeWithSchedules(userNo, standardTime);

        List<VacationServiceDto> vacationDtos = new ArrayList<>();
        for (Vacation vacation : vacations) {
            VacationServiceDto vacationDto = VacationServiceDto.builder()
                    .id(vacation.getId())
                    .user(vacation.getUser())
                    .scheduleDtos(new ArrayList<>())
                    .type(vacation.getType())
                    .usedTime(new BigDecimal("0.0000"))
                    .remainTime(new BigDecimal("0.0000"))
                    .occurDate(vacation.getOccurDate())
                    .expiryDate(vacation.getExpiryDate())
                    .build();

//            if (vacation.getSchedules().isEmpty()) {
//                vacationDto.setRemainTime(vacation.getGrantTime());
//                vacationDtos.add(vacationDto);
//                continue;
//            }

//            List<ScheduleServiceDto> scheduleDtos = scheduleService.convertRealUsedTimeDto(vacation.getSchedules());
//            BigDecimal usedTime = new BigDecimal("0.0000");
//            for (ScheduleServiceDto scheduleDto : scheduleDtos) {
//                usedTime = usedTime.add(scheduleDto.getRealUsedTime());
//            }

//            if (vacation.getGrantTime().compareTo(usedTime) == 0) { continue; }

//            vacationDto.setScheduleDtos(scheduleDtos);
//            vacationDto.setUsedTime(usedTime);
//            vacationDto.setRemainTime(vacation.getGrantTime().subtract(usedTime));

            vacationDtos.add(vacationDto);
        }

        return vacationDtos;
    }

    public Vacation checkVacationExist(Long vacationId) {
        Optional<Vacation> vacation = vacationRepositoryImpl.findById(vacationId);
        vacation.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.vacation", null, null)));
        return vacation.get();
    }
}
