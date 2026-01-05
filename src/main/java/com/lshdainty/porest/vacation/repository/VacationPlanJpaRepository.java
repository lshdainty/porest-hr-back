package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationPlan;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VacationPlan Repository 구현체 (JPQL)<br>
 * JPQL을 활용한 휴가 플랜 조회 구현 (백업용)
 */
@Repository("vacationPlanJpaRepository")
@RequiredArgsConstructor
public class VacationPlanJpaRepository implements VacationPlanRepository {
    private final EntityManager em;

    @Override
    public void save(VacationPlan vacationPlan) {
        em.persist(vacationPlan);
    }

    @Override
    public Optional<VacationPlan> findByIdWithPolicies(Long id) {
        List<VacationPlan> result = em.createQuery(
                "select distinct vp from VacationPlan vp " +
                "left join fetch vp.vacationPlanPolicies vpp " +
                "left join fetch vpp.vacationPolicy p " +
                "where vp.id = :id and vp.isDeleted = :isDeleted " +
                "and (vpp.isDeleted = :isDeleted or vpp is null)", VacationPlan.class)
                .setParameter("id", id)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<VacationPlan> findByCode(String code) {
        List<VacationPlan> result = em.createQuery(
                "select vp from VacationPlan vp where vp.code = :code and vp.isDeleted = :isDeleted", VacationPlan.class)
                .setParameter("code", code)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<VacationPlan> findByCodeWithPolicies(String code) {
        List<VacationPlan> result = em.createQuery(
                "select distinct vp from VacationPlan vp " +
                "left join fetch vp.vacationPlanPolicies vpp " +
                "left join fetch vpp.vacationPolicy p " +
                "where vp.code = :code and vp.isDeleted = :isDeleted " +
                "and (vpp.isDeleted = :isDeleted or vpp is null)", VacationPlan.class)
                .setParameter("code", code)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<VacationPlan> findAllWithPolicies() {
        return em.createQuery(
                "select distinct vp from VacationPlan vp " +
                "left join fetch vp.vacationPlanPolicies vpp " +
                "left join fetch vpp.vacationPolicy p " +
                "where vp.isDeleted = :isDeleted " +
                "and (vpp.isDeleted = :isDeleted or vpp is null) " +
                "order by vp.code asc", VacationPlan.class)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public boolean existsByCode(String code) {
        List<VacationPlan> result = em.createQuery(
                "select vp from VacationPlan vp where vp.code = :code and vp.isDeleted = :isDeleted", VacationPlan.class)
                .setParameter("code", code)
                .setParameter("isDeleted", YNType.N)
                .setMaxResults(1)
                .getResultList();
        return !result.isEmpty();
    }
}
