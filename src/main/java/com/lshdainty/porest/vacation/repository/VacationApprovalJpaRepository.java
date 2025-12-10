package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationApproval;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("vacationApprovalJpaRepository")
@RequiredArgsConstructor
public class VacationApprovalJpaRepository implements VacationApprovalRepository {
    private final EntityManager em;

    @Override
    public void save(VacationApproval vacationApproval) {
        em.persist(vacationApproval);
    }

    @Override
    public void saveAll(List<VacationApproval> vacationApprovals) {
        for (VacationApproval va : vacationApprovals) {
            em.persist(va);
        }
    }

    @Override
    public List<VacationApproval> findByVacationGrantId(Long vacationGrantId) {
        return em.createQuery(
                        "select va from VacationApproval va " +
                                "join fetch va.vacationGrant " +
                                "join fetch va.approver " +
                                "where va.vacationGrant.id = :vacationGrantId and va.isDeleted = :isDeleted", VacationApproval.class)
                .setParameter("vacationGrantId", vacationGrantId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<Long> findAllVacationGrantIdsByApproverId(String approverId) {
        return em.createQuery(
                        "select distinct va.vacationGrant.id from VacationApproval va " +
                                "where va.approver.id = :approverId and va.isDeleted = :isDeleted", Long.class)
                .setParameter("approverId", approverId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<Long> findAllVacationGrantIdsByApproverIdAndYear(String approverId, Integer year) {
        return em.createQuery(
                        "select distinct va.vacationGrant.id from VacationApproval va " +
                                "where va.approver.id = :approverId " +
                                "and va.isDeleted = :isDeleted " +
                                "and function('year', va.vacationGrant.createDate) = :year", Long.class)
                .setParameter("approverId", approverId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("year", year)
                .getResultList();
    }

    @Override
    public Optional<VacationApproval> findById(Long id) {
        List<VacationApproval> result = em.createQuery(
                        "select va from VacationApproval va " +
                                "where va.id = :id and va.isDeleted = :isDeleted", VacationApproval.class)
                .setParameter("id", id)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<VacationApproval> findByIdWithVacationGrantAndUser(Long id) {
        List<VacationApproval> result = em.createQuery(
                        "select va from VacationApproval va " +
                                "join fetch va.vacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "join fetch va.approver " +
                                "where va.id = :id and va.isDeleted = :isDeleted", VacationApproval.class)
                .setParameter("id", id)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
