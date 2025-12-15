package com.lshdainty.porest.company.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.company.controller.dto.CompanyApiDto;
import com.lshdainty.porest.company.service.CompanyService;
import com.lshdainty.porest.company.service.dto.CompanyServiceDto;
import com.lshdainty.porest.department.controller.dto.DepartmentApiDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompanyApiController implements CompanyApi {
    private final CompanyService companyService;

    @Override
    @PreAuthorize("hasAuthority('COMPANY:MANAGE')")
    public ApiResponse registCompany(CompanyApiDto.RegistCompanyReq data) {
        String companyId = companyService.regist(CompanyServiceDto.builder()
                .id(data.getCompanyId())
                .name(data.getCompanyName())
                .desc(data.getCompanyDesc())
                .build()
        );

        return ApiResponse.success(new CompanyApiDto.RegistCompanyResp(companyId));
    }

    @Override
    @PreAuthorize("hasAuthority('COMPANY:READ')")
    public ApiResponse searchCompany() {
        CompanyServiceDto company = companyService.searchCompany();

        if (company == null || company.getId() == null || company.getId().isEmpty()) {
            return ApiResponse.success();
        }

        return ApiResponse.success(new CompanyApiDto.SearchCompanyResp(
                company.getId(),
                company.getName(),
                company.getDesc()
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('COMPANY:MANAGE')")
    public ApiResponse editCompany(String companyId, CompanyApiDto.EditCompanyReq data) {
        companyService.edit(CompanyServiceDto.builder()
                .id(companyId)
                .name(data.getCompanyName())
                .desc(data.getCompanyDesc())
                .build()
        );
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('COMPANY:MANAGE')")
    public ApiResponse deleteCompany(String companyId) {
        companyService.delete(companyId);
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('COMPANY:READ')")
    public ApiResponse searchCompanyWithDepartments(String companyId) {
        CompanyServiceDto company = companyService.searchCompanyWithDepartments(companyId);

        return ApiResponse.success(new CompanyApiDto.SearchCompanyWithDepartmentsResp(
                company.getId(),
                company.getName(),
                company.getDesc(),
                company.getDepartments() != null
                        ? company.getDepartments().stream()
                        .map(DepartmentApiDto.SearchDepartmentWithChildrenResp::fromServiceDto)
                        .toList()
                        : null
        ));
    }
}
