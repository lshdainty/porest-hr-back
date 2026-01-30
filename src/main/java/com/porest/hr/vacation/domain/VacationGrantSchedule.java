package com.porest.hr.vacation.domain;

import com.porest.hr.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.porest.hr.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VacationGrantSchedule Entity<br>
 * 휴가 자동 부여 스케줄 정보를 관리하는 엔티티<br>
 * REPEAT_GRANT 정책의 부여 이력 및 다음 부여 예정일 추적<br>
 * (User, VacationPolicy) 조합당 하나만 존재
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vacation_grant_schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vacation_grant_schedule_user_policy",
                columnNames = {"user_no", "vacation_policy_id"}
        ))
public class VacationGrantSchedule extends AuditingFields {
    /**
     * 스케줄 아이디<br>
     * 자동 생성되는 고유 식별자
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_grant_schedule_id")
    private Long id;

    /**
     * 사용자<br>
     * 휴가가 부여될 대상 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private User user;

    /**
     * 휴가 정책<br>
     * 자동 부여될 휴가 정책
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_policy_id", nullable = false)
    private VacationPolicy vacationPolicy;

    /**
     * 마지막 휴가 부여 일시<br>
     * 스케줄러 실행으로 휴가를 부여한 마지막 시점<br>
     * 중복 부여 방지를 위한 컬럼
     */
    @Column(name = "last_granted_at")
    private LocalDateTime lastGrantedAt;

    /**
     * 다음 휴가 부여 일자<br>
     * 스케줄러 조회 최적화용 컬럼<br>
     * 인덱스 추가 예정
     */
    @Column(name = "next_grant_date")
    private LocalDate nextGrantDate;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 스케줄 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 스케줄 생성할 것
     *
     * @param user 사용자
     * @param vacationPolicy 휴가 정책
     * @return VacationGrantSchedule
     */
    public static VacationGrantSchedule createSchedule(User user, VacationPolicy vacationPolicy) {
        VacationGrantSchedule schedule = new VacationGrantSchedule();
        schedule.user = user;
        schedule.vacationPolicy = vacationPolicy;
        schedule.lastGrantedAt = null;
        schedule.nextGrantDate = null;
        schedule.isDeleted = YNType.N;
        return schedule;
    }

    /**
     * 스케줄 생성 함수 (다음 부여일 포함)<br>
     * 최초 부여 예정일을 지정하여 생성
     *
     * @param user 사용자
     * @param vacationPolicy 휴가 정책
     * @param nextGrantDate 다음 부여 예정일
     * @return VacationGrantSchedule
     */
    public static VacationGrantSchedule createScheduleWithNextDate(User user, VacationPolicy vacationPolicy, LocalDate nextGrantDate) {
        VacationGrantSchedule schedule = new VacationGrantSchedule();
        schedule.user = user;
        schedule.vacationPolicy = vacationPolicy;
        schedule.lastGrantedAt = null;
        schedule.nextGrantDate = nextGrantDate;
        schedule.isDeleted = YNType.N;
        return schedule;
    }

    /**
     * 스케줄 삭제 함수 (Soft Delete)<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 스케줄 삭제할 것
     */
    public void deleteSchedule() {
        this.isDeleted = YNType.Y;
    }

    /**
     * 스케줄 복구 함수 (Soft Delete 복구)<br>
     * Soft Delete된 스케줄을 다시 활성화
     */
    public void restoreSchedule() {
        this.isDeleted = YNType.N;
    }

    /**
     * 휴가 부여 후 부여 이력 업데이트<br>
     * 스케줄러에서 휴가 부여 후 마지막 부여 시점과 다음 부여 예정일을 갱신할 때 사용
     *
     * @param lastGrantedAt 마지막 부여 시점
     * @param nextGrantDate 다음 부여 예정일
     */
    public void updateGrantHistory(LocalDateTime lastGrantedAt, LocalDate nextGrantDate) {
        this.lastGrantedAt = lastGrantedAt;
        this.nextGrantDate = nextGrantDate;
    }

    /**
     * 다음 부여 예정일만 갱신<br>
     * 스케줄러에서 휴가 부여 대상이 아닌 경우 다음 부여 예정일만 갱신할 때 사용
     *
     * @param nextGrantDate 다음 부여 예정일
     */
    public void updateNextGrantDate(LocalDate nextGrantDate) {
        this.nextGrantDate = nextGrantDate;
    }
}
