package com.porest.hr.company.domain;

import com.porest.hr.common.domain.AuditingFieldsWithIp;
import com.porest.hr.department.domain.Department;
import com.porest.core.type.YNType;
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
@Table(name = "company")
public class Company extends AuditingFieldsWithIp {
    /**
     * 회사 순번<br>
     * 테이블 관리용 Primary Key (자동 생성)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_no")
    private Long no;

    /**
     * 회사 아이디<br>
     * 비즈니스 식별자 (고유값)
     */
    @Column(name = "company_id", length = 50, unique = true, nullable = false)
    private String id;

    /**
     * 회사명<br>
     * 회사의 이름
     */
    @Column(name = "company_name", nullable = false, length = 100)
    private String name;

    /**
     * 회사 설명<br>
     * 회사에 대한 상세 설명
     */
    @Column(name = "company_desc", length = 1000)
    private String desc;

    /**
     * 삭제 여부<br>
     * Soft delete를 위한 플래그
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted", nullable = false, length = 1)
    private YNType isDeleted;

    /**
     * 부서 목록<br>
     * 회사에 속한 부서 목록
     */
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Department> departments = new ArrayList<>();

    /**
     * 회사 생성 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 회사 생성할 것
     *
     * @return Company
     */
    public static Company createCompany(String id, String name, String desc) {
        Company company = new Company();
        company.id = id;
        company.name = name;
        company.desc = desc;
        company.isDeleted = YNType.N;
        return company;
    }

    /**
     * 회사 수정 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 회사 수정할 것
     */
    public void updateCompany(String name, String desc) {
        if (!Objects.isNull(name)) { this.name = name; }
        if (!Objects.isNull(desc)) { this.desc = desc; }
    }

    /**
     * 회사 삭제 함수<br>
     * Entity의 경우 Setter없이 Getter만 사용<br>
     * 해당 메소드를 통해 회사 삭제할 것
     */
    public void deleteCompany() {
        this.isDeleted = YNType.Y;
    }
}
