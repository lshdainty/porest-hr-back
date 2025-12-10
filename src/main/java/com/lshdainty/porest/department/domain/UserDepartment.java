package com.lshdainty.porest.department.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "user_department")
public class UserDepartment extends AuditingFields {
    /**
     * 사용자-부서 매핑 아이디<br>
     * 테이블 관리용 seq
     */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_department_id", columnDefinition = "bigint(20) COMMENT '사용자-부서 매핑 아이디'")
    private Long id;

    /**
     * 사용자 객체<br>
     * 테이블 컬럼은 user_id<br>
     * 어떤 유저가 부서에 속해 있는지 알기 위해 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 부서 객체<br>
     * 테이블 컬럼은 department_id<br>
     * 유저가 속한 부서 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * 메인 부서 여부<br>
     * 유저가 여러 부서에 속할 경우 대표 부서를 구분하기 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "main_department_yn", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '메인 부서 여부'")
    private YNType mainYN;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1, columnDefinition = "varchar(1) DEFAULT 'N' NOT NULL COMMENT '삭제 여부'")
    private YNType isDeleted;

    // user 추가 연관관계 편의 메소드
    public void addUser(User user) {
        this.user = user;
        user.getUserDepartments().add(this);
    }

    // department 추가 연관관계 편의 메소드
    public void addDepartment(Department department) {
        this.department = department;
        department.getUserDepartments().add(this);
    }

    /**
     * 유저 부서 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 부서 생성할 것
     *
     * @return UserDepartment
     */
    public static UserDepartment createUserDepartment(User user, Department department, YNType mainYN) {
        UserDepartment userDepartment = new UserDepartment();
        userDepartment.addUser(user);
        userDepartment.addDepartment(department);
        userDepartment.mainYN = mainYN;
        userDepartment.isDeleted = YNType.N;
        return userDepartment;
    }

    /**
     * 유저 부서 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 부서 삭제할 것
     */
    public void deleteUserDepartment() {
        this.mainYN = YNType.N;
        this.isDeleted = YNType.Y;
    }
}
