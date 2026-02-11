package com.porest.hr.company.repository;

import com.porest.hr.company.domain.Company;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.porest.hr.company.domain.QCompany.company;
import static com.porest.hr.department.domain.QDepartment.department;


@Repository
@Primary
@RequiredArgsConstructor
public class CompanyQueryDslRepository implements CompanyRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(Company company) {
        em.persist(company);
    }

    @Override
    public Optional<Company> find() {
        return Optional.ofNullable(query
                .selectFrom(company)
                .fetchFirst()
        );
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
        return Optional.ofNullable(query
                .selectFrom(company)
                .leftJoin(company.departments, department).fetchJoin()
                .where(
                        company.id.eq(id),
                        department.parent.isNull().or(department.isNull())
                )
                .distinct()
                .fetchOne()
        );
    }
}
