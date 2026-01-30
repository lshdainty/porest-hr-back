package com.porest.hr.vacation.service.dto;

import com.lshdainty.porest.common.type.YNType;
import com.porest.hr.vacation.domain.VacationPlan;
import com.porest.hr.vacation.domain.VacationPlanPolicy;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class VacationPlanServiceDto {
    private Long id;
    private String code;
    private String name;
    private String desc;
    private YNType isDeleted;
    private List<VacationPolicyServiceDto> policies;
    private List<Long> policyIds;

    /**
     * VacationPlan 엔티티를 ServiceDto로 변환
     */
    public static VacationPlanServiceDto from(VacationPlan plan) {
        return VacationPlanServiceDto.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .desc(plan.getDesc())
                .isDeleted(plan.getIsDeleted())
                .build();
    }

    /**
     * VacationPlan 엔티티와 정책 목록을 포함하여 ServiceDto로 변환
     */
    public static VacationPlanServiceDto fromWithPolicies(VacationPlan plan) {
        List<VacationPolicyServiceDto> policyDtos = plan.getVacationPlanPolicies().stream()
                .filter(vpp -> YNType.isN(vpp.getIsDeleted()))
                .sorted((a, b) -> {
                    Integer orderA = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    Integer orderB = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return orderA.compareTo(orderB);
                })
                .map(vpp -> VacationPolicyServiceDto.builder()
                        .id(vpp.getVacationPolicy().getId())
                        .name(vpp.getVacationPolicy().getName())
                        .desc(vpp.getVacationPolicy().getDesc())
                        .vacationType(vpp.getVacationPolicy().getVacationType())
                        .grantMethod(vpp.getVacationPolicy().getGrantMethod())
                        .grantTime(vpp.getVacationPolicy().getGrantTime())
                        .isFlexibleGrant(vpp.getVacationPolicy().getIsFlexibleGrant())
                        .minuteGrantYn(vpp.getVacationPolicy().getMinuteGrantYn())
                        .repeatUnit(vpp.getVacationPolicy().getRepeatUnit())
                        .repeatInterval(vpp.getVacationPolicy().getRepeatInterval())
                        .specificMonths(vpp.getVacationPolicy().getSpecificMonths())
                        .specificDays(vpp.getVacationPolicy().getSpecificDays())
                        .effectiveType(vpp.getVacationPolicy().getEffectiveType())
                        .expirationType(vpp.getVacationPolicy().getExpirationType())
                        .build())
                .toList();

        return VacationPlanServiceDto.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .desc(plan.getDesc())
                .isDeleted(plan.getIsDeleted())
                .policies(policyDtos)
                .build();
    }
}
