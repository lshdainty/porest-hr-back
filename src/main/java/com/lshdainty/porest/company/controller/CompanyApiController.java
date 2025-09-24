package com.lshdainty.porest.company.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.company.controller.dto.CompanyDto;
import com.lshdainty.porest.company.service.CompanyService;
import com.lshdainty.porest.company.service.dto.CompanyServiceDto;
import com.lshdainty.porest.department.controller.dto.DepartmentDto;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompanyApiController {
    private final CompanyService companyService;

    @GetMapping("/api/v1/company/{id}/departments")
    public ApiResponse getCompanyWithDepartments(@PathVariable("id") String companyId) {
        CompanyServiceDto company = companyService.findCompanyWithDepartments(companyId);
        CompanyDto resp = CompanyDto.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyDesc(company.getDesc())
                .departments(company.getDepartments() != null
                        ? company.getDepartments().stream()
                        .map(this::convertToDepartmentDto)
                        .toList()
                        : null)
                .build();
        log.info("Company with departments: " + resp);
        return ApiResponse.success(resp);
    }

    /**
     * DepartmentServiceDto -> DepartmentDto 변환 (재귀적)
     */
    private DepartmentDto convertToDepartmentDto(DepartmentServiceDto serviceDto) {
        if (serviceDto == null) return null;

        return DepartmentDto.builder()
                .departmentId(serviceDto.getId())
                .departmentName(serviceDto.getName())
                .departmentNameKR(serviceDto.getNameKR())
                .parentId(serviceDto.getParentId())
                .headUserId(serviceDto.getHeadUserId())
                .treeLevel(serviceDto.getLevel())
                .departmentDesc(serviceDto.getDesc())
                .children(serviceDto.getChildren() != null
                        ? serviceDto.getChildren().stream()
                        .map(this::convertToDepartmentDto)
                        .toList()
                        : null)
                .build();
    }
}
