package com.lshdainty.porest.schedule.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.schedule.type.ScheduleType;
import com.lshdainty.porest.user.domain.User;
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
    /**
     * 스케줄 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id", columnDefinition = "bigint(20) COMMENT '스케줄 아이디'")
    private Long id;

    /**
     * 사용자 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저의 스케줄인지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 스케줄 타입<br>
     * 스케줄의 종류를 구분하기 위한 타입
     */
    @Column(name = "schedule_type", nullable = false, length = 15, columnDefinition = "varchar(15) NOT NULL COMMENT '스케줄 타입'")
    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    /**
     * 스케줄 설명<br>
     * 스케줄에 대한 상세 내용 및 설명
     */
    @Column(name = "schedule_desc", length = 1000, columnDefinition = "varchar(1000) COMMENT '스케줄 설명'")
    private String desc;

    /**
     * 시작 일자<br>
     * 스케줄이 시작되는 날짜와 시간
     */
    @Column(name = "start_date", columnDefinition = "datetime(6) COMMENT '시작 일자'")
    private LocalDateTime startDate;

    /**
     * 종료 일자<br>
     * 스케줄이 종료되는 날짜와 시간
     */
    @Column(name = "end_date", columnDefinition = "datetime(6) COMMENT '종료 일자'")
    private LocalDateTime endDate;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '삭제 여부'")
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
