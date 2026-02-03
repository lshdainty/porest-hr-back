package com.porest.hr.vacation.domain;

import com.porest.hr.common.domain.AuditingFieldsWithIp;
import com.porest.core.type.YNType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * VacationPlanPolicy Entity<br>
 * 휴가 플랜과 휴가 정책의 매핑 정보를 관리하는 중간 엔티티<br>
 * 누가 언제 정책을 추가/수정했는지 추적 가능
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vacation_plan_policy")
public class VacationPlanPolicy extends AuditingFieldsWithIp {
    /**
     * 플랜-정책 매핑 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_plan_policy_id")
    private Long id;

    /**
     * 휴가 플랜<br>
     * 어떤 플랜에 정책이 추가되었는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_plan_id", nullable = false)
    private VacationPlan vacationPlan;

    /**
     * 휴가 정책<br>
     * 어떤 정책이 추가되었는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacation_policy_id", nullable = false)
    private VacationPolicy vacationPolicy;

    /**
     * 정렬 순서<br>
     * 플랜 내에서 정책의 표시 순서
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /**
     * 기본값 여부<br>
     * 해당 플랜에서 기본으로 사용되는 정책인지 여부
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_default", nullable = false, length = 1)
    private YNType isDefault;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 플랜-정책 매핑 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 플랜-정책 매핑 생성할 것
     *
     * @param vacationPlan 휴가 플랜
     * @param vacationPolicy 휴가 정책
     * @param sortOrder 정렬 순서
     * @param isDefault 기본값 여부
     * @return VacationPlanPolicy
     */
    public static VacationPlanPolicy createPlanPolicy(VacationPlan vacationPlan, VacationPolicy vacationPolicy, Integer sortOrder, YNType isDefault) {
        VacationPlanPolicy planPolicy = new VacationPlanPolicy();
        planPolicy.vacationPlan = vacationPlan;
        planPolicy.vacationPolicy = vacationPolicy;
        planPolicy.sortOrder = sortOrder;
        planPolicy.isDefault = isDefault;
        planPolicy.isDeleted = YNType.N;
        return planPolicy;
    }

    /**
     * 플랜-정책 매핑 삭제 함수 (Soft Delete)<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 플랜-정책 매핑 삭제할 것
     */
    public void deletePlanPolicy() {
        this.isDeleted = YNType.Y;
    }

    /**
     * 정렬 순서 변경<br>
     * 플랜 내에서 정책의 표시 순서를 변경
     *
     * @param sortOrder 새로운 정렬 순서
     */
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 기본값 여부 변경<br>
     * 해당 정책을 기본값으로 설정하거나 해제
     *
     * @param isDefault 기본값 여부
     */
    public void updateIsDefault(YNType isDefault) {
        this.isDefault = isDefault;
    }
}
