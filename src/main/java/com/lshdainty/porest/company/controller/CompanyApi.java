package com.lshdainty.porest.company.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.company.controller.dto.CompanyApiDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Company", description = "회사 관리 API")
public interface CompanyApi {

    @Operation(
            summary = "회사 등록",
            description = "새로운 회사 정보를 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회사 등록 성공",
                    content = @Content(schema = @Schema(implementation = CompanyApiDto.RegistCompanyResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            )
    })
    @PostMapping("/api/v1/company")
    ApiResponse registCompany(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회사 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CompanyApiDto.RegistCompanyReq.class))
            )
            @RequestBody CompanyApiDto.RegistCompanyReq data
    );

    @Operation(
            summary = "회사 조회",
            description = "등록된 회사 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회사 조회 성공",
                    content = @Content(schema = @Schema(implementation = CompanyApiDto.SearchCompanyResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:READ 필요)"
            )
    })
    @GetMapping("/api/v1/company")
    ApiResponse searchCompany();

    @Operation(
            summary = "회사 수정",
            description = "기존 회사 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회사 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회사 정보를 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/company/{id}")
    ApiResponse editCompany(
            @Parameter(description = "회사 ID", example = "POREST", required = true)
            @PathVariable("id") String companyId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회사 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CompanyApiDto.EditCompanyReq.class))
            )
            @RequestBody CompanyApiDto.EditCompanyReq data
    );

    @Operation(
            summary = "회사 삭제",
            description = "기존 회사 정보를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회사 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회사 정보를 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/company/{id}")
    ApiResponse deleteCompany(
            @Parameter(description = "회사 ID", example = "POREST", required = true)
            @PathVariable("id") String companyId
    );

    @Operation(
            summary = "회사 및 부서 조회",
            description = "회사 정보와 소속 부서 목록을 함께 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회사 및 부서 조회 성공",
                    content = @Content(schema = @Schema(implementation = CompanyApiDto.SearchCompanyWithDepartmentsResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:READ 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "회사 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/api/v1/company/{id}/departments")
    ApiResponse searchCompanyWithDepartments(
            @Parameter(description = "회사 ID", example = "POREST", required = true)
            @PathVariable("id") String companyId
    );
}
