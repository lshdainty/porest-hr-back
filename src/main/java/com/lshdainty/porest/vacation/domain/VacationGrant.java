package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.type.GrantStatus;
import com.lshdainty.porest.vacation.type.VacationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "vacation_grant")
public class VacationGrant extends AuditingFields {
    /**
     * 휴가 부여 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue
    @Column(name = "vacation_grant_id")
    private Long id;

    /**
     * 유저 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저에게 부여했는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    /**
     * 휴가 정책 객체<br>
     * 테이블 컬럼은 vacation_policy_id
     * 어떤 휴가 정책에 의해 부여 받았는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_policy_id")
    private VacationPolicy policy;

    /**
     * 휴가 부여 사유 설명<br>
     * 휴가 부여 사유를 작성, 관리하는 컬럼
     */
    @Column(name = "vacation_grant_desc")
    private String desc;

    /**
     * 휴가 타입<br>
     * 휴가 타입으로 그룹핑하여 휴가 사용일수 관리함
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_type")
    private VacationType type;

    /**
     * 휴가 사용 가능기간 시작 일자<br>
     * 사용자가 휴가 정책을 통해 휴가를 부여받아
     * 사용할 수 있는 기간의 시작 일자를 의미<br>
     * 시스템에서 추가하는 createAt하고 grantDate는 다름
     */
    @Column(name = "grant_date")
    private LocalDateTime grantDate;

    /**
     * 휴가 사용 가능기간 만료 일자<br>
     * 사용자가 휴가 정책을 통해 휴가를 부여받아
     * 사용할 수 있는 기간의 만료 일자를 의미
     */
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    /**
     * 휴가 부여 시간<br>
     * 휴가 정책에 따른 휴가 부여 내역<br>
     * (grantTime만 있다면 휴가 부여 내역으로 간주)
     */
    @Column(name = "grant_time", precision = 7, scale = 4)
    private BigDecimal grantTime;

    /**
     * 휴가 잔여 시간<br>
     * 사용자가 부여받은 휴가 중에서 만료 기간이 짧은 것을 기준으로<br>
     * 부여받은 시간에서 사용한 시간만큼 차감하여 남아있는 시간을 기록
     */
    @Column(name = "remain_time", precision = 7, scale = 4)
    private BigDecimal remainTime;

    /**
     * 휴가 부여 상태<br>
     * 부여한 휴가의 현재 상태를 의미함. is_deleted와는 의미가 다름<br>
     * 사용 기간이 지난 휴가 등 상태가 다양하기 때문에 해당 컬럼을 사용하고<br>
     * 완전히 잘못된 데이터의 경우만 is_deleted로 소프트 삭제 처리함
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_status")
    private GrantStatus status;

    /**
     * 신청일시<br>
     * ON_REQUEST 방식으로 사용자가 휴가를 신청한 일시
     */
    @Column(name = "request_date")
    private LocalDateTime requestDate;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "vacationGrant", cascade = CascadeType.ALL)
    private List<VacationApproval> vacationApprovals = new ArrayList<>();

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getVacationGrants().add(this);
    }

    // policy 추가 연관관계 편의 메소드
    public void addPolicy(VacationPolicy policy) {
        this.policy = policy;
        policy.getVacationGrants().add(this);
    }

    /**
     * 휴가 부여 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 부여할 것
     *
     * @return VacationGrant
     */
    public static VacationGrant createVacationGrant(User user, VacationPolicy policy, String desc, VacationType type, BigDecimal grantTime, LocalDateTime grantDate, LocalDateTime expiryDate) {
        VacationGrant vg = new VacationGrant();
        vg.addUser(user);
        vg.addPolicy(policy);
        vg.desc = desc;
        vg.type = type;
        vg.grantDate = grantDate;
        vg.expiryDate = expiryDate;
        vg.grantTime = grantTime;
        vg.remainTime = grantTime;
        vg.status = GrantStatus.ACTIVE;
        vg.isDeleted = YNType.N;
        return vg;
    }

    /**
     * 휴가 신청 함수 (승인 대기 상태)<br>
     * ON_REQUEST 방식으로 사용자가 휴가를 신청할 때 사용<br>
     * 승인이 완료되면 ACTIVE 상태로 변경됨
     *
     * @param user 사용자
     * @param policy 휴가 정책
     * @param desc 휴가 신청 사유
     * @param type 휴가 타입
     * @param grantTime 부여 시간
     * @return VacationGrant
     */
    public static VacationGrant createPendingVacationGrant(User user, VacationPolicy policy, String desc, VacationType type, BigDecimal grantTime) {
        VacationGrant vg = new VacationGrant();
        vg.addUser(user);
        vg.addPolicy(policy);
        vg.desc = desc;
        vg.requestDate = LocalDateTime.now();
        vg.type = type;
        vg.grantTime = grantTime;
        vg.remainTime = grantTime;
        vg.status = GrantStatus.PENDING_APPROVAL;
        vg.isDeleted = YNType.N;
        // grantDate와 expiryDate는 승인 완료 시점에 설정
        return vg;
    }

    /* 비즈니스 편의 메소드 */
    /**
     * 휴가 복원 메소드<br>
     * remainTime(잔여시간)에 grantTime(추가시간)을 더함
     */
    public void restore(BigDecimal grantTime) {
        this.remainTime = getRemainTime().add(grantTime);
        if (getStatus() == GrantStatus.EXHAUSTED) {
            this.status = GrantStatus.ACTIVE;
        }
    }

    /**
     * 휴가 차감 메소드<br>
     * remainTime(잔여시간)에서 deductTime을(사용시간)을 뺌
     */
    public void deduct(BigDecimal deductTime) {
        this.remainTime = getRemainTime().subtract(deductTime);

        if (getRemainTime().compareTo(BigDecimal.ZERO) == 0) {
            this.status = GrantStatus.EXHAUSTED;
        }
    }

    /**
     * 만료 확인
     */
    public boolean isExpired() {
        return getExpiryDate() != null &&
                LocalDate.now().isAfter(getExpiryDate().toLocalDate());
    }

    /**
     * 만료 처리<br>
     * 만료 처리시 remainTime은 0으로 처리하지 않는다.<br>
     * 전체 사용 안해도 남아있는 시간을 쉽게 확인하기 위해 0처리 안함
     */
    public void expire() {
        this.status = GrantStatus.EXPIRED;
    }

    /**
     * 회수 처리<br>
     * 휴가 정책 삭제 또는 유저에게서 정책 회수 시 호출<br>
     * remainTime은 유지하여 회수 전 상태를 추적 가능하도록 함
     */
    public void revoke() {
        this.status = GrantStatus.REVOKED;
    }

    /**
     * 사용 가능 여부
     */
    public boolean isAvailable() {
        return getStatus().equals(GrantStatus.ACTIVE) &&
                getRemainTime().compareTo(BigDecimal.ZERO) > 0 &&
                !isExpired() &&
                getIsDeleted().equals(YNType.N);
    }

    /**
     * 승인 완료 처리<br>
     * 모든 승인자가 승인하면 ACTIVE 상태로 전환하고 유효기간을 설정
     *
     * @param grantDate 휴가 시작일
     * @param expiryDate 휴가 만료일
     */
    public void approve(LocalDateTime grantDate, LocalDateTime expiryDate) {
        if (this.status != GrantStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("승인 대기 상태가 아닌 휴가는 승인할 수 없습니다.");
        }
        this.status = GrantStatus.ACTIVE;
        this.grantDate = grantDate;
        this.expiryDate = expiryDate;
    }

    /**
     * 승인 거부 처리<br>
     * 한 명이라도 거부하면 REJECTED 상태로 전환
     */
    public void reject() {
        if (this.status != GrantStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("승인 대기 상태가 아닌 휴가는 거부할 수 없습니다.");
        }
        this.status = GrantStatus.REJECTED;
    }
}
