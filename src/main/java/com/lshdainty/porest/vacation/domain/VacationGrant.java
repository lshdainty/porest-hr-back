package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.type.ApprovalStatus;
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
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_grant_id", columnDefinition = "bigint(20) COMMENT '휴가 부여 아이디'")
    private Long id;

    /**
     * 사용자 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저에게 부여했는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;

    /**
     * 휴가 정책 객체<br>
     * 테이블 컬럼은 vacation_policy_id
     * 어떤 휴가 정책에 의해 부여 받았는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_policy_id", nullable = false)
    private VacationPolicy policy;

    /**
     * 휴가 부여 사유<br>
     * 휴가 부여 사유를 작성, 관리하는 컬럼
     */
    @Column(name = "vacation_grant_desc", length = 1000, columnDefinition = "varchar(1000) COMMENT '휴가 부여 사유'")
    private String desc;

    /**
     * 휴가 타입<br>
     * 휴가 타입으로 그룹핑하여 휴가 사용일수 관리함
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "vacation_type", nullable = false, length = 15, columnDefinition = "varchar(15) NOT NULL COMMENT '휴가 타입'")
    private VacationType type;

    /**
     * 휴가 사용 가능시간 시작 일시<br>
     * 사용자가 휴가 정책을 통해 휴가를 부여받아
     * 사용할 수 있는 기간의 시작 일자를 의미<br>
     * 시스템에서 추가하는 createAt하고 grantDate는 다름
     */
    @Column(name = "grant_date", columnDefinition = "datetime(6) COMMENT '휴가 사용 가능시간 시작 일시'")
    private LocalDateTime grantDate;

    /**
     * 휴가 사용 가능시간 종료 일시<br>
     * 사용자가 휴가 정책을 통해 휴가를 부여받아
     * 사용할 수 있는 기간의 만료 일자를 의미
     */
    @Column(name = "expiry_date", columnDefinition = "datetime(6) COMMENT '휴가 사용 가능시간 종료 일시'")
    private LocalDateTime expiryDate;

    /**
     * 휴가 부여 시간<br>
     * 휴가 정책에 따른 휴가 부여 내역<br>
     * (grantTime만 있다면 휴가 부여 내역으로 간주)
     */
    @Column(name = "grant_time", precision = 7, scale = 4, columnDefinition = "decimal(7,4) COMMENT '휴가 부여 시간'")
    private BigDecimal grantTime;

    /**
     * 휴가 잔여 시간<br>
     * 사용자가 부여받은 휴가 중에서 만료 기간이 짧은 것을 기준으로<br>
     * 부여받은 시간에서 사용한 시간만큼 차감하여 남아있는 시간을 기록
     */
    @Column(name = "remain_time", precision = 7, scale = 4, columnDefinition = "decimal(7,4) COMMENT '휴가 잔여 시간'")
    private BigDecimal remainTime;

    /**
     * 휴가 부여 상태<br>
     * 부여한 휴가의 현재 상태를 의미함. is_deleted와는 의미가 다름<br>
     * 사용 기간이 지난 휴가 등 상태가 다양하기 때문에 해당 컬럼을 사용하고<br>
     * 완전히 잘못된 데이터의 경우만 is_deleted로 소프트 삭제 처리함
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_status", nullable = false, length = 15, columnDefinition = "varchar(15) NOT NULL COMMENT '휴가 부여 상태'")
    private GrantStatus status;

    /**
     * 요청 대상 일자 시작 일시<br>
     * 사용자가 신청시 부여 타입으로 휴가를 신청할 때 시작 일시<br>
     * - OT: OT 시작 시간 (예: 2025-09-14 18:00)<br>
     * - 결혼/출산: 해당 일자 (예: 2025-09-14 00:00)<br>
     * 모든 신청 타입에서 필수로 사용됨
     */
    @Column(name = "request_start_time", columnDefinition = "datetime(6) COMMENT '요청 대상 일자 시작 일시'")
    private LocalDateTime requestStartTime;

    /**
     * 요청 대상 일자 종료 일시<br>
     * OT일 경우에만 값이 들어간다.<br>
     * OT 종료 시간 (예: 2025-09-14 19:00)<br>
     * 결혼/출산 등 OT가 아닌 경우는 null
     */
    @Column(name = "request_end_time", columnDefinition = "datetime(6) COMMENT '요청 대상 일자 종료 일시'")
    private LocalDateTime requestEndTime;

    /**
     * 요청 사유<br>
     * 신청 시 추가 타입으로 휴가를 신청할 때 휴가 신청 사유를 작성하는데 해당 컬럼에 값이 들어감
     */
    @Column(name = "request_desc", length = 1000, columnDefinition = "varchar(1000) COMMENT '요청 사유'")
    private String requestDesc;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '삭제 여부'")
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
     * @param desc 휴가 부여 사유
     * @param type 휴가 타입
     * @param grantTime 부여 시간
     * @param requestStartTime 신청 시작 일시 (OT/결혼/출산 등)
     * @param requestEndTime 신청 종료 일시 (OT일 경우에만 사용, 나머지는 null)
     * @param requestDesc 휴가 신청 상세 사유
     * @return VacationGrant
     */
    public static VacationGrant createPendingVacationGrant(User user, VacationPolicy policy, String desc, VacationType type, BigDecimal grantTime, LocalDateTime requestStartTime, LocalDateTime requestEndTime, String requestDesc) {
        VacationGrant vg = new VacationGrant();
        vg.addUser(user);
        vg.addPolicy(policy);
        vg.desc = desc;
        vg.requestStartTime = requestStartTime;
        vg.requestEndTime = requestEndTime;
        vg.requestDesc = requestDesc;
        vg.type = type;
        vg.grantTime = grantTime;
        vg.remainTime = grantTime;
        vg.status = GrantStatus.PENDING;
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
     * 승인 진행 중 상태로 변경<br>
     * 승인자가 2명 이상이고 1명 이상이 승인했을 때 PROGRESS 상태로 전환
     */
    public void updateToProgress() {
        if (this.status != GrantStatus.PENDING && this.status != GrantStatus.PROGRESS) {
            throw new IllegalStateException("대기 또는 진행 상태가 아닌 휴가는 진행 상태로 변경할 수 없습니다.");
        }
        this.status = GrantStatus.PROGRESS;
    }

    /**
     * 승인 완료 처리<br>
     * 모든 승인자가 승인하면 ACTIVE 상태로 전환하고 유효기간을 설정
     *
     * @param grantDate 휴가 시작일
     * @param expiryDate 휴가 만료일
     */
    public void approve(LocalDateTime grantDate, LocalDateTime expiryDate) {
        if (this.status != GrantStatus.PENDING && this.status != GrantStatus.PROGRESS) {
            throw new IllegalStateException("대기 또는 진행 상태가 아닌 휴가는 승인할 수 없습니다.");
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
        if (this.status != GrantStatus.PENDING && this.status != GrantStatus.PROGRESS) {
            throw new IllegalStateException("대기 또는 진행 상태가 아닌 휴가는 거부할 수 없습니다.");
        }
        this.status = GrantStatus.REJECTED;
    }

    /**
     * 휴가 신청 취소 처리<br>
     * 신청자가 직접 휴가 신청을 취소하면 CANCELED 상태로 전환<br>
     * 한 명도 승인하지 않은 PENDING 상태에서만 취소 가능
     */
    public void cancel() {
        if (this.status != GrantStatus.PENDING) {
            throw new IllegalStateException("대기 상태가 아닌 휴가는 취소할 수 없습니다.");
        }
        this.status = GrantStatus.CANCELED;
    }



    /**
     * 현재 승인 대기 중인 승인자 조회<br>
     * 승인 순서가 가장 빠른(approvalOrder가 가장 작은) PENDING 상태의 승인자를 반환
     *
     * @return 현재 승인 대기 중인 승인자 (없으면 null)
     */
    public User getCurrentPendingApprover() {
        return vacationApprovals.stream()
                .filter(approval -> approval.getApprovalStatus() == ApprovalStatus.PENDING)
                .min((a1, a2) -> Integer.compare(a1.getApprovalOrder(), a2.getApprovalOrder()))
                .map(VacationApproval::getApprover)
                .orElse(null);
    }
}
