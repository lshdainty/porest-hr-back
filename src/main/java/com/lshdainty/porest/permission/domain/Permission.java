package com.lshdainty.porest.permission.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
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
     * 권한 아이디<br>
     * 자동 생성되는 고유 식별자
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id", columnDefinition = "bigint(20) COMMENT '권한 아이디'")
    private Long id;

    /**
     * 권한 코드<br>
     * 예: USER:READ, VACATION:REQUEST
     */
    @Column(name = "permission_code", unique = true, nullable = false, length = 100, columnDefinition = "varchar(100) NOT NULL COMMENT '권한 코드'")
    private String code;

    /**
     * 권한 이름<br>
     * 예: 사용자 조회, 휴가 신청
     */
    @Column(name = "permission_name", length = 20, columnDefinition = "varchar(20) COMMENT '권한 이름'")
    private String name;

    /**
     * 권한 설명<br>
     * 권한에 대한 상세 설명
     */
    @Column(name = "permission_desc", length = 1000, columnDefinition = "varchar(1000) COMMENT '권한 설명'")
    private String desc;

    /**
     * 리소스 타입<br>
     * 권한이 적용되는 대상 리소스<br>
     * 예: USER, VACATION, WORK, SCHEDULE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource", nullable = false, length = 15, columnDefinition = "varchar(15) NOT NULL COMMENT '리소스 타입'")
    private ResourceType resource;

    /**
     * 액션 타입<br>
     * 리소스에 대해 수행할 수 있는 작업<br>
     * 예: READ, WRITE, MANAGE, APPROVE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 15, columnDefinition = "varchar(15) NOT NULL COMMENT '액션 타입'")
    private ActionType action;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '삭제 여부'")
    private YNType isDeleted;

    /**
     * 권한 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 권한 생성할 것
     *
     * @param code 권한 코드
     * @param name 권한 이름 (한글명)
     * @param desc 권한 설명
     * @param resource 리소스
     * @param action 액션
     * @return Permission
     */
    public static Permission createPermission(String code, String name, String desc, ResourceType resource, ActionType action) {
        Permission permission = new Permission();
        permission.code = code;
        permission.name = name;
        permission.desc = desc;
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
     * @param name 권한 이름 (한글명)
     * @param desc 권한 설명
     * @param resource 리소스
     * @param action 액션
     */
    public void updatePermission(String name, String desc, ResourceType resource, ActionType action) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(desc)) { this.desc = desc; }
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
