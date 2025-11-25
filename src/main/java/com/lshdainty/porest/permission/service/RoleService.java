package com.lshdainty.porest.permission.service;

import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.PermissionRepository;
import com.lshdainty.porest.permission.repository.RoleRepository;
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
     * @param roleName 역할 이름
     * @return Role
     */
    public Role getRole(String roleName) {
        return roleRepository.findByNameWithPermissions(roleName)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.role", null, null)));
    }

    /**
     * 역할 생성
     *
     * @param roleName 역할 이름
     * @param description 역할 설명
     * @return Role
     */
    @Transactional
    public Role createRole(String roleName, String description) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.role.already.exists", null, null));
        }
        Role role = Role.createRole(roleName, description);
        roleRepository.save(role);
        return role;
    }

    /**
     * 역할 생성 (권한 포함)
     *
     * @param roleName 역할 이름
     * @param description 역할 설명
     * @param permissionNames 권한 이름 리스트
     * @return Role
     */
    @Transactional
    public Role createRoleWithPermissions(String roleName, String description, List<String> permissionNames) {
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.role.already.exists", null, null));
        }

        List<Permission> permissions = permissionNames.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null))))
                .collect(Collectors.toList());

        Role role = Role.createRoleWithPermissions(roleName, description, permissions);
        roleRepository.save(role);
        return role;
    }

    /**
     * 역할 정보 수정 (설명만)
     *
     * @param roleName 역할 이름
     * @param description 역할 설명
     */
    @Transactional
    public void updateRole(String roleName, String description) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.role", null, null)));
        role.updateRole(description, null);
    }

    /**
     * 역할 정보 수정 (설명 + 권한)
     *
     * @param roleName 역할 이름
     * @param description 역할 설명
     * @param permissionNames 권한 이름 리스트
     */
    @Transactional
    public void updateRoleWithPermissions(String roleName, String description, List<String> permissionNames) {
        Role role = getRole(roleName);

        List<Permission> permissions = permissionNames.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null))))
                .collect(Collectors.toList());

        role.updateRole(description, permissions);
    }

    /**
     * 역할의 권한만 수정
     *
     * @param roleName 역할 이름
     * @param permissionNames 권한 이름 리스트
     */
    @Transactional
    public void updateRolePermissions(String roleName, List<String> permissionNames) {
        Role role = getRole(roleName);

        List<Permission> permissions = permissionNames.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null))))
                .collect(Collectors.toList());

        role.clearPermissions();
        permissions.forEach(role::addPermission);
    }

    /**
     * 역할에 권한 추가
     *
     * @param roleName 역할 이름
     * @param permissionName 권한 이름
     */
    @Transactional
    public void addPermissionToRole(String roleName, String permissionName) {
        Role role = getRole(roleName);
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null)));
        role.addPermission(permission);
    }

    /**
     * 역할에서 권한 제거
     *
     * @param roleName 역할 이름
     * @param permissionName 권한 이름
     */
    @Transactional
    public void removePermissionFromRole(String roleName, String permissionName) {
        Role role = getRole(roleName);
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null)));
        role.removePermission(permission);
    }

    /**
     * 역할 삭제 (Soft Delete)
     *
     * @param roleName 역할 이름
     */
    @Transactional
    public void deleteRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
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
     * @param permissionName 권한 이름
     * @return Permission
     */
    public Permission getPermission(String permissionName) {
        return permissionRepository.findByName(permissionName)
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
     * @param name 권한 이름
     * @param description 권한 설명
     * @param resource 리소스
     * @param action 액션
     * @return Permission
     */
    @Transactional
    public Permission createPermission(String name, String description, String resource, String action) {
        if (permissionRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.permission.already.exists", null, null));
        }
        Permission permission = Permission.createPermission(name, description, resource, action);
        permissionRepository.save(permission);
        return permission;
    }

    /**
     * 권한 수정
     *
     * @param name 권한 이름
     * @param description 권한 설명
     * @param resource 리소스
     * @param action 액션
     */
    @Transactional
    public void updatePermission(String name, String description, String resource, String action) {
        Permission permission = getPermission(name);
        permission.updatePermission(description, resource, action);
    }

    /**
     * 권한 삭제 (Soft Delete)
     *
     * @param permissionName 권한 이름
     */
    @Transactional
    public void deletePermission(String permissionName) {
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.permission", null, null)));
        permission.deletePermission();
    }
}
