package com.porest.hr.permission.controller;

import com.porest.core.controller.ApiResponse;
import com.porest.hr.permission.controller.dto.RoleApiDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Role & Permission", description = "역할 및 권한 관리 API")
public interface RoleApi {

    /* ==================== Role API ==================== */

    @Operation(summary = "전체 역할 목록 조회", description = "시스템의 모든 역할을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @GetMapping("/api/v1/roles")
    ApiResponse<List<RoleApiDto.RoleResp>> getAllRoles();

    @Operation(summary = "특정 역할 조회", description = "역할 코드로 특정 역할을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음")
    })
    @GetMapping("/api/v1/roles/{roleCode}")
    ApiResponse<RoleApiDto.RoleResp> getRole(
            @Parameter(description = "역할 코드", example = "ADMIN", required = true)
            @PathVariable String roleCode
    );

    @Operation(summary = "역할 생성", description = "새로운 역할을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @PostMapping("/api/v1/roles")
    ApiResponse<String> createRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "역할 생성 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RoleApiDto.CreateRoleReq.class))
            )
            @RequestBody RoleApiDto.CreateRoleReq req
    );

    @Operation(summary = "역할 수정", description = "기존 역할의 설명 및 권한을 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음")
    })
    @PutMapping("/api/v1/roles/{roleCode}")
    ApiResponse<Void> updateRole(
            @Parameter(description = "역할 코드", example = "ADMIN", required = true)
            @PathVariable String roleCode,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "역할 수정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RoleApiDto.UpdateRoleReq.class))
            )
            @RequestBody RoleApiDto.UpdateRoleReq req
    );

    @Operation(summary = "역할 삭제", description = "역할을 삭제합니다 (Soft Delete).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "역할을 찾을 수 없음")
    })
    @DeleteMapping("/api/v1/roles/{roleCode}")
    ApiResponse<Void> deleteRole(
            @Parameter(description = "역할 코드", example = "ADMIN", required = true)
            @PathVariable String roleCode
    );

    /* ==================== 역할 권한 관리 ==================== */

    @Operation(summary = "역할 권한 목록 조회", description = "특정 역할에 할당된 권한 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @GetMapping("/api/v1/roles/{roleCode}/permissions")
    ApiResponse<List<String>> getRolePermissions(
            @Parameter(description = "역할 코드", example = "ADMIN", required = true)
            @PathVariable String roleCode
    );

    @Operation(summary = "역할 권한 설정", description = "역할의 권한을 전체 교체합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @PutMapping("/api/v1/roles/{roleCode}/permissions")
    ApiResponse<Void> updateRolePermissions(
            @Parameter(description = "역할 코드", example = "ADMIN", required = true)
            @PathVariable String roleCode,
            @RequestBody RoleApiDto.UpdateRolePermissionsReq req
    );

    @Operation(summary = "역할에 권한 추가", description = "특정 역할에 권한을 추가합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추가 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @PostMapping("/api/v1/roles/{roleCode}/permissions")
    ApiResponse<Void> addPermissionToRole(
            @Parameter(description = "역할 코드", example = "ADMIN", required = true)
            @PathVariable String roleCode,
            @RequestBody RoleApiDto.RolePermissionReq req
    );

    @Operation(summary = "역할에서 권한 제거", description = "특정 역할에서 권한을 제거합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "제거 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @DeleteMapping("/api/v1/roles/{roleCode}/permissions/{permissionCode}")
    ApiResponse<Void> removePermissionFromRole(
            @Parameter(description = "역할 코드", example = "ADMIN", required = true)
            @PathVariable String roleCode,
            @Parameter(description = "권한 코드", example = "USER_READ", required = true)
            @PathVariable String permissionCode
    );

    /* ==================== Permission API ==================== */

    @Operation(summary = "내 권한 목록 조회", description = "현재 로그인한 사용자의 권한 목록을 조회합니다.")
    @GetMapping("/api/v1/permissions/my")
    ApiResponse<List<String>> getMyPermissions();

    @Operation(summary = "전체 권한 목록 조회", description = "시스템의 모든 권한을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @GetMapping("/api/v1/permissions")
    ApiResponse<List<RoleApiDto.PermissionResp>> getAllPermissions();

    @Operation(summary = "특정 권한 조회", description = "권한 코드로 특정 권한을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @GetMapping("/api/v1/permissions/{permissionCode}")
    ApiResponse<RoleApiDto.PermissionResp> getPermission(
            @Parameter(description = "권한 코드", example = "USER_READ", required = true)
            @PathVariable String permissionCode
    );

    @Operation(summary = "리소스별 권한 조회", description = "특정 리소스에 대한 권한 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @GetMapping("/api/v1/permissions/resource/{resource}")
    ApiResponse<List<RoleApiDto.PermissionResp>> getPermissionsByResource(
            @Parameter(description = "리소스명", example = "USER", required = true)
            @PathVariable String resource
    );

    @Operation(summary = "권한 생성", description = "새로운 권한을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @PostMapping("/api/v1/permissions")
    ApiResponse<String> createPermission(@RequestBody RoleApiDto.CreatePermissionReq req);

    @Operation(summary = "권한 수정", description = "기존 권한 정보를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @PutMapping("/api/v1/permissions/{permissionCode}")
    ApiResponse<Void> updatePermission(
            @Parameter(description = "권한 코드", example = "USER_READ", required = true)
            @PathVariable String permissionCode,
            @RequestBody RoleApiDto.UpdatePermissionReq req
    );

    @Operation(summary = "권한 삭제", description = "권한을 삭제합니다 (Soft Delete).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ROLE:MANAGE 필요)")
    })
    @DeleteMapping("/api/v1/permissions/{permissionCode}")
    ApiResponse<Void> deletePermission(
            @Parameter(description = "권한 코드", example = "USER_READ", required = true)
            @PathVariable String permissionCode
    );
}
