package com.lshdainty.porest.company.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.company.controller.dto.CompanyDto;
import com.lshdainty.porest.company.service.CompanyService;
import com.lshdainty.porest.company.service.dto.CompanyServiceDto;
import com.lshdainty.porest.department.controller.dto.DepartmentDto;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompanyApiController {
    private final CompanyService companyService;

    @PostMapping("/api/v1/company")
    public ApiResponse registCompany(@RequestBody CompanyDto data) {
        String companyId = companyService.regist(CompanyServiceDto.builder()
                .id(data.getCompanyId())
                .name(data.getCompanyName())
                .desc(data.getCompanyDesc())
                .build()
        );

        return ApiResponse.success(CompanyDto.builder().companyId(companyId).build());
    }

    @GetMapping("/api/v1/company")
    public ApiResponse searchCompany() {
        CompanyServiceDto company = companyService.searchCompany();

        if (company == null || company.getId() == null || company.getId().isEmpty()) {
            return ApiResponse.success();
        }

        return ApiResponse.success(CompanyDto.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyDesc(company.getDesc())
                .build()
        );
    }

    @PutMapping("/api/v1/company/{id}")
    public ApiResponse editCompany(@PathVariable("id") String companyId, @RequestBody CompanyDto data) {
        companyService.edit(CompanyServiceDto.builder()
                .id(companyId)
                .name(data.getCompanyName())
                .desc(data.getCompanyDesc())
                .build()
        );
        return ApiResponse.success();
    }

    @DeleteMapping("/api/v1/company/{id}")
    public ApiResponse deleteCompany(@PathVariable("id") String companyId) {
        companyService.delete(companyId);
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/company/{id}/departments")
    public ApiResponse searchCompanyWithDepartments(@PathVariable("id") String companyId) {
        CompanyServiceDto company = companyService.searchCompanyWithDepartments(companyId);

        return ApiResponse.success(CompanyDto.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .companyDesc(company.getDesc())
                .departments(company.getDepartments() != null
                        ? company.getDepartments().stream()
                        .map(this::convertToDepartmentDto)
                        .toList()
                        : null)
                .build()
        );
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
