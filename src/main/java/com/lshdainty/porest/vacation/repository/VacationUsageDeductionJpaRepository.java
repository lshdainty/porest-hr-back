package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationUsageDeduction;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("vacationUsageDeductionJpaRepository")
@RequiredArgsConstructor
public class VacationUsageDeductionJpaRepository implements VacationUsageDeductionRepository {
    private final EntityManager em;

    @Override
    public void saveAll(List<VacationUsageDeduction> deductions) {
        for (VacationUsageDeduction deduction : deductions) {
            em.persist(deduction);
        }
    }

    @Override
    public List<VacationUsageDeduction> findByUsageId(Long usageId) {
        return em.createQuery(
                        "select vud from VacationUsageDeduction vud " +
                                "join fetch vud.usage " +
                                "join fetch vud.grant " +
                                "where vud.usage.id = :usageId", VacationUsageDeduction.class)
                .setParameter("usageId", usageId)
                .getResultList();
    }

    @Override
    public List<VacationUsageDeduction> findByGrantIds(List<Long> grantIds) {
        if (grantIds == null || grantIds.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "select vud from VacationUsageDeduction vud " +
                                "join fetch vud.usage " +
                                "join fetch vud.grant " +
                                "where vud.grant.id in :grantIds and vud.usage.isDeleted = :isDeleted", VacationUsageDeduction.class)
                .setParameter("grantIds", grantIds)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<VacationUsageDeduction> findByUsageIds(List<Long> usageIds) {
        if (usageIds == null || usageIds.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "select vud from VacationUsageDeduction vud " +
                                "join fetch vud.usage " +
                                "join fetch vud.grant " +
                                "where vud.usage.id in :usageIds", VacationUsageDeduction.class)
                .setParameter("usageIds", usageIds)
                .getResultList();
    }
}
