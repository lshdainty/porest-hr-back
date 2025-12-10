package com.lshdainty.porest.vacation.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.type.ApprovalStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vacation_approval")
public class VacationApproval extends AuditingFields {
    /**
     * 휴가 승인 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_approval_id")
    private Long id;

    /**
     * 휴가 부여 객체<br>
     * 테이블 컬럼은 vacation_grant_id<br>
     * 어떤 휴가 부여에 대한 승인인지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_grant_id", nullable = false)
    @Setter
    private VacationGrant vacationGrant;

    /**
     * 승인자 유저 객체<br>
     * 테이블 컬럼은 approver_id<br>
     * 누가 승인하는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User approver;

    /**
     * 승인 순서<br>
     * 레벨 기반 순차 승인을 위한 순서 (1부터 시작, 작을수록 먼저 승인)
     */
    @Column(name = "approval_order", nullable = false)
    private Integer approvalOrder;

    /**
     * 승인 상태<br>
     * 해당 승인자의 승인 상태를 의미함
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 15)
    private ApprovalStatus approvalStatus;

    /**
     * 승인일시<br>
     * 승인자가 승인 또는 거부한 일시
     */
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    /**
     * 거부 사유<br>
     * 승인자가 거부했을 경우 거부 사유
     */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    /**
     * 삭제 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    // vacationGrant 추가 연관관계 편의 메소드
    public void addVacationGrant(VacationGrant vacationGrant) {
        this.vacationGrant = vacationGrant;
        vacationGrant.getVacationApprovals().add(this);
    }

    // approver 추가 연관관계 편의 메소드
    public void addApprover(User approver) {
        this.approver = approver;
        approver.getVacationApprovals().add(this);
    }

    /**
     * 휴가 승인 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 휴가 승인을 생성할 것
     *
     * @param vacationGrant 휴가 부여 객체
     * @param approver 승인자
     * @param approvalOrder 승인 순서
     * @return VacationApproval
     */
    public static VacationApproval createVacationApproval(VacationGrant vacationGrant, User approver, Integer approvalOrder) {
        VacationApproval va = new VacationApproval();
        va.addVacationGrant(vacationGrant);
        va.addApprover(approver);
        va.approvalOrder = approvalOrder;
        va.approvalStatus = ApprovalStatus.PENDING;
        va.isDeleted = YNType.N;
        return va;
    }

    /**
     * 승인 처리
     */
    public void approve() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 승인입니다.");
        }
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvalDate = LocalDateTime.now();
    }

    /**
     * 거부 처리
     *
     * @param rejectionReason 거부 사유
     */
    public void reject(String rejectionReason) {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 승인입니다.");
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    /**
     * 대기 중인지 확인
     */
    public boolean isPending() {
        return this.approvalStatus == ApprovalStatus.PENDING;
    }

    /**
     * 승인되었는지 확인
     */
    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }

    /**
     * 거부되었는지 확인
     */
    public boolean isRejected() {
        return this.approvalStatus == ApprovalStatus.REJECTED;
    }
}
