package com.lshdainty.porest.department.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.department.controller.dto.DepartmentApiDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Department", description = "부서 관리 API")
public interface DepartmentApi {

    @Operation(
            summary = "부서 등록",
            description = "새로운 부서를 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "부서 등록 성공",
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.RegistDepartmentResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            )
    })
    @PostMapping("/api/v1/departments")
    ApiResponse registDepartment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "부서 등록 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.RegistDepartmentReq.class))
            )
            @RequestBody DepartmentApiDto.RegistDepartmentReq data
    );

    @Operation(
            summary = "부서 수정",
            description = "기존 부서 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "부서 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "부서 정보를 찾을 수 없음"
            )
    })
    @PutMapping("/api/v1/departments/{id}")
    ApiResponse editDepartment(
            @Parameter(description = "부서 ID", example = "1", required = true)
            @PathVariable("id") Long departmentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "부서 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.EditDepartmentReq.class))
            )
            @RequestBody DepartmentApiDto.EditDepartmentReq data
    );

    @Operation(
            summary = "부서 삭제",
            description = "기존 부서를 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "부서 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "부서 정보를 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/departments/{id}")
    ApiResponse deleteDepartment(
            @Parameter(description = "부서 ID", example = "1", required = true)
            @PathVariable("id") Long departmentId
    );

    @Operation(
            summary = "부서 조회",
            description = "부서 ID로 부서 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "부서 조회 성공",
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.SearchDepartmentResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:READ 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "부서 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/api/v1/departments/{id}")
    ApiResponse searchDepartmentById(
            @Parameter(description = "부서 ID", example = "1", required = true)
            @PathVariable("id") Long departmentId
    );

    @Operation(
            summary = "부서 및 하위 부서 조회",
            description = "부서 ID로 부서 정보와 하위 부서 목록을 함께 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "부서 및 하위 부서 조회 성공",
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.SearchDepartmentWithChildrenResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:READ 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "부서 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/api/v1/departments/{id}/children")
    ApiResponse searchDepartmentByIdWithChildren(
            @Parameter(description = "부서 ID", example = "1", required = true)
            @PathVariable("id") Long departmentId
    );

    @Operation(
            summary = "부서에 사용자 추가",
            description = "특정 부서에 사용자를 추가합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 추가 성공",
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.RegistDepartmentUserResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "부서 정보를 찾을 수 없음"
            )
    })
    @PostMapping("/api/v1/departments/{departmentId}/users")
    ApiResponse registDepartmentUsers(
            @Parameter(description = "부서 ID", example = "1", required = true)
            @PathVariable("departmentId") Long departmentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "부서에 추가할 사용자 목록",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.RegistDepartmentUserReq.class))
            )
            @RequestBody DepartmentApiDto.RegistDepartmentUserReq data
    );

    @Operation(
            summary = "부서에서 사용자 제거",
            description = "특정 부서에서 사용자를 제거합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 제거 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:MANAGE 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "부서 정보를 찾을 수 없음"
            )
    })
    @DeleteMapping("/api/v1/departments/{departmentId}/users")
    ApiResponse deleteDepartmentUsers(
            @Parameter(description = "부서 ID", example = "1", required = true)
            @PathVariable("departmentId") Long departmentId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "부서에서 제거할 사용자 ID 목록",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.DeleteDepartmentUserReq.class))
            )
            @RequestBody DepartmentApiDto.DeleteDepartmentUserReq data
    );

    @Operation(
            summary = "부서 소속 사용자 조회",
            description = "부서에 소속된 사용자와 소속되지 않은 사용자 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = DepartmentApiDto.GetDepartmentUsersResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (COMPANY:READ 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "부서 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/api/v1/departments/{departmentId}/users")
    ApiResponse getDepartmentUsers(
            @Parameter(description = "부서 ID", example = "1", required = true)
            @PathVariable("departmentId") Long departmentId
    );
}
