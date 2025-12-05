package com.lshdainty.porest.schedule.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.schedule.type.ScheduleType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    /**
     * 스케줄 아이디<br>
     * 테이블 관리용 seq
     */
    @Id
    @GeneratedValue
    @Column(name = "schedule_id")
    private Long id;

    /**
     * 유저 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저의 스케줄인지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 스케줄 타입<br>
     * 스케줄의 종류를 구분하기 위한 타입
     */
    @Column(name = "schedule_type")
    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    /**
     * 스케줄 설명<br>
     * 스케줄에 대한 상세 내용 및 설명
     */
    @Column(name = "schedule_desc")
    private String desc;

    /**
     * 스케줄 시작 일시<br>
     * 스케줄이 시작되는 날짜와 시간
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * 스케줄 종료 일시<br>
     * 스케줄이 종료되는 날짜와 시간
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Column(name = "is_deleted")
    @Enumerated(EnumType.STRING)
    private YNType isDeleted;

    /**
     * 스케줄 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 스케줄 생성할 것
     *
     * @return Schedule
     */
    public static Schedule createSchedule(User user, String desc, ScheduleType type, LocalDateTime startDate, LocalDateTime endDate) {
        Schedule schedule = new Schedule();
        schedule.user = user;
        schedule.desc = desc;
        schedule.type = type;
        schedule.startDate = startDate;
        schedule.endDate = endDate;
        schedule.isDeleted = YNType.N;
        return schedule;
    }

    /**
     * 스케줄 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 스케줄 삭제할 것
     */
    public void deleteSchedule() {
        this.isDeleted = YNType.Y;
    }
}
