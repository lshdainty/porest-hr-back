package com.lshdainty.porest.department.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.common.type.YNType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // -> protected Order() {}와 동일한 의미 (롬복으로 생성자 막기)
@Table(name = "department")
public class Department extends AuditingFields {
    @Id @GeneratedValue
    @Column(name = "department_id")
    private Long id;        // 부서 아이디

    @Column(name = "department_name")
    private String name;    // 부서명

    @Column(name = "department_name_kr")
    private String nameKR;  // 부서명(국문)

    @Column(name = "parent_department_id", insertable = false, updatable = false)
    private Long parentId;  // 읽기 전용 (부모에 대한 수정은 parent 객체를 통해 진행할 것

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parent;  // 상위 부서

    @Column(name = "head_user_id")
    private String headUserId;

    @Column(name = "department_level")
    private Long level;     // tree 레벨

    @Column(name = "department_desc")
    private String desc;    // 부서 설명

    @Column(name = "color_code")
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Department> children = new ArrayList<>();

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<UserDepartment> userDepartments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted; // 삭제여부

    // company 추가 연관관계 편의 메소드
    public void addCompany(Company company) {
        this.company = company;
        company.getDepartments().add(this);
    }

    // parent 추가 연관관계 편의 메소드
    public void addParent(Department parent) {
        this.parent = parent;
        if  (parent != null) {
            parent.getChildren().add(this);
        }
    }

    /**
     * 부서 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 생성할 것
     *
     * @return Department
     */
    public static Department createDepartment(String name, String nameKR, Department parent, String headUserId, Long level, String desc, String color, Company company) {
        Department department = new Department();
        department.name = name;
        department.nameKR = nameKR;
        department.addParent(parent);
        department.headUserId = headUserId;
        department.level = level;
        department.desc = desc;
        department.color = color;
        department.addCompany(company);
        department.isDeleted = YNType.N;
        return department;
    }

    /**
     * 부서 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 유저 생성할 것
     */
    public void updateDepartment(String name, String nameKR, Department parent, String headUserId, Long level, String desc, String color) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(nameKR)) { this.nameKR = nameKR; }
        if (!Objects.isNull(parent)) { this.changeParent(parent); }
        // 부서장의 경우 잘못 선택한 경우 공란으로 설정가능
        this.headUserId = headUserId;
        if (!Objects.isNull(level)) { this.level = level; }
        if (!Objects.isNull(desc)) { this.desc = desc; }
        // 색상 코드의 경우 잘못 선택한 경우 공란으로 설정가능
        this.color = color;
    }

    /**
     * 부모 부서 변경
     */
    public void changeParent(Department newParent) {
        // 기존 부모에서 제거
        if (this.parent != null) {
            this.parent.getChildren().remove(this);
        }

        // 새로운 부모
        this.addParent(newParent);
    }

    /**
     * 부서 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 부서 삭제할 것
     */
    public void deleteDepartment() {
        this.isDeleted = YNType.Y;
    }
}
