package com.porest.hr.department.service.dto;

import com.lshdainty.porest.common.type.YNType;
import com.porest.hr.company.domain.Company;
import com.porest.hr.department.domain.Department;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Builder
public class DepartmentServiceDto {
    private Long id;
    private String name;
    private String nameKR;
    private Long parentId;
    private DepartmentServiceDto parent;
    private String headUserId;
    private Long level;
    private String desc;
    private String color;
    private Company company;
    private String companyId;
    private List<DepartmentServiceDto> children;

    private List<UserDepartmentServiceDto> usersInDepartment;
    private List<UserDepartmentServiceDto> usersNotInDepartment;

    /**
     * Department Entity -> DepartmentServiceDto 변환 (자식 포함, 재귀적)
     */
    public static DepartmentServiceDto fromEntityWithChildren(Department department) {
        if (department == null) return null;

        return DepartmentServiceDto.builder()
                .id(department.getId())
                .name(department.getName())
                .nameKR(department.getNameKR())
                .parentId(department.getParentId())
                .headUserId(department.getHeadUser() != null ? department.getHeadUser().getId() : null)
                .level(department.getLevel())
                .desc(department.getDesc())
                .color(department.getColor())
                .company(department.getCompany())
                .companyId(department.getCompany() != null ? department.getCompany().getId() : null)
                .children(department.getChildren() != null
                        ? department.getChildren().stream()
                        .filter(child -> YNType.isN(child.getIsDeleted()))
                        .map(DepartmentServiceDto::fromEntityWithChildren)
                        .toList()
                        : null)
                .build();
    }
}
