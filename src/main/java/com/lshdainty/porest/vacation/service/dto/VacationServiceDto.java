package com.lshdainty.porest.vacation.service.dto;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import com.lshdainty.porest.vacation.type.VacationTimeType;
import com.lshdainty.porest.vacation.type.VacationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
public class VacationServiceDto {
    // 휴가 아이디
    private Long id;
    // 사용자 아이디
    private String userId;
    // 휴가 사유
    private String desc;
    // 휴가 타입
    private VacationType type;
    // 휴가 잔여 시간
    private BigDecimal remainTime;
    // 휴가 발생 시간
    private LocalDateTime occurDate;
    // 휴가 소멸 시간
    private LocalDateTime expiryDate;
    // 휴가 이력 삭제 여부
    private String isDeleted;

    // 휴가를 사용한 유저 객체 정보
    private User user;

    // 휴가에 속한 히스토리 아이디 리스트
    private List<Long> historyIds;
    // 휴가에 속한 히스토리 아이디
    private Long historyId;

    // 휴가 부여 시간
    private BigDecimal grantTime;
    // 휴가 부여 시작일 (관리자 부여용)
    private LocalDateTime grantDate;
    // 휴가 정책 ID (관리자 부여용)
    private Long policyId;

    // 휴가 사용 시작 시간
    private LocalDateTime startDate;
    // 휴가 사용 종료 시간
    private LocalDateTime endDate;
    // 휴가 시간 타입
    private VacationTimeType timeType;

    // 월별 통계 월
    private int month;
    // 휴가 사용 시간
    private BigDecimal usedTime;

    // 휴가 사용 예정 시간
    private BigDecimal expectUsedTime;
    // 이전달 휴가 잔여 시간
    private BigDecimal prevRemainTime;
    // 이전달 휴가 사용 시간
    private BigDecimal prevUsedTime;
    // 이전달 휴가 사용 예정 시간
    private BigDecimal prevExpectUsedTime;

    // 부여받은 휴가 내역 리스트
    private List<VacationGrant> grants;
    // 사용한 휴가 내역 리스트
    private List<VacationUsage> usages;

    // 휴가 신청 사유 (ON_REQUEST 방식)
    private String requestReason;
    // 승인자 ID 리스트 (ON_REQUEST 방식)
    private List<String> approverIds;

    @Override
    public String toString() {
        return "VacationServiceDto{" +
                "id: " + id +
                ", desc: '" + desc + '\'' +
                ", type: " + type +
                ", remainTime: " + remainTime +
                ", occurDate: " + occurDate +
                ", expiryDate: " + expiryDate +
                ", historyIds: " + historyIds +
                ", grantTime: " + grantTime +
                ", startDate: " + startDate +
                ", endDate: " + endDate +
                ", timeType: " + timeType +
                ", isDeleted: '" + isDeleted + '\'' +
                ", grants: " + grants +
                ", usages: " + usages +
                '}';
    }
}
