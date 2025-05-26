package com.lshdainty.myhr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

    /**
     * 스케줄 생성자
     * 최초 스케줄 생성시 사용하는 생성자
     * Setter를 사용하지 말고 해당 생성자를 통해 생성 및 사용할 것
     */
    public static Schedule createSchedule(User user, String desc, ScheduleType type, LocalDateTime startDate, LocalDateTime endDate, Long userNo, String clientIP) {
        Schedule schedule = new Schedule();
        schedule.user = user;
        schedule.desc = desc;
        schedule.type = type;
        schedule.startDate = startDate;
        schedule.endDate = endDate;
        schedule.delYN = "N";
        schedule.setCreated(LocalDateTime.now(), userNo, clientIP);
        return schedule;
    }

    /**
     * 스케줄 삭제
     * Setter를 사용하지 말고 해당 메소드를 통해 설정할 것
     */
    public void deleteSchedule(Long userNo, String clientIP) {
        this.delYN = "Y";
        this.setmodified(LocalDateTime.now(), userNo, clientIP);
    }
}
