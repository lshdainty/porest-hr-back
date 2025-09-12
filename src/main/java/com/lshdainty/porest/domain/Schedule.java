package com.lshdainty.porest.domain;

import com.lshdainty.porest.type.ScheduleType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Entity
@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "schedule")
public class Schedule extends AuditingFields {
    @Id
    @GeneratedValue
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
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
     * 스케줄 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 스케줄 생성할 것
     *
     * @return Schedule
     */
    public static Schedule createSchedule(User user, String desc, ScheduleType type, LocalDateTime startDate, LocalDateTime endDate, String crtUserId, String clientIP) {
        Schedule schedule = new Schedule();
        schedule.user = user;
        schedule.desc = desc;
        schedule.type = type;
        schedule.startDate = startDate;
        schedule.endDate = endDate;
        schedule.delYN = "N";
        schedule.setCreated(LocalDateTime.now(), crtUserId, clientIP);
        return schedule;
    }

    /**
     * 스케줄 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 스케줄 삭제할 것
     */
    public void deleteSchedule(String mdfUserId, String clientIP) {
        this.delYN = "Y";
        this.setModified(LocalDateTime.now(), mdfUserId, clientIP);
    }
}
