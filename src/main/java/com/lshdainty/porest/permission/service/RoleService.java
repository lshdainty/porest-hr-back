package com.lshdainty.porest.permission.service;

import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.PermissionRepository;
import com.lshdainty.porest.permission.repository.RoleRepository;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role & Permission Service<br>
 * 역할 및 권한 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {
    private final MessageSource ms;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /* ==================== Role 관련 메서드 ==================== */

    /**
     * 전체 역할 목록 조회 (권한 포함)
     *
     * @return List<Role>
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAllRolesWithPermissions();
    }

    /**
     * 특정 역할 조회 (권한 포함)
     *
     * @param roleCode 역할 코드
     * @return Role
     */
    public Role getRole(String roleCode) {
        return roleRepository.findByCodeWithPermissions(roleCode)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.role", null, null)));
    }

    /**
     * 역할 생성
     *
     * @param roleCode 역할 코드
     * @param roleName 역할 이름
     * @param description 역할 설명
     * @return Role
     */
    @Transactional
    public Role createRole(String roleCode, String roleName, String description) {
        if (roleRepository.findByCode(roleCode).isPresent()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.role.already.exists", null, null));
        }
        Role role = Role.createRole(roleCode, roleName, description);
        roleRepository.save(role);
        return role;
    }

    /**
     * 역할 생성 (권한 포함)
     *
     * @param roleCode 역할 코드
     * @param roleName 역할 이름
     * @param description 역할 설명
     * @param permissionCodes 권한 코드 리스트
     * @return Role
     */
    @Transactional
    public Role createRoleWithPermissions(String roleCode, String roleName, String description, List<String> permissionCodes) {
        if (roleRepository.findByCode(roleCode).isPresent()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.role.already.exists", null, null));
        }

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null))))
                .collect(Collectors.toList());

        Role role = Role.createRoleWithPermissions(roleCode, roleName, description, permissions);
        roleRepository.save(role);
        return role;
    }

    /**
     * 역할 정보 수정 (설명만)
     *
     * @param roleCode 역할 코드
     * @param description 역할 설명
     */
    @Transactional
    public void updateRole(String roleCode, String description) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.role", null, null)));
        role.updateRole(null, description, null);
    }

    /**
     * 역할 정보 수정 (설명 + 권한)
     *
     * @param roleCode 역할 코드
     * @param description 역할 설명
     * @param permissionCodes 권한 코드 리스트
     */
    @Transactional
    public void updateRoleWithPermissions(String roleCode, String description, List<String> permissionCodes) {
        Role role = getRole(roleCode);

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null))))
                .collect(Collectors.toList());

        role.updateRole(null, description, permissions);
    }

    /**
     * 역할의 권한만 수정
     *
     * @param roleCode 역할 코드
     * @param permissionCodes 권한 코드 리스트
     */
    @Transactional
    public void updateRolePermissions(String roleCode, List<String> permissionCodes) {
        Role role = getRole(roleCode);

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null))))
                .collect(Collectors.toList());

        role.clearPermissions();
        permissions.forEach(role::addPermission);
    }

    /**
     * 역할에 권한 추가
     *
     * @param roleCode 역할 코드
     * @param permissionCode 권한 코드
     */
    @Transactional
    public void addPermissionToRole(String roleCode, String permissionCode) {
        Role role = getRole(roleCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null)));
        role.addPermission(permission);
    }

    /**
     * 역할에서 권한 제거
     *
     * @param roleCode 역할 코드
     * @param permissionCode 권한 코드
     */
    @Transactional
    public void removePermissionFromRole(String roleCode, String permissionCode) {
        Role role = getRole(roleCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null)));
        role.removePermission(permission);
    }

    /**
     * 역할 삭제 (Soft Delete)
     *
     * @param roleCode 역할 코드
     */
    @Transactional
    public void deleteRole(String roleCode) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.role", null, null)));
        role.deleteRole();
    }

    /* ==================== Permission 관련 메서드 ==================== */

    /**
     * 전체 권한 목록 조회
     *
     * @return List<Permission>
     */
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllPermissions();
    }

    /**
     * 특정 권한 조회
     *
     * @param permissionCode 권한 코드
     * @return Permission
     */
    public Permission getPermission(String permissionCode) {
        return permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null)));
    }

    /**
     * 리소스별 권한 목록 조회
     *
     * @param resource 리소스명
     * @return List<Permission>
     */
    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    /**
     * 권한 생성
     *
     * @param code 권한 코드
     * @param name 권한 이름 (한글명)
     * @param description 권한 설명
     * @param resource 리소스
     * @param action 액션
     * @return Permission
     */
    @Transactional
    public Permission createPermission(String code, String name, String description, String resource, String action) {
        if (permissionRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.permission.already.exists", null, null));
        }
        ResourceType resourceType = ResourceType.valueOf(resource);
        ActionType actionType = ActionType.valueOf(action);
        Permission permission = Permission.createPermission(code, name, description, resourceType, actionType);
        permissionRepository.save(permission);
        return permission;
    }

    /**
     * 권한 수정
     *
     * @param code 권한 코드
     * @param name 권한 이름 (한글명)
     * @param description 권한 설명
     * @param resource 리소스
     * @param action 액션
     */
    @Transactional
    public void updatePermission(String code, String name, String description, String resource, String action) {
        Permission permission = getPermission(code);
        ResourceType resourceType = resource != null ? ResourceType.valueOf(resource) : null;
        ActionType actionType = action != null ? ActionType.valueOf(action) : null;
        permission.updatePermission(name, description, resourceType, actionType);
    }

    /**
     * 권한 삭제 (Soft Delete)
     *
     * @param permissionCode 권한 코드
     */
    @Transactional
    public void deletePermission(String permissionCode) {
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null)));
        permission.deletePermission();
    }
}
