package com.lshdainty.porest.permission.service;

import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.PermissionRepository;
import com.lshdainty.porest.permission.repository.RoleRepository;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /* ==================== Role 관련 메서드 ==================== */

    @Override
    public List<Role> getAllRoles() {
        log.debug("전체 역할 목록 조회");
        return roleRepository.findAllRolesWithPermissions();
    }

    @Override
    public Role getRole(String roleCode) {
        log.debug("역할 조회: roleCode={}", roleCode);
        return roleRepository.findByCodeWithPermissions(roleCode)
                .orElseThrow(() -> {
                    log.warn("역할 조회 실패 - 존재하지 않는 역할: roleCode={}", roleCode);
                    return new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND);
                });
    }

    @Override
    @Transactional
    public Role createRole(String roleCode, String roleName, String desc) {
        log.debug("역할 생성 시작: roleCode={}, roleName={}", roleCode, roleName);
        if (roleRepository.findByCode(roleCode).isPresent()) {
            log.warn("역할 생성 실패 - 중복 코드: roleCode={}", roleCode);
            throw new DuplicateException(ErrorCode.ROLE_ALREADY_EXISTS);
        }
        Role role = Role.createRole(roleCode, roleName, desc);
        roleRepository.save(role);
        log.info("역할 생성 완료: roleCode={}", roleCode);
        return role;
    }

    @Override
    @Transactional
    public Role createRoleWithPermissions(String roleCode, String roleName, String desc, List<String> permissionCodes) {
        log.debug("역할 생성 (권한 포함) 시작: roleCode={}, roleName={}, permissionCount={}", roleCode, roleName, permissionCodes.size());
        if (roleRepository.findByCode(roleCode).isPresent()) {
            log.warn("역할 생성 실패 - 중복 코드: roleCode={}", roleCode);
            throw new DuplicateException(ErrorCode.ROLE_ALREADY_EXISTS);
        }

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> {
                            log.warn("역할 생성 실패 - 존재하지 않는 권한: permissionCode={}", code);
                            return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                        }))
                .collect(Collectors.toList());

        Role role = Role.createRoleWithPermissions(roleCode, roleName, desc, permissions);
        roleRepository.save(role);
        log.info("역할 생성 (권한 포함) 완료: roleCode={}, permissionCount={}", roleCode, permissions.size());
        return role;
    }

    @Override
    @Transactional
    public void updateRole(String roleCode, String desc) {
        log.debug("역할 수정 시작: roleCode={}", roleCode);
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> {
                    log.warn("역할 수정 실패 - 존재하지 않는 역할: roleCode={}", roleCode);
                    return new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND);
                });
        role.updateRole(null, desc, null);
        log.info("역할 수정 완료: roleCode={}", roleCode);
    }

    @Override
    @Transactional
    public void updateRoleWithPermissions(String roleCode, String desc, List<String> permissionCodes) {
        log.debug("역할 수정 (권한 포함) 시작: roleCode={}", roleCode);
        Role role = getRole(roleCode);

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> {
                            log.warn("역할 수정 실패 - 존재하지 않는 권한: permissionCode={}", code);
                            return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                        }))
                .collect(Collectors.toList());

        role.updateRole(null, desc, permissions);
        log.info("역할 수정 (권한 포함) 완료: roleCode={}", roleCode);
    }

    @Override
    @Transactional
    public void updateRolePermissions(String roleCode, List<String> permissionCodes) {
        log.debug("역할 권한 수정 시작: roleCode={}", roleCode);
        Role role = getRole(roleCode);

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> {
                            log.warn("역할 권한 수정 실패 - 존재하지 않는 권한: permissionCode={}", code);
                            return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                        }))
                .collect(Collectors.toList());

        role.clearPermissions();
        permissions.forEach(role::addPermission);
        log.info("역할 권한 수정 완료: roleCode={}, permissionCount={}", roleCode, permissions.size());
    }

    @Override
    @Transactional
    public void addPermissionToRole(String roleCode, String permissionCode) {
        log.debug("역할에 권한 추가 시작: roleCode={}, permissionCode={}", roleCode, permissionCode);
        Role role = getRole(roleCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 추가 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        role.addPermission(permission);
        log.info("역할에 권한 추가 완료: roleCode={}, permissionCode={}", roleCode, permissionCode);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(String roleCode, String permissionCode) {
        log.debug("역할에서 권한 제거 시작: roleCode={}, permissionCode={}", roleCode, permissionCode);
        Role role = getRole(roleCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 제거 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        role.removePermission(permission);
        log.info("역할에서 권한 제거 완료: roleCode={}, permissionCode={}", roleCode, permissionCode);
    }

    @Override
    @Transactional
    public void deleteRole(String roleCode) {
        log.debug("역할 삭제 시작: roleCode={}", roleCode);
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> {
                    log.warn("역할 삭제 실패 - 존재하지 않는 역할: roleCode={}", roleCode);
                    return new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND);
                });
        role.deleteRole();
        log.info("역할 삭제 완료: roleCode={}", roleCode);
    }

    /* ==================== Permission 관련 메서드 ==================== */

    @Override
    public List<Permission> getAllPermissions() {
        log.debug("전체 권한 목록 조회");
        return permissionRepository.findAllPermissions();
    }

    @Override
    public Permission getPermission(String permissionCode) {
        log.debug("권한 조회: permissionCode={}", permissionCode);
        return permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 조회 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
    }

    @Override
    public List<Permission> getPermissionsByResource(String resource) {
        log.debug("리소스별 권한 목록 조회: resource={}", resource);
        return permissionRepository.findByResource(resource);
    }

    @Override
    @Transactional
    public Permission createPermission(String code, String name, String desc, String resource, String action) {
        log.debug("권한 생성 시작: code={}, name={}", code, name);
        if (permissionRepository.findByCode(code).isPresent()) {
            log.warn("권한 생성 실패 - 중복 코드: code={}", code);
            throw new DuplicateException(ErrorCode.PERMISSION_ALREADY_EXISTS);
        }
        ResourceType resourceType = ResourceType.valueOf(resource);
        ActionType actionType = ActionType.valueOf(action);
        Permission permission = Permission.createPermission(code, name, desc, resourceType, actionType);
        permissionRepository.save(permission);
        log.info("권한 생성 완료: code={}", code);
        return permission;
    }

    @Override
    @Transactional
    public void updatePermission(String code, String name, String desc, String resource, String action) {
        log.debug("권한 수정 시작: code={}", code);
        Permission permission = getPermission(code);
        ResourceType resourceType = resource != null ? ResourceType.valueOf(resource) : null;
        ActionType actionType = action != null ? ActionType.valueOf(action) : null;
        permission.updatePermission(name, desc, resourceType, actionType);
        log.info("권한 수정 완료: code={}", code);
    }

    @Override
    @Transactional
    public void deletePermission(String permissionCode) {
        log.debug("권한 삭제 시작: permissionCode={}", permissionCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 삭제 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        permission.deletePermission();
        log.info("권한 삭제 완료: permissionCode={}", permissionCode);
    }
}
