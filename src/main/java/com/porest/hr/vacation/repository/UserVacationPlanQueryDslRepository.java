package com.porest.hr.vacation.repository;

import com.porest.core.type.YNType;
import com.porest.hr.vacation.domain.QUserVacationPlan;
import com.porest.hr.vacation.domain.QVacationPlan;
import com.porest.hr.vacation.domain.QVacationPlanPolicy;
import com.porest.hr.vacation.domain.QVacationPolicy;
import com.porest.hr.vacation.domain.UserVacationPlan;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserVacationPlan Repository 구현체<br>
 * QueryDSL을 활용한 사용자-플랜 매핑 조회 구현
 */
@Repository
@Primary
@RequiredArgsConstructor
public class UserVacationPlanQueryDslRepository implements UserVacationPlanRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(UserVacationPlan userVacationPlan) {
        em.persist(userVacationPlan);
    }

    @Override
    public List<UserVacationPlan> findByUserIdWithPlanAndPolicies(String userId) {
        QUserVacationPlan userPlan = QUserVacationPlan.userVacationPlan;
        QVacationPlan plan = QVacationPlan.vacationPlan;
        QVacationPlanPolicy planPolicy = QVacationPlanPolicy.vacationPlanPolicy;
        QVacationPolicy policy = QVacationPolicy.vacationPolicy;

        return queryFactory
                .selectFrom(userPlan)
                .distinct()
                .leftJoin(userPlan.vacationPlan, plan).fetchJoin()
                .leftJoin(plan.vacationPlanPolicies, planPolicy).fetchJoin()
                .leftJoin(planPolicy.vacationPolicy, policy).fetchJoin()
                .where(
                        userPlan.user.id.eq(userId),
                        userPlan.isDeleted.eq(YNType.N),
                        plan.isDeleted.eq(YNType.N),
                        planPolicy.isDeleted.eq(YNType.N).or(planPolicy.isNull())
                )
                .fetch();
    }

    @Override
    public Optional<UserVacationPlan> findByUserIdAndPlanCode(String userId, String planCode) {
        QUserVacationPlan userPlan = QUserVacationPlan.userVacationPlan;
        QVacationPlan plan = QVacationPlan.vacationPlan;

        UserVacationPlan result = queryFactory
                .selectFrom(userPlan)
                .leftJoin(userPlan.vacationPlan, plan).fetchJoin()
                .where(
                        userPlan.user.id.eq(userId),
                        plan.code.eq(planCode),
                        userPlan.isDeleted.eq(YNType.N),
                        plan.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByUserIdAndPlanCode(String userId, String planCode) {
        QUserVacationPlan userPlan = QUserVacationPlan.userVacationPlan;
        QVacationPlan plan = QVacationPlan.vacationPlan;

        Integer result = queryFactory
                .selectOne()
                .from(userPlan)
                .innerJoin(userPlan.vacationPlan, plan)
                .where(
                        userPlan.user.id.eq(userId),
                        plan.code.eq(planCode),
                        userPlan.isDeleted.eq(YNType.N),
                        plan.isDeleted.eq(YNType.N)
                )
                .fetchFirst();

        return result != null;
    }
}
