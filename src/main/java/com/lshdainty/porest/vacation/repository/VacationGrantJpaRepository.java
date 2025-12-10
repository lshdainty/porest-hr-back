package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.GrantStatus;
import com.lshdainty.porest.vacation.type.VacationType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("vacationGrantJpaRepository")
@RequiredArgsConstructor
public class VacationGrantJpaRepository implements VacationGrantRepository {
    private final EntityManager em;

    @Override
    public void save(VacationGrant vacationGrant) {
        em.persist(vacationGrant);
    }

    @Override
    public void saveAll(List<VacationGrant> vacationGrants) {
        for (VacationGrant vg : vacationGrants) {
            em.persist(vg);
        }
    }

    @Override
    public List<VacationGrant> findByUserId(String userId) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.status = :status " +
                                "order by vg.grantDate asc", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("status", GrantStatus.ACTIVE)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findByUserIdAndYear(String userId, int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.status in :statuses " +
                                "and vg.grantDate <= :endOfYear " +
                                "and vg.expiryDate >= :startOfYear " +
                                "order by vg.grantDate asc", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("statuses", List.of(GrantStatus.ACTIVE, GrantStatus.EXHAUSTED, GrantStatus.EXPIRED))
                .setParameter("startOfYear", startOfYear)
                .setParameter("endOfYear", endOfYear)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findByPolicyId(Long policyId) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.policy.id = :policyId and vg.isDeleted = :isDeleted", VacationGrant.class)
                .setParameter("policyId", policyId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findAvailableGrantsByUserIdOrderByExpiryDate(String userId) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.status = :status " +
                                "and vg.remainTime > :zero " +
                                "order by vg.expiryDate asc, vg.grantDate asc", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("status", GrantStatus.ACTIVE)
                .setParameter("zero", BigDecimal.ZERO)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findAvailableGrantsByUserIdAndTypeAndDate(String userId, VacationType vacationType, LocalDateTime usageStartDate) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.status = :status " +
                                "and vg.type = :vacationType " +
                                "and vg.remainTime > :zero " +
                                "and vg.grantDate <= :usageStartDate " +
                                "and vg.expiryDate >= :usageStartDate " +
                                "order by vg.expiryDate asc, vg.grantDate asc", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("status", GrantStatus.ACTIVE)
                .setParameter("vacationType", vacationType)
                .setParameter("zero", BigDecimal.ZERO)
                .setParameter("usageStartDate", usageStartDate)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findAllWithUser() {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.isDeleted = :isDeleted and vg.status = :status " +
                                "order by vg.user.id asc, vg.expiryDate asc", VacationGrant.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("status", GrantStatus.ACTIVE)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findAvailableGrantsByUserIdAndDate(String userId, LocalDateTime usageStartDate) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.status = :status " +
                                "and vg.remainTime > :zero " +
                                "and vg.grantDate <= :usageStartDate " +
                                "and vg.expiryDate >= :usageStartDate " +
                                "order by vg.expiryDate asc, vg.grantDate asc", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("status", GrantStatus.ACTIVE)
                .setParameter("zero", BigDecimal.ZERO)
                .setParameter("usageStartDate", usageStartDate)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findValidGrantsByUserIdAndBaseTime(String userId, LocalDateTime baseTime) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.status in :statuses " +
                                "and vg.grantDate <= :baseTime " +
                                "and vg.expiryDate >= :baseTime", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("statuses", List.of(GrantStatus.ACTIVE, GrantStatus.EXHAUSTED, GrantStatus.EXPIRED))
                .setParameter("baseTime", baseTime)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findExpiredTargets(LocalDateTime currentDate) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.isDeleted = :isDeleted " +
                                "and vg.status = :status " +
                                "and vg.expiryDate < :currentDate", VacationGrant.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("status", GrantStatus.ACTIVE)
                .setParameter("currentDate", currentDate)
                .getResultList();
    }

    @Override
    public Optional<VacationGrant> findById(Long id) {
        List<VacationGrant> result = em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.id = :id", VacationGrant.class)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<VacationGrant> findAllRequestedVacationsByUserId(String userId) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.policy.grantMethod = :grantMethod " +
                                "order by vg.requestStartTime desc", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("grantMethod", GrantMethod.ON_REQUEST)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findAllRequestedVacationsByUserIdAndYear(String userId, Integer year) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.user.id = :userId " +
                                "and vg.isDeleted = :isDeleted " +
                                "and vg.policy.grantMethod = :grantMethod " +
                                "and function('year', vg.createDate) = :year " +
                                "order by vg.requestStartTime desc", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("grantMethod", GrantMethod.ON_REQUEST)
                .setParameter("year", year)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findByIdsWithUserAndPolicy(List<Long> vacationGrantIds) {
        if (vacationGrantIds == null || vacationGrantIds.isEmpty()) {
            return List.of();
        }

        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "join fetch vg.user " +
                                "join fetch vg.policy " +
                                "where vg.id in :ids and vg.isDeleted = :isDeleted " +
                                "order by vg.createDate desc", VacationGrant.class)
                .setParameter("ids", vacationGrantIds)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findByUserIdAndValidPeriod(String userId, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "where vg.user.id = :userId " +
                                "and vg.grantDate <= :endOfPeriod " +
                                "and vg.expiryDate >= :startOfPeriod " +
                                "and vg.status in :statuses " +
                                "and vg.isDeleted = :isDeleted", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("startOfPeriod", startOfPeriod)
                .setParameter("endOfPeriod", endOfPeriod)
                .setParameter("statuses", List.of(GrantStatus.ACTIVE, GrantStatus.EXHAUSTED))
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<VacationGrant> findByUserIdAndStatusesAndPeriod(String userId, List<GrantStatus> statuses, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {
        return em.createQuery(
                        "select vg from VacationGrant vg " +
                                "where vg.user.id = :userId " +
                                "and vg.status in :statuses " +
                                "and vg.requestStartTime between :startOfPeriod and :endOfPeriod " +
                                "and vg.isDeleted = :isDeleted", VacationGrant.class)
                .setParameter("userId", userId)
                .setParameter("statuses", statuses)
                .setParameter("startOfPeriod", startOfPeriod)
                .setParameter("endOfPeriod", endOfPeriod)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }
}
