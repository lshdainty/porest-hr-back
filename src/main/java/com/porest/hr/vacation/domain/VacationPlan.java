package com.porest.hr.vacation.domain;

import com.porest.hr.common.domain.AuditingFieldsWithIp;
import com.porest.core.type.YNType;
import com.porest.hr.vacation.type.GrantMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * VacationPlan Entity<br>
 * 휴가 플랜 정보를 관리하는 엔티티<br>
 * 여러 개의 VacationPolicy를 묶어서 관리<br>
 * User에게 Plan 단위로 휴가 정책을 부여
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vacation_plan")
public class VacationPlan extends AuditingFieldsWithIp {
    /**
     * 휴가 플랜 행 아이디<br>
     * 자동 생성되는 고유 식별자
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long rowId;

    /**
     * 플랜 코드<br>
     * 예: FULL_TIME, CONTRACT, INTERN
     */
    @Column(name = "plan_code", unique = true, nullable = false, length = 30)
    private String code;

    /**
     * 플랜 이름<br>
     * 예: 정규직 플랜, 계약직 플랜
     */
    @Column(name = "plan_name", nullable = false, length = 50)
    private String name;

    /**
     * 플랜 설명<br>
     * 플랜에 대한 상세 설명
     */
    @Column(name = "plan_desc", length = 1000)
    private String desc;

    /**
     * 플랜-정책 매핑 목록<br>
     * 해당 플랜이 가진 정책 매핑 리스트<br>
     * 중간 엔티티를 통해 생성/수정 이력 추적 가능
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "vacationPlan", cascade = CascadeType.ALL)
    private List<VacationPlanPolicy> vacationPlanPolicies = new ArrayList<>();

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 휴가 플랜 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 플랜 생성할 것
     *
     * @param code 플랜 코드
     * @param name 플랜 이름 (한글명)
     * @param desc 플랜 설명
     * @return VacationPlan
     */
    public static VacationPlan createPlan(String code, String name, String desc) {
        VacationPlan plan = new VacationPlan();
        plan.code = code;
        plan.name = name;
        plan.desc = desc;
        plan.vacationPlanPolicies = new ArrayList<>();
        plan.isDeleted = YNType.N;
        return plan;
    }

    /**
     * 휴가 플랜 생성 함수 (정책 포함)<br>
     * 정책 리스트와 함께 플랜을 생성
     *
     * @param code 플랜 코드
     * @param name 플랜 이름 (한글명)
     * @param desc 플랜 설명
     * @param policies 정책 리스트
     * @return VacationPlan
     */
    public static VacationPlan createPlanWithPolicies(String code, String name, String desc, List<VacationPolicy> policies) {
        VacationPlan plan = new VacationPlan();
        plan.code = code;
        plan.name = name;
        plan.desc = desc;
        plan.vacationPlanPolicies = new ArrayList<>();
        plan.isDeleted = YNType.N;

        if (policies != null) {
            int sortOrder = 1;
            for (VacationPolicy policy : policies) {
                VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, sortOrder++, YNType.N);
                plan.vacationPlanPolicies.add(planPolicy);
            }
        }
        return plan;
    }

    /**
     * 휴가 플랜 수정 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 플랜 수정할 것
     *
     * @param name 플랜 이름 (한글명)
     * @param desc 플랜 설명
     */
    public void updatePlan(String name, String desc) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(desc)) { this.desc = desc; }
    }

    /**
     * 휴가 플랜 삭제 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 플랜 삭제할 것 (Soft Delete)
     */
    public void deletePlan() {
        this.isDeleted = YNType.Y;
    }

    /* 비즈니스 편의 메소드 */

    /**
     * 정책 목록 조회<br>
     * VacationPlanPolicy에서 VacationPolicy만 추출하여 반환
     *
     * @return 정책 리스트
     */
    public List<VacationPolicy> getPolicies() {
        return this.vacationPlanPolicies.stream()
                .filter(pp -> YNType.isN(pp.getIsDeleted()))
                .map(VacationPlanPolicy::getVacationPolicy)
                .toList();
    }

    /**
     * REPEAT_GRANT 정책 목록 조회<br>
     * 반복 부여 방식의 정책만 추출하여 반환
     *
     * @return REPEAT_GRANT 정책 리스트
     */
    public List<VacationPolicy> getRepeatGrantPolicies() {
        return this.vacationPlanPolicies.stream()
                .filter(pp -> YNType.isN(pp.getIsDeleted()))
                .map(VacationPlanPolicy::getVacationPolicy)
                .filter(p -> p.getGrantMethod() == GrantMethod.REPEAT_GRANT)
                .toList();
    }

    /**
     * 정책 추가<br>
     * 플랜에 새로운 정책을 추가
     *
     * @param policy 추가할 정책
     */
    public void addPolicy(VacationPolicy policy) {
        boolean exists = this.vacationPlanPolicies.stream()
                .anyMatch(pp -> pp.getVacationPolicy().getRowId().equals(policy.getRowId())
                        && YNType.isN(pp.getIsDeleted()));

        if (!exists) {
            int maxSortOrder = this.vacationPlanPolicies.stream()
                    .filter(pp -> YNType.isN(pp.getIsDeleted()))
                    .mapToInt(VacationPlanPolicy::getSortOrder)
                    .max()
                    .orElse(0);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(this, policy, maxSortOrder + 1, YNType.N);
            this.vacationPlanPolicies.add(planPolicy);
        }
    }

    /**
     * 정책 제거<br>
     * 플랜에서 특정 정책을 제거 (Soft Delete)
     *
     * @param policy 제거할 정책
     */
    public void removePolicy(VacationPolicy policy) {
        this.vacationPlanPolicies.stream()
                .filter(pp -> pp.getVacationPolicy().getRowId().equals(policy.getRowId())
                        && YNType.isN(pp.getIsDeleted()))
                .forEach(VacationPlanPolicy::deletePlanPolicy);
    }

    /**
     * 모든 정책 제거<br>
     * 플랜의 모든 정책을 제거
     */
    public void clearPolicies() {
        this.vacationPlanPolicies.forEach(VacationPlanPolicy::deletePlanPolicy);
    }

    /**
     * 특정 정책 보유 여부 확인<br>
     * 플랜이 특정 정책을 가지고 있는지 확인
     *
     * @param policyId 확인할 정책 ID
     * @return 정책 보유 여부
     */
    public boolean hasPolicy(Long policyId) {
        return this.vacationPlanPolicies.stream()
                .filter(pp -> YNType.isN(pp.getIsDeleted()))
                .anyMatch(pp -> pp.getVacationPolicy().getRowId().equals(policyId));
    }
}
