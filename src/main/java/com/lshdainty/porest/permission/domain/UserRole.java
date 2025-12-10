package com.lshdainty.porest.permission.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UserRole Entity<br>
 * 사용자와 역할의 매핑 정보를 관리하는 중간 엔티티<br>
 * 누가 언제 역할을 부여/수정했는지 추적 가능
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_roles")
public class UserRole extends AuditingFields {
    /**
     * 사용자-역할 매핑 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Long id;

    /**
     * 사용자<br>
     * 어떤 사용자에게 역할이 부여되었는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 역할<br>
     * 어떤 역할이 부여되었는지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 사용자-역할 매핑 생성 함수<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 사용자-역할 매핑 생성할 것
     *
     * @param user 사용자
     * @param role 역할
     * @return UserRole
     */
    public static UserRole createUserRole(User user, Role role) {
        UserRole userRole = new UserRole();
        userRole.user = user;
        userRole.role = role;
        userRole.isDeleted = YNType.N;
        return userRole;
    }

    /**
     * 사용자-역할 매핑 삭제 함수 (Soft Delete)<br>
     * Entity의 경우 Setter 없이 Getter만 사용<br>
     * 해당 메소드를 통해 사용자-역할 매핑 삭제할 것
     */
    public void deleteUserRole() {
        this.isDeleted = YNType.Y;
    }

    /**
     * 사용자-역할 매핑 복구 함수 (Soft Delete 복구)<br>
     * Soft Delete된 매핑을 다시 활성화
     */
    public void restoreUserRole() {
        this.isDeleted = YNType.N;
    }
}
