package com.lshdainty.porest.permission.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Permission Entity<br>
 * 권한 정보를 관리하는 엔티티<br>
 * RBAC(Role-Based Access Control) 기반의 세부 권한 관리
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "permissions")
public class Permission extends AuditingFields {

    /**
     * 권한 이름 (Primary Key)<br>
     * 예: VACATION_VIEW, VACATION_CREATE, VACATION_APPROVE
     */
    @Id
    @Column(name = "permission_name")
    private String name;

    /**
     * 권한 설명<br>
     * 권한에 대한 상세 설명
     */
    @Column(name = "description")
    private String description;

    /**
     * 리소스<br>
     * 권한이 적용되는 대상 리소스<br>
     * 예: VACATION, USER, DEPARTMENT
     */
    @Column(name = "resource")
    private String resource;

    /**
     * 액션<br>
     * 리소스에 대해 수행할 수 있는 작업<br>
     * 예: VIEW, CREATE, UPDATE, DELETE, APPROVE
     */
    @Column(name = "action")
    private String action;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;

    /**
     * 권한 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 권한 생성할 것
     *
     * @param name 권한 이름
     * @param description 권한 설명
     * @param resource 리소스
     * @param action 액션
     * @return Permission
     */
    public static Permission createPermission(String name, String description, String resource, String action) {
        Permission permission = new Permission();
        permission.name = name;
        permission.description = description;
        permission.resource = resource;
        permission.action = action;
        permission.isDeleted = YNType.N;
        return permission;
    }

    /**
     * 권한 수정 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 권한 수정할 것
     *
     * @param description 권한 설명
     * @param resource 리소스
     * @param action 액션
     */
    public void updatePermission(String description, String resource, String action) {
        if (!Objects.isNull(description)) { this.description = description; }
        if (!Objects.isNull(resource)) { this.resource = resource; }
        if (!Objects.isNull(action)) { this.action = action; }
    }

    /**
     * 권한 삭제 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 권한 삭제할 것 (Soft Delete)
     */
    public void deletePermission() {
        this.isDeleted = YNType.Y;
    }
}
