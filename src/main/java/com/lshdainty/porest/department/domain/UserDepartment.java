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
    @Id @GeneratedValue
    @Column(name = "user_department_id")
    private Long id;                // seq

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;              // 유저 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;  // 유저가 속한 부서 정보

    @Enumerated(EnumType.STRING)
    @Column(name = "main_department_yn")
    private YNType mainYN;          // 메인 부서 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted;           // 삭제여부

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
