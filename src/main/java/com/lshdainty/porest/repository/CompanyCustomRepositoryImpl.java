package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Company;
import com.lshdainty.porest.domain.Department;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.domain.QCompany.company;
import static com.lshdainty.porest.domain.QDepartment.department;

@Repository
@RequiredArgsConstructor
public class CompanyCustomRepositoryImpl implements CompanyCustomRepository {
    private final EntityManager em;

    private final JPAQueryFactory query;

    @Override
    public void save(Company company) {
        em.persist(company);
    }

    @Override
    public Optional<Company> findById(String id) {
        return Optional.ofNullable(query
                .selectFrom(company)
                .where(company.id.eq(id))
                .fetchOne()
        );
    }

    @Override
    public Optional<Company> findByIdWithDepartments(String id) {
        Optional<Company> result =  Optional.ofNullable(query
                .selectFrom(company)
                .leftJoin(company.departments, department).fetchJoin()
                .where(
                        company.id.eq(id),
                        department.parent.isNull().or(department.isNull())
                )
                .distinct()
                .fetchOne()
        );

        if (result.isPresent()) {
            loadAllDepartmentLevels(result.get().getDepartments());
        }

        return result;
    }

    /**
     * 모든 레벨의 부서를 강제로 초기화
     */
    private void loadAllDepartmentLevels(List<Department> departments) {
        for (Department dept : departments) {
            // 지연 로딩 트리거 (BatchSize 효과 활용)
            dept.getChildren().size();
            if (!dept.getChildren().isEmpty()) {
                loadAllDepartmentLevels(dept.getChildren());
            }
        }
    }
}
