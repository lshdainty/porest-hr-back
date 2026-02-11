package com.porest.hr.company.service.dto;

import com.porest.hr.department.service.dto.DepartmentServiceDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Builder
public class CompanyServiceDto {
    private String id;
    private String name;
    private String desc;
    private List<DepartmentServiceDto> departments;
}
