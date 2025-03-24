package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.HolidayRepository;
import com.lshdainty.myhr.repository.ScheduleRepository;
import com.lshdainty.myhr.repository.UserRepository;
import com.lshdainty.myhr.repository.VacationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final VacationRepository vacationRepository;
    private final HolidayRepository holidayRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long addSchedule(Long userNo, Long vacationId, ScheduleType type, String desc, LocalDateTime start, LocalDateTime end, Long addUserNo, String clientIP) {
        // 유저 조회
        User user = userRepository.findById(userNo);

        // 유저 없으면 에러 반환
        if (Objects.isNull(user) || user.getDelYN().equals("Y")) { throw new IllegalArgumentException("user not found"); }

        // 휴가 조회
        Vacation vacation = vacationRepository.findById(vacationId);

        // 사용하려는 휴가가 없으면 에러 반환
        if (Objects.isNull(vacation) || vacation.getDelYN().equals("Y")) { throw new IllegalArgumentException("vacation not found"); }
        // 사용기한이 지난 휴가면 사용불가
        if (vacation.getExpiryDate().isBefore(LocalDateTime.now())) { throw new IllegalArgumentException("this vacation has expired"); }

        // 이제까지 해당 휴가에 사용된 스케줄 리스트 가져오기
        List<Schedule> findSchedules = scheduleRepository.findCountByVacation(vacation);

        // 공휴일 리스트를 가져오기 위한 startDate 최소값 구하기
        int minStartDate = findSchedules.stream()
                .map(s -> s.getStartDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                .mapToInt(Integer::parseInt)
                .summaryStatistics()
                .getMin();
        minStartDate = Math.min(minStartDate, Integer.parseInt(start.format(DateTimeFormatter.BASIC_ISO_DATE)));

        // 공휴일 리스트를 가져오기 위한 endDate 최대값 구하기
        int maxEndDate = findSchedules.stream()
                .map(s -> s.getEndDate().format(DateTimeFormatter.BASIC_ISO_DATE))
                .mapToInt(Integer::parseInt)
                .summaryStatistics()
                .getMax();
        maxEndDate = Math.max(maxEndDate, Integer.parseInt(end.format(DateTimeFormatter.BASIC_ISO_DATE)));

        log.debug("add schedule minStartDate : {}, maxEndDate : {}", minStartDate, maxEndDate);

        // 계산에 필요한 공휴일 리스트 가져오기
        List<Holiday> holidays = holidayRepository.findHolidaysByStartEndDate(Integer.toString(minStartDate), Integer.toString(maxEndDate));

        // 공휴일 리스트 타입 변경
        List<LocalDate> holidayDates = holidays.stream()
                .map(h -> LocalDate.parse(h.getDate(), DateTimeFormatter.BASIC_ISO_DATE))
                .toList();

        // 사용된 시간 계산
        BigDecimal used = new BigDecimal(0);
        for (Schedule schedule : findSchedules) {
            used = used.add(calculateRealUsed(schedule, holidayDates));
        }

        // 휴가 등록이 가능한지 확인을 위한 스케줄 생성
        Schedule schedule = Schedule.createSchedule(user, vacation, desc, type, start, end, addUserNo, clientIP);

        // 사용할 휴가의 실제 사용 시간 계산
        BigDecimal toBeUse = calculateRealUsed(schedule, holidayDates);
        log.debug("add schedule grantTime : {}, used : {}, toBeUse : {}", vacation.getGrantTime(), used, toBeUse);

        // 남은 시간 계산
        // grantTime - totalUsed - tobeuse < 0
        if (vacation.getGrantTime().subtract(used).subtract(toBeUse).compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("there is not enough vacation left"); }

        // start, end 시간이 사용자 workTime에 맞도록 설정되어 있는지 확인
        if (schedule.getStartDate().isAfter(schedule.getEndDate())) { throw new IllegalArgumentException("the start time is greater than the end time"); }
        if (!schedule.isBetweenWorkTime()) { throw new IllegalArgumentException("please match the start and end times to work time"); }

        // 휴가 등록
        scheduleRepository.save(schedule);

        return schedule.getId();
    }

    @Transactional
    public Long addSchedule(Long userNo, ScheduleType type, String desc, LocalDateTime start, LocalDateTime end, Long addUserNo, String clientIP) {
        // 유저 조회
        User user = userRepository.findById(userNo);

        // 유저 없으면 에러 반환
        if (Objects.isNull(user) || user.getDelYN().equals("Y")) { throw new IllegalArgumentException("user not found"); }

        Schedule schedule = Schedule.createSchedule(user, null, desc, type, start, end, addUserNo, clientIP);

        // 휴가 등록
        scheduleRepository.save(schedule);

        return schedule.getId();
    }

    public List<Schedule> findSchedulesByUserNo(Long userNo) {
        return scheduleRepository.findSchedulesByUserNo(userNo);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long delUserNo, String clientIP) {
        Schedule schedule = scheduleRepository.findById(scheduleId);

        if (Objects.isNull(schedule) || schedule.getDelYN().equals("Y")) { throw new IllegalArgumentException("schedule not found"); }

        if (schedule.getEndDate().isBefore(LocalDateTime.now())) { throw new IllegalArgumentException("Past schedules cannot be deleted"); }

        schedule.deleteSchedule(delUserNo, clientIP);
    }

    /**
     * 사용자가 실제 사용한 휴가일수를 계산하기 위한 함수
     * 캘린더에서 드래그해서 휴가를 등록하는 경우 중간에
     * 주말, 공휴일이 포함되는 경우가 있어 제외한 후
     * 실제로 사용한 휴가일수를 알아내기위해 사용함
     *
     * @param schedule 계산 대상 스케줄
     * @param holidays 계산에 필요한 공휴일 리스트
     * @return 스케줄의 실제 사용 시간 (휴가, 주말 등 제외)
     */
    public BigDecimal calculateRealUsed(Schedule schedule, List<LocalDate> holidays) {
        BigDecimal used = new BigDecimal(0);

        List<LocalDate> dates = schedule.getBetweenDatesByDayOfWeek(new int[]{6, 7});
        dates.addAll(holidays);
        log.debug("calculateRealUse dates : {}", dates);

        List<LocalDate> results = schedule.removeAllDates(dates);

        used = used.add(schedule.getType().convertToValue(results.size()));

        return used;
    }
}
