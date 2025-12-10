package com.lshdainty.porest.vacation.service.dto;

import com.lshdainty.porest.vacation.type.ApprovalStatus;
import com.lshdainty.porest.vacation.type.VacationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class VacationApprovalServiceDto {
    // 승인 ID
    private Long id;
    // 휴가 부여 ID
    private Long vacationGrantId;
    // 신청자 ID
    private String requesterId;
    // 신청자 이름
    private String requesterName;
    // 휴가 정책 ID
    private Long policyId;
    // 휴가 정책 이름
    private String policyName;
    // 휴가 사유 (신청 사유)
    private String desc;
    // 신청 시작 일시 (OT 시작 시간, 결혼/출산 일자 등)
    private LocalDateTime requestStartTime;
    // 신청 종료 일시 (OT 종료 시간, OT가 아니면 null)
    private LocalDateTime requestEndTime;
    // 부여 시간
    private BigDecimal grantTime;
    // 휴가 타입
    private VacationType vacationType;
    // 승인 상태
    private ApprovalStatus approvalStatus;
    // 승인일시
    private LocalDateTime approvalDate;
    // 거부 사유
    private String rejectionReason;
    // 승인 순서
    private Integer approvalOrder;
    // 승인자 ID
    private String approverId;
    // 승인자 이름
    private String approverName;

    @Override
    public String toString() {
        return "VacationApprovalServiceDto{" +
                "id=" + id +
                ", vacationGrantId=" + vacationGrantId +
                ", requesterId='" + requesterId + '\'' +
                ", requesterName='" + requesterName + '\'' +
                ", policyId=" + policyId +
                ", policyName='" + policyName + '\'' +
                ", desc='" + desc + '\'' +
                ", requestStartTime=" + requestStartTime +
                ", requestEndTime=" + requestEndTime +
                ", grantTime=" + grantTime +
                ", vacationType=" + vacationType +
                ", approvalStatus=" + approvalStatus +
                ", approvalDate=" + approvalDate +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}
