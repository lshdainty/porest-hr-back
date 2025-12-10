package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationUsageDeduction;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.lshdainty.porest.vacation.domain.QVacationUsageDeduction.vacationUsageDeduction;

@Repository
@Primary
@RequiredArgsConstructor
public class VacationUsageDeductionQueryDslRepository implements VacationUsageDeductionRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(VacationUsageDeduction deduction) {
        em.persist(deduction);
    }

    @Override
    public void saveAll(List<VacationUsageDeduction> deductions) {
        for (VacationUsageDeduction deduction : deductions) {
            em.persist(deduction);
        }
    }

    @Override
    public List<VacationUsageDeduction> findByUsageId(Long usageId) {
        return query
                .selectFrom(vacationUsageDeduction)
                .join(vacationUsageDeduction.usage).fetchJoin()
                .join(vacationUsageDeduction.grant).fetchJoin()
                .where(vacationUsageDeduction.usage.id.eq(usageId))
                .fetch();
    }

    @Override
    public List<VacationUsageDeduction> findByGrantId(Long grantId) {
        return query
                .selectFrom(vacationUsageDeduction)
                .join(vacationUsageDeduction.usage).fetchJoin()
                .join(vacationUsageDeduction.grant).fetchJoin()
                .where(vacationUsageDeduction.grant.id.eq(grantId))
                .fetch();
    }

    @Override
    public List<VacationUsageDeduction> findByGrantIds(List<Long> grantIds) {
        if (grantIds == null || grantIds.isEmpty()) {
            return List.of();
        }
        return query
                .selectFrom(vacationUsageDeduction)
                .join(vacationUsageDeduction.usage).fetchJoin()
                .join(vacationUsageDeduction.grant).fetchJoin()
                .where(vacationUsageDeduction.grant.id.in(grantIds)
                        .and(vacationUsageDeduction.usage.isDeleted.eq(YNType.N)))
                .fetch();
    }
}