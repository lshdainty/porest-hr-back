package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.UserVacationPlan;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserVacationPlan Repository 구현체 (JPQL)<br>
 * JPQL을 활용한 사용자-플랜 매핑 조회 구현 (백업용)
 */
@Repository("userVacationPlanJpaRepository")
@RequiredArgsConstructor
public class UserVacationPlanJpaRepository implements UserVacationPlanRepository {
    private final EntityManager em;

    @Override
    public void save(UserVacationPlan userVacationPlan) {
        em.persist(userVacationPlan);
    }

    @Override
    public List<UserVacationPlan> findByUserIdWithPlanAndPolicies(String userId) {
        return em.createQuery(
                "select distinct uvp from UserVacationPlan uvp " +
                "left join fetch uvp.vacationPlan vp " +
                "left join fetch vp.vacationPlanPolicies vpp " +
                "left join fetch vpp.vacationPolicy p " +
                "where uvp.user.id = :userId and uvp.isDeleted = :isDeleted " +
                "and vp.isDeleted = :isDeleted " +
                "and (vpp.isDeleted = :isDeleted or vpp is null)", UserVacationPlan.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public Optional<UserVacationPlan> findByUserIdAndPlanCode(String userId, String planCode) {
        List<UserVacationPlan> result = em.createQuery(
                "select uvp from UserVacationPlan uvp " +
                "left join fetch uvp.vacationPlan vp " +
                "where uvp.user.id = :userId and vp.code = :planCode " +
                "and uvp.isDeleted = :isDeleted and vp.isDeleted = :isDeleted", UserVacationPlan.class)
                .setParameter("userId", userId)
                .setParameter("planCode", planCode)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public boolean existsByUserIdAndPlanCode(String userId, String planCode) {
        List<UserVacationPlan> result = em.createQuery(
                "select uvp from UserVacationPlan uvp " +
                "inner join uvp.vacationPlan vp " +
                "where uvp.user.id = :userId and vp.code = :planCode " +
                "and uvp.isDeleted = :isDeleted and vp.isDeleted = :isDeleted", UserVacationPlan.class)
                .setParameter("userId", userId)
                .setParameter("planCode", planCode)
                .setParameter("isDeleted", YNType.N)
                .setMaxResults(1)
                .getResultList();
        return !result.isEmpty();
    }
}
