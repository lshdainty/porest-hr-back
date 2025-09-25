package com.lshdainty.porest.department.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.department.domain.Department;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.lshdainty.porest.department.domain.QDepartment.department;

@Repository
@RequiredArgsConstructor
public class DepartmentCustomRepositoryImpl implements DepartmentCustomRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(Department department) {
        em.persist(department);
    }

    @Override
    public Optional<Department> findById(Long id) {
        return Optional.ofNullable(query
                .selectFrom(department)
                .where(
                        department.id.eq(id),
                        department.delYN.eq(YNType.N)
                )
                .fetchOne()
        );
    }

    @Override
    public Optional<Department> findByIdWithChildren(Long id) {
        return Optional.ofNullable(query
                .selectFrom(department)
                .leftJoin(department.children).fetchJoin()
                .where(
                        department.id.eq(id),
                        department.delYN.eq(YNType.N)
                )
                .distinct()
                .fetchOne()
        );
    }

    @Override
    public boolean hasActiveChildren(Long departmentId) {
        Long count = query
                .select(department.count())
                .from(department)
                .where(
                        department.parent.id.eq(departmentId),
                        department.delYN.eq(YNType.N)
                )
                .fetchOne();

        return count != null && count > 0;
    }
}
