package com.lshdainty.porest.company.repository;

import com.lshdainty.porest.company.domain.Company;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.lshdainty.porest.company.domain.QCompany.company;
import static com.lshdainty.porest.department.domain.QDepartment.department;


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
