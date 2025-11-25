package com.lshdainty.porest.permission.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.permission.controller.dto.RoleApiDto;
import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role & Permission API Controller<br>
 * 역할 및 권한 관리 통합 RESTful API
 */
@RestController
@RequiredArgsConstructor
public class RoleApiController {
    private final RoleService roleService;

    /* ==================== Role API ==================== */

    /**
     * 전체 역할 목록 조회
     * GET /api/v1/roles
     *
     * @return List<RoleResp>
     */
    @GetMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<RoleApiDto.RoleResp>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        List<RoleApiDto.RoleResp> resp = roles.stream()
                .map(role -> new RoleApiDto.RoleResp(
                        role.getCode(),
                        role.getName(),
                        role.getDescription(),
                        role.getPermissions().stream()
                                .map(Permission::getCode)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    /**
     * 특정 역할 조회
     * GET /api/v1/roles/{roleCode}
     *
     * @param roleCode 역할 코드
     * @return RoleResp
     */
    @GetMapping("/api/v1/roles/{roleCode}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RoleApiDto.RoleResp> getRole(@PathVariable String roleCode) {
        Role role = roleService.getRole(roleCode);
        return ApiResponse.success(new RoleApiDto.RoleResp(
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(Permission::getCode)
                        .collect(Collectors.toList())
        ));
    }

    /**
     * 역할 생성
     * POST /api/v1/roles
     *
     * @param req CreateRoleReq
     * @return 생성된 역할 코드
     */
    @PostMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<String> createRole(@RequestBody RoleApiDto.CreateRoleReq req) {
        Role role;
        if (req.getPermissionCodes() != null && !req.getPermissionCodes().isEmpty()) {
            role = roleService.createRoleWithPermissions(
                    req.getRoleCode(),
                    req.getRoleName(),
                    req.getDescription(),
                    req.getPermissionCodes()
            );
        } else {
            role = roleService.createRole(req.getRoleCode(), req.getRoleName(), req.getDescription());
        }
        return ApiResponse.success(role.getCode());
    }

    /**
     * 역할 수정 (설명 및/또는 권한)
     * PUT /api/v1/roles/{roleCode}
     *
     * @param roleCode 역할 코드
     * @param req UpdateRoleReq
     * @return void
     */
    @PutMapping("/api/v1/roles/{roleCode}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> updateRole(
            @PathVariable String roleCode,
            @RequestBody RoleApiDto.UpdateRoleReq req
    ) {
        if (req.getPermissionCodes() != null && !req.getPermissionCodes().isEmpty()) {
            // 설명 + 권한 둘 다 수정
            roleService.updateRoleWithPermissions(
                    roleCode,
                    req.getDescription(),
                    req.getPermissionCodes()
            );
        } else if (StringUtils.hasText(req.getDescription())) {
            // 설명만 수정
            roleService.updateRole(roleCode, req.getDescription());
        }
        return ApiResponse.success();
    }

    /**
     * 역할 삭제 (Soft Delete)
     * DELETE /api/v1/roles/{roleCode}
     *
     * @param roleCode 역할 코드
     * @return void
     */
    @DeleteMapping("/api/v1/roles/{roleCode}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> deleteRole(@PathVariable String roleCode) {
        roleService.deleteRole(roleCode);
        return ApiResponse.success();
    }

    /* ==================== 역할 권한 관리 ==================== */

    /**
     * 역할 권한 목록 조회
     * GET /api/v1/roles/{roleCode}/permissions
     *
     * @param roleCode 역할 코드
     * @return List<String>
     */
    @GetMapping("/api/v1/roles/{roleCode}/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<String>> getRolePermissions(@PathVariable String roleCode) {
        Role role = roleService.getRole(roleCode);
        List<String> permissions = role.getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
        return ApiResponse.success(permissions);
    }

    /**
     * 역할 권한 설정 (전체 교체)
     * PUT /api/v1/roles/{roleCode}/permissions
     *
     * @param roleCode 역할 코드
     * @param req UpdateRolePermissionsReq
     * @return void
     */
    @PutMapping("/api/v1/roles/{roleCode}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> updateRolePermissions(
            @PathVariable String roleCode,
            @RequestBody RoleApiDto.UpdateRolePermissionsReq req
    ) {
        roleService.updateRolePermissions(roleCode, req.getPermissionCodes());
        return ApiResponse.success();
    }

    /**
     * 역할에 권한 추가
     * POST /api/v1/roles/{roleCode}/permissions
     *
     * @param roleCode 역할 코드
     * @param req RolePermissionReq
     * @return void
     */
    @PostMapping("/api/v1/roles/{roleCode}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> addPermissionToRole(
            @PathVariable String roleCode,
            @RequestBody RoleApiDto.RolePermissionReq req
    ) {
        roleService.addPermissionToRole(roleCode, req.getPermissionCode());
        return ApiResponse.success();
    }

    /**
     * 역할에서 권한 제거
     * DELETE /api/v1/roles/{roleCode}/permissions/{permissionCode}
     *
     * @param roleCode 역할 코드
     * @param permissionCode 권한 코드
     * @return void
     */
    @DeleteMapping("/api/v1/roles/{roleCode}/permissions/{permissionCode}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> removePermissionFromRole(
            @PathVariable String roleCode,
            @PathVariable String permissionCode
    ) {
        roleService.removePermissionFromRole(roleCode, permissionCode);
        return ApiResponse.success();
    }

    /* ==================== Permission API ==================== */

    /**
     * 현재 사용자의 권한 목록 조회
     * GET /api/v1/permissions/my
     *
     * @return List<String>
     */
    @GetMapping("/api/v1/permissions/my")
    public ApiResponse<List<String>> getMyPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return ApiResponse.success(permissions);
    }

    /**
     * 전체 권한 목록 조회
     * GET /api/v1/permissions
     *
     * @return List<PermissionResp>
     */
    @GetMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<RoleApiDto.PermissionResp>> getAllPermissions() {
        List<Permission> permissions = roleService.getAllPermissions();
        List<RoleApiDto.PermissionResp> resp = permissions.stream()
                .map(p -> new RoleApiDto.PermissionResp(
                        p.getCode(),
                        p.getName(),
                        p.getDescription(),
                        p.getResource().name(),
                        p.getAction().name()
                ))
                .collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    /**
     * 특정 권한 조회
     * GET /api/v1/permissions/{permissionCode}
     *
     * @param permissionCode 권한 코드
     * @return PermissionResp
     */
    @GetMapping("/api/v1/permissions/{permissionCode}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RoleApiDto.PermissionResp> getPermission(@PathVariable String permissionCode) {
        Permission permission = roleService.getPermission(permissionCode);
        return ApiResponse.success(new RoleApiDto.PermissionResp(
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                permission.getResource().name(),
                permission.getAction().name()
        ));
    }

    /**
     * 리소스별 권한 목록 조회
     * GET /api/v1/permissions/resource/{resource}
     *
     * @param resource 리소스명
     * @return List<PermissionResp>
     */
    @GetMapping("/api/v1/permissions/resource/{resource}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<RoleApiDto.PermissionResp>> getPermissionsByResource(@PathVariable String resource) {
        List<Permission> permissions = roleService.getPermissionsByResource(resource);
        List<RoleApiDto.PermissionResp> resp = permissions.stream()
                .map(p -> new RoleApiDto.PermissionResp(
                        p.getCode(),
                        p.getName(),
                        p.getDescription(),
                        p.getResource().name(),
                        p.getAction().name()
                ))
                .collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    /**
     * 권한 생성
     * POST /api/v1/permissions
     *
     * @param req CreatePermissionReq
     * @return 생성된 권한 코드
     */
    @PostMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<String> createPermission(@RequestBody RoleApiDto.CreatePermissionReq req) {
        Permission permission = roleService.createPermission(
                req.getCode(),
                req.getName(),
                req.getDescription(),
                req.getResource(),
                req.getAction()
        );
        return ApiResponse.success(permission.getCode());
    }

    /**
     * 권한 수정
     * PUT /api/v1/permissions/{permissionCode}
     *
     * @param permissionCode 권한 코드
     * @param req UpdatePermissionReq
     * @return void
     */
    @PutMapping("/api/v1/permissions/{permissionCode}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> updatePermission(
            @PathVariable String permissionCode,
            @RequestBody RoleApiDto.UpdatePermissionReq req
    ) {
        roleService.updatePermission(
                permissionCode,
                req.getName(),
                req.getDescription(),
                req.getResource(),
                req.getAction()
        );
        return ApiResponse.success();
    }

    /**
     * 권한 삭제 (Soft Delete)
     * DELETE /api/v1/permissions/{permissionCode}
     *
     * @param permissionCode 권한 코드
     * @return void
     */
    @DeleteMapping("/api/v1/permissions/{permissionCode}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> deletePermission(@PathVariable String permissionCode) {
        roleService.deletePermission(permissionCode);
        return ApiResponse.success();
    }
}
