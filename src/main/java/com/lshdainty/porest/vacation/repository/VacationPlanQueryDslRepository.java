package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.QVacationPlan;
import com.lshdainty.porest.vacation.domain.QVacationPlanPolicy;
import com.lshdainty.porest.vacation.domain.QVacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationPlan;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VacationPlan Repository 구현체<br>
 * QueryDSL을 활용한 휴가 플랜 조회 구현
 */
@Repository
@Primary
@RequiredArgsConstructor
public class VacationPlanQueryDslRepository implements VacationPlanRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(VacationPlan vacationPlan) {
        em.persist(vacationPlan);
    }

    @Override
    public Optional<VacationPlan> findByIdWithPolicies(Long id) {
        QVacationPlan plan = QVacationPlan.vacationPlan;
        QVacationPlanPolicy planPolicy = QVacationPlanPolicy.vacationPlanPolicy;
        QVacationPolicy policy = QVacationPolicy.vacationPolicy;

        VacationPlan result = queryFactory
                .selectFrom(plan)
                .leftJoin(plan.vacationPlanPolicies, planPolicy).fetchJoin()
                .leftJoin(planPolicy.vacationPolicy, policy).fetchJoin()
                .where(
                        plan.id.eq(id),
                        plan.isDeleted.eq(YNType.N),
                        planPolicy.isDeleted.eq(YNType.N).or(planPolicy.isNull())
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<VacationPlan> findByCode(String code) {
        QVacationPlan plan = QVacationPlan.vacationPlan;

        VacationPlan result = queryFactory
                .selectFrom(plan)
                .where(
                        plan.code.eq(code),
                        plan.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<VacationPlan> findByCodeWithPolicies(String code) {
        QVacationPlan plan = QVacationPlan.vacationPlan;
        QVacationPlanPolicy planPolicy = QVacationPlanPolicy.vacationPlanPolicy;
        QVacationPolicy policy = QVacationPolicy.vacationPolicy;

        VacationPlan result = queryFactory
                .selectFrom(plan)
                .leftJoin(plan.vacationPlanPolicies, planPolicy).fetchJoin()
                .leftJoin(planPolicy.vacationPolicy, policy).fetchJoin()
                .where(
                        plan.code.eq(code),
                        plan.isDeleted.eq(YNType.N),
                        planPolicy.isDeleted.eq(YNType.N).or(planPolicy.isNull())
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<VacationPlan> findAllWithPolicies() {
        QVacationPlan plan = QVacationPlan.vacationPlan;
        QVacationPlanPolicy planPolicy = QVacationPlanPolicy.vacationPlanPolicy;
        QVacationPolicy policy = QVacationPolicy.vacationPolicy;

        return queryFactory
                .selectFrom(plan)
                .distinct()
                .leftJoin(plan.vacationPlanPolicies, planPolicy).fetchJoin()
                .leftJoin(planPolicy.vacationPolicy, policy).fetchJoin()
                .where(
                        plan.isDeleted.eq(YNType.N),
                        planPolicy.isDeleted.eq(YNType.N).or(planPolicy.isNull())
                )
                .orderBy(plan.code.asc())
                .fetch();
    }

    @Override
    public boolean existsByCode(String code) {
        QVacationPlan plan = QVacationPlan.vacationPlan;

        Integer result = queryFactory
                .selectOne()
                .from(plan)
                .where(
                        plan.code.eq(code),
                        plan.isDeleted.eq(YNType.N)
                )
                .fetchFirst();

        return result != null;
    }
}
