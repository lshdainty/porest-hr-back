package com.lshdainty.porest.permission.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RolePermission Entity<br>
 * 역할과 권한의 매핑 정보를 관리하는 중간 엔티티<br>
 * 누가 언제 권한을 부여/수정했는지 추적 가능
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "role_permissions")
public class RolePermission extends AuditingFields {
    /**
     * 역할-권한 매핑 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_permission_id")
    private Long id;

    /**
     * 역할<br>
     * 어떤 역할에 권한이 부여되었는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * 권한<br>
     * 어떤 권한이 부여되었는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 역할-권한 매핑 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 역할-권한 매핑 생성할 것
     *
     * @param role 역할
     * @param permission 권한
     * @return RolePermission
     */
    public static RolePermission createRolePermission(Role role, Permission permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.role = role;
        rolePermission.permission = permission;
        rolePermission.isDeleted = YNType.N;
        return rolePermission;
    }

    /**
     * 역할-권한 매핑 삭제 함수 (Soft Delete)<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 역할-권한 매핑 삭제할 것
     */
    public void deleteRolePermission() {
        this.isDeleted = YNType.Y;
    }
}
