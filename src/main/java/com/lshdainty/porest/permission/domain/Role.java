package com.lshdainty.porest.permission.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Role Entity<br>
 * 역할 정보를 관리하는 엔티티<br>
 * RBAC(Role-Based Access Control) 기반의 역할 관리<br>
 * 각 역할은 여러 개의 Permission을 가질 수 있음
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "roles")
public class Role extends AuditingFields {

    /**
     * 역할 이름 (Primary Key)<br>
     * 예: ADMIN, MANAGER, EMPLOYEE
     */
    @Id
    @Column(name = "role_name")
    private String name;

    /**
     * 역할 설명<br>
     * 역할에 대한 상세 설명
     */
    @Column(name = "description")
    private String description;

    /**
     * 권한 목록<br>
     * 해당 역할이 가진 권한들의 리스트<br>
     * EAGER 전략으로 권한 정보를 함께 로드
     */
    @BatchSize(size = 100)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    private List<Permission> permissions = new ArrayList<>();

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;

    /**
     * 역할 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 역할 생성할 것
     *
     * @param name 역할 이름
     * @param description 역할 설명
     * @return Role
     */
    public static Role createRole(String name, String description) {
        Role role = new Role();
        role.name = name;
        role.description = description;
        role.permissions = new ArrayList<>();
        role.isDeleted = YNType.N;
        return role;
    }

    /**
     * 역할 생성 함수 (권한 포함)<br>
     * 권한 리스트와 함께 역할을 생성
     *
     * @param name 역할 이름
     * @param description 역할 설명
     * @param permissions 권한 리스트
     * @return Role
     */
    public static Role createRoleWithPermissions(String name, String description, List<Permission> permissions) {
        Role role = new Role();
        role.name = name;
        role.description = description;
        role.permissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
        role.isDeleted = YNType.N;
        return role;
    }

    /**
     * 역할 수정 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 역할 수정할 것
     *
     * @param description 역할 설명
     * @param permissions 권한 리스트
     */
    public void updateRole(String description, List<Permission> permissions) {
        if (!Objects.isNull(description)) { this.description = description; }
        if (!Objects.isNull(permissions)) { this.permissions = new ArrayList<>(permissions); }
    }

    /**
     * 역할 삭제 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 역할 삭제할 것 (Soft Delete)
     */
    public void deleteRole() {
        this.isDeleted = YNType.Y;
    }

    /* 비즈니스 편의 메소드 */

    /**
     * 권한 추가<br>
     * 역할에 새로운 권한을 추가
     *
     * @param permission 추가할 권한
     */
    public void addPermission(Permission permission) {
        if (!this.permissions.contains(permission)) {
            this.permissions.add(permission);
        }
    }

    /**
     * 권한 제거<br>
     * 역할에서 특정 권한을 제거
     *
     * @param permission 제거할 권한
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    /**
     * 모든 권한 제거<br>
     * 역할의 모든 권한을 제거
     */
    public void clearPermissions() {
        this.permissions.clear();
    }

    /**
     * 특정 권한 보유 여부 확인<br>
     * 역할이 특정 권한을 가지고 있는지 확인
     *
     * @param permissionName 확인할 권한 이름
     * @return 권한 보유 여부
     */
    public boolean hasPermission(String permissionName) {
        return this.permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }
}
