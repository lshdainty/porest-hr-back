package com.lshdainty.porest.company.domain;

import com.lshdainty.porest.common.domain.AuditingFields;
import com.lshdainty.porest.department.domain.Department;
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
@Table(name = "company")
public class Company extends AuditingFields {
    @Id
    @Column(name = "company_id")
    private String id; // 회사 아이디

    @Column(name = "company_name")
    private String name; // 회사명

    @Column(name = "company_desc")
    private String desc; // 회사 설명

    @Enumerated(EnumType.STRING)
    @Column(name = "is_deleted")
    private YNType isDeleted; // 삭제여부

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)   // JPA에서는 mappedBy는 읽기 전용
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
