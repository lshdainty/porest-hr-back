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
                        role.getName(),
                        role.getDescription(),
                        role.getPermissions().stream()
                                .map(Permission::getName)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    /**
     * 특정 역할 조회
     * GET /api/v1/roles/{roleName}
     *
     * @param roleName 역할 이름
     * @return RoleResp
     */
    @GetMapping("/api/v1/roles/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RoleApiDto.RoleResp> getRole(@PathVariable String roleName) {
        Role role = roleService.getRole(roleName);
        return ApiResponse.success(new RoleApiDto.RoleResp(
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(Permission::getName)
                        .collect(Collectors.toList())
        ));
    }

    /**
     * 역할 생성
     * POST /api/v1/roles
     *
     * @param req CreateRoleReq
     * @return 생성된 역할 이름
     */
    @PostMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<String> createRole(@RequestBody RoleApiDto.CreateRoleReq req) {
        Role role;
        if (req.getPermissionNames() != null && !req.getPermissionNames().isEmpty()) {
            role = roleService.createRoleWithPermissions(
                    req.getRoleName(),
                    req.getDescription(),
                    req.getPermissionNames()
            );
        } else {
            role = roleService.createRole(req.getRoleName(), req.getDescription());
        }
        return ApiResponse.success(role.getName());
    }

    /**
     * 역할 수정 (설명 및/또는 권한)
     * PUT /api/v1/roles/{roleName}
     *
     * @param roleName 역할 이름
     * @param req UpdateRoleReq
     * @return void
     */
    @PutMapping("/api/v1/roles/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> updateRole(
            @PathVariable String roleName,
            @RequestBody RoleApiDto.UpdateRoleReq req
    ) {
        if (req.getPermissionNames() != null && !req.getPermissionNames().isEmpty()) {
            // 설명 + 권한 둘 다 수정
            roleService.updateRoleWithPermissions(
                    roleName,
                    req.getDescription(),
                    req.getPermissionNames()
            );
        } else if (StringUtils.hasText(req.getDescription())) {
            // 설명만 수정
            roleService.updateRole(roleName, req.getDescription());
        }
        return ApiResponse.success();
    }

    /**
     * 역할 삭제 (Soft Delete)
     * DELETE /api/v1/roles/{roleName}
     *
     * @param roleName 역할 이름
     * @return void
     */
    @DeleteMapping("/api/v1/roles/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> deleteRole(@PathVariable String roleName) {
        roleService.deleteRole(roleName);
        return ApiResponse.success();
    }

    /* ==================== 역할 권한 관리 ==================== */

    /**
     * 역할 권한 목록 조회
     * GET /api/v1/roles/{roleName}/permissions
     *
     * @param roleName 역할 이름
     * @return List<String>
     */
    @GetMapping("/api/v1/roles/{roleName}/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<String>> getRolePermissions(@PathVariable String roleName) {
        Role role = roleService.getRole(roleName);
        List<String> permissions = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
        return ApiResponse.success(permissions);
    }

    /**
     * 역할 권한 설정 (전체 교체)
     * PUT /api/v1/roles/{roleName}/permissions
     *
     * @param roleName 역할 이름
     * @param req UpdateRolePermissionsReq
     * @return void
     */
    @PutMapping("/api/v1/roles/{roleName}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> updateRolePermissions(
            @PathVariable String roleName,
            @RequestBody RoleApiDto.UpdateRolePermissionsReq req
    ) {
        roleService.updateRolePermissions(roleName, req.getPermissionNames());
        return ApiResponse.success();
    }

    /**
     * 역할에 권한 추가
     * POST /api/v1/roles/{roleName}/permissions
     *
     * @param roleName 역할 이름
     * @param req RolePermissionReq
     * @return void
     */
    @PostMapping("/api/v1/roles/{roleName}/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> addPermissionToRole(
            @PathVariable String roleName,
            @RequestBody RoleApiDto.RolePermissionReq req
    ) {
        roleService.addPermissionToRole(roleName, req.getPermissionName());
        return ApiResponse.success();
    }

    /**
     * 역할에서 권한 제거
     * DELETE /api/v1/roles/{roleName}/permissions/{permissionName}
     *
     * @param roleName 역할 이름
     * @param permissionName 권한 이름
     * @return void
     */
    @DeleteMapping("/api/v1/roles/{roleName}/permissions/{permissionName}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> removePermissionFromRole(
            @PathVariable String roleName,
            @PathVariable String permissionName
    ) {
        roleService.removePermissionFromRole(roleName, permissionName);
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
                        p.getId(),
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
     * GET /api/v1/permissions/{permissionId}
     *
     * @param permissionId 권한 ID
     * @return PermissionResp
     */
    @GetMapping("/api/v1/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RoleApiDto.PermissionResp> getPermission(@PathVariable String permissionId) {
        Permission permission = roleService.getPermission(permissionId);
        return ApiResponse.success(new RoleApiDto.PermissionResp(
                permission.getId(),
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
                        p.getId(),
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
     * @return 생성된 권한 ID
     */
    @PostMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<String> createPermission(@RequestBody RoleApiDto.CreatePermissionReq req) {
        Permission permission = roleService.createPermission(
                req.getId(),
                req.getName(),
                req.getDescription(),
                req.getResource(),
                req.getAction()
        );
        return ApiResponse.success(permission.getId());
    }

    /**
     * 권한 수정
     * PUT /api/v1/permissions/{permissionId}
     *
     * @param permissionId 권한 ID
     * @param req UpdatePermissionReq
     * @return void
     */
    @PutMapping("/api/v1/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> updatePermission(
            @PathVariable String permissionId,
            @RequestBody RoleApiDto.UpdatePermissionReq req
    ) {
        roleService.updatePermission(
                permissionId,
                req.getName(),
                req.getDescription(),
                req.getResource(),
                req.getAction()
        );
        return ApiResponse.success();
    }

    /**
     * 권한 삭제 (Soft Delete)
     * DELETE /api/v1/permissions/{permissionId}
     *
     * @param permissionId 권한 ID
     * @return void
     */
    @DeleteMapping("/api/v1/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public ApiResponse<Void> deletePermission(@PathVariable String permissionId) {
        roleService.deletePermission(permissionId);
        return ApiResponse.success();
    }
}
