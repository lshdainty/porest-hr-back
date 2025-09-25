package com.lshdainty.porest.department.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.department.controller.dto.DepartmentDto;
import com.lshdainty.porest.department.service.DepartmentService;
import com.lshdainty.porest.department.service.dto.DepartmentServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DepartmentApiController {
    private final DepartmentService departmentService;

    @PostMapping("/api/v1/department")
    public ApiResponse<DepartmentDto> registDepartment(@RequestBody DepartmentDto department) {
        Long departmentId = departmentService.regist(convertToServiceDto(department));
        return ApiResponse.success(DepartmentDto.builder().departmentId(departmentId).build());
    }

    @PutMapping("/api/v1/department/{id}")
    public ApiResponse<DepartmentDto> editDepartment(@PathVariable("id") Long departmentId, @RequestBody DepartmentDto department) {
        DepartmentServiceDto serviceDto = convertToServiceDto(department);
        serviceDto.setId(departmentId);
        departmentService.edit(serviceDto);
        return ApiResponse.success();
    }

    @DeleteMapping("/api/v1/department/{id}")
    public ApiResponse<DepartmentDto> deleteCompany(@PathVariable("id") Long departmentId) {
        departmentService.delete(departmentId);
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/department/{id}")
    public ApiResponse<DepartmentDto> getDepartment(@PathVariable("id") Long departmentId) {
        DepartmentServiceDto serviceDto = departmentService.searchDepartmentById(departmentId);
        DepartmentDto responseDto = convertToResponseDto(serviceDto);
        return ApiResponse.success(responseDto);
    }

    @GetMapping("/api/v1/department/{id}/children")
    public ApiResponse<DepartmentDto> getDepartmentWithChildren(@PathVariable("id") Long departmentId) {
        DepartmentServiceDto serviceDto = departmentService.searchDepartmentByIdWithChildren(departmentId);
        DepartmentDto responseDto = convertToResponseDtoWithChildren(serviceDto);
        return ApiResponse.success(responseDto);
    }

    /**
     * Controller DTO -> Service DTO 변환
     */
    private DepartmentServiceDto convertToServiceDto(DepartmentDto dto) {
        if (dto == null) return null;

        return DepartmentServiceDto.builder()
                .id(dto.getDepartmentId())
                .name(dto.getDepartmentName())
                .nameKR(dto.getDepartmentNameKR())
                .parentId(dto.getParentId())
                .headUserId(dto.getHeadUserId())
                .level(dto.getTreeLevel())
                .desc(dto.getDepartmentDesc())
                .companyId(dto.getCompanyId())
                .build();
    }

    /**
     * Service DTO -> Controller DTO 변환 (단건)
     */
    private DepartmentDto convertToResponseDto(DepartmentServiceDto serviceDto) {
        if (serviceDto == null) return null;

        return DepartmentDto.builder()
                .departmentId(serviceDto.getId())
                .departmentName(serviceDto.getName())
                .departmentNameKR(serviceDto.getNameKR())
                .parentId(serviceDto.getParentId())
                .headUserId(serviceDto.getHeadUserId())
                .treeLevel(serviceDto.getLevel())
                .departmentDesc(serviceDto.getDesc())
                .companyId(serviceDto.getCompanyId())
                .build();
    }

    /**
     * Service DTO -> Controller DTO 변환 (자식 포함, 재귀적)
     */
    private DepartmentDto convertToResponseDtoWithChildren(DepartmentServiceDto serviceDto) {
        if (serviceDto == null) return null;

        return DepartmentDto.builder()
                .departmentId(serviceDto.getId())
                .departmentName(serviceDto.getName())
                .departmentNameKR(serviceDto.getNameKR())
                .parentId(serviceDto.getParentId())
                .headUserId(serviceDto.getHeadUserId())
                .treeLevel(serviceDto.getLevel())
                .departmentDesc(serviceDto.getDesc())
                .companyId(serviceDto.getCompanyId())
                .children(serviceDto.getChildren() != null
                        ? serviceDto.getChildren().stream()
                        .map(this::convertToResponseDtoWithChildren)
                        .toList()
                        : null)
                .build();
    }
}
