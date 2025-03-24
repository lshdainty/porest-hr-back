package com.lshdainty.myhr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "deptop_schedule")
public class Schedule extends AuditingFields {
    @Id
    @GeneratedValue
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_id")
    private Vacation vacation;

    @Column(name = "schedule_type")
    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    @Column(name = "schedule_desc")
    private String desc;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "del_yn")
    private String delYN;

    // 스케줄 생성자 (setter말고 해당 메소드 사용할 것)
    public static Schedule createSchedule(User user, Vacation vacation, String desc, ScheduleType type, LocalDateTime startDate, LocalDateTime endDate, Long userNo, String clientIP) {
        Schedule schedule = new Schedule();
        schedule.user = user;
        schedule.vacation = vacation;
        schedule.desc = desc;
        schedule.type = type;
        schedule.startDate = startDate;
        schedule.endDate = endDate;
        schedule.delYN = "N";
        schedule.setCreated(LocalDateTime.now(), userNo, clientIP);
        return schedule;
    }

    // 스케줄 삭제 (setter말고 해당 메소드 사용할 것)
    public void deleteSchedule(Long userNo, String clientIP) {
        this.delYN = "Y";
        this.setDeleted(LocalDateTime.now(), userNo, clientIP);
    }

    /* 비즈니스 편의 메소드 */

    /**
     * startDate, endDate 사이의
     * 모든 날짜들의 목록을 반환
     *
     * @return start ~ end 사이의 모든 날짜들
     */
    public List<LocalDate> getBetweenDates() {
        return getStartDate().toLocalDate().datesUntil(getEndDate().toLocalDate().plusDays(1))
                .collect(Collectors.toList());
    }

    /**
     * 시작, 끝 기간에 해당하는 모든 날짜 중
     * 사용자가 선택한 요일에 해당하는 모든 날짜를 반환하는 함수
     *
     * @param daysOfWeek int로 된 요일 리스트 (1 월요일 ~ 7 일요일)
     * @return 요일에 해당하는 모든 날짜들
     */
    public List<LocalDate> getBetweenDatesByDayOfWeek(int[] daysOfWeek) {
        List<DayOfWeek> targetDays = new ArrayList<>();
        for (int day : daysOfWeek) {
            targetDays.add(DayOfWeek.of(day));
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate checkDay = getStartDate().toLocalDate();
        while (!checkDay.isAfter(getEndDate().toLocalDate())) {
            if (targetDays.contains(checkDay.getDayOfWeek())) {
                dates.add(checkDay);
            }
            checkDay = checkDay.plusDays(1);
        }

        return dates;
    }

    /**
     * 두 날짜 사이에 있는 모든 날짜들에서
     * 사용자가 넘긴 target을 합친 날짜들을 반환하는 함수
     *
     * @param dates 합칠 날짜 리스트들
     * @return source - target을 한 날짜 리스트 반환
     */
    public List<LocalDate> addAllDates(List<LocalDate> dates) {
        Set<LocalDate> sourceSet = new HashSet<>(getBetweenDates());
        Set<LocalDate> targetSet = new HashSet<>(dates);
        sourceSet.addAll(targetSet);
        return sourceSet.stream()
                .map(LocalDate::from)
                .collect(Collectors.toList());
    }

    /**
     * 두 날짜 사이에 있는 모든 날짜들에서
     * 사용자가 넘긴 target을 제외하고 남은 날짜들만 반환하는 함수
     *
     * @param dates 제외할 날짜 리스트들
     * @return source - target을 한 날짜 리스트 반환
     */
    public List<LocalDate> removeAllDates(List<LocalDate> dates) {
        Set<LocalDate> sourceSet = new HashSet<>(getBetweenDates());
        Set<LocalDate> targetSet = new HashSet<>(dates);
        sourceSet.removeAll(targetSet);
        return sourceSet.stream()
                .map(LocalDate::from)
                .collect(Collectors.toList());
    }

    /**
     * start 날짜가 유저의 유연근무제에 맞춰
     * 정상적으로 설정되어 있는지 확인하는 함수
     *
     * @return true, false 반환
     */
    public boolean isBetweenWorkTime() {
        List<LocalTime> workTimes = getUser().convertWorkTimeToLocalTime();
        LocalTime startTime = getStartDate().toLocalTime();

        return (startTime.isAfter(workTimes.get(0)) || startTime.equals(workTimes.get(0))) &&
                (startTime.isBefore(workTimes.get(1)) || startTime.equals(workTimes.get(1)));
    }
}
