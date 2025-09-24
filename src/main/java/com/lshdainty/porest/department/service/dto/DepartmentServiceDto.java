package com.lshdainty.porest.department.service.dto;

import com.lshdainty.porest.company.domain.Company;
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
    private Company company;
    private String companyId;
    private List<DepartmentServiceDto> children;
}
