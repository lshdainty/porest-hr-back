package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("vacationUsageJpaRepository")
@RequiredArgsConstructor
public class VacationUsageJpaRepository implements VacationUsageRepository {
    private final EntityManager em;

    @Override
    public void save(VacationUsage vacationUsage) {
        em.persist(vacationUsage);
    }

    @Override
    public void saveAll(List<VacationUsage> vacationUsages) {
        for (VacationUsage vu : vacationUsages) {
            em.persist(vu);
        }
    }

    @Override
    public Optional<VacationUsage> findById(Long vacationUsageId) {
        List<VacationUsage> result = em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user " +
                                "where vu.id = :id", VacationUsage.class)
                .setParameter("id", vacationUsageId)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<VacationUsage> findByUserId(String userId) {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user " +
                                "where vu.user.id = :userId and vu.isDeleted = :isDeleted " +
                                "order by vu.startDate asc", VacationUsage.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findByUserIdAndYear(String userId, int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user " +
                                "where vu.user.id = :userId " +
                                "and vu.isDeleted = :isDeleted " +
                                "and vu.startDate between :startOfYear and :endOfYear " +
                                "order by vu.startDate asc", VacationUsage.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("startOfYear", startOfYear)
                .setParameter("endOfYear", endOfYear)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findAllWithUser() {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user u " +
                                "where vu.isDeleted = :isDeleted and u.company != :systemCompany " +
                                "order by vu.user.id asc, vu.startDate desc", VacationUsage.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("systemCompany", OriginCompanyType.SYSTEM)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findByPeriodWithUser(LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user u " +
                                "where vu.isDeleted = :isDeleted " +
                                "and u.company != :systemCompany " +
                                "and vu.startDate >= :startDate " +
                                "and vu.startDate <= :endDate " +
                                "order by vu.startDate asc", VacationUsage.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("systemCompany", OriginCompanyType.SYSTEM)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findByUserIdAndPeriodWithUser(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user " +
                                "where vu.user.id = :userId " +
                                "and vu.isDeleted = :isDeleted " +
                                "and vu.startDate >= :startDate " +
                                "and vu.startDate <= :endDate " +
                                "order by vu.startDate asc", VacationUsage.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findUsedByUserIdAndBaseTime(String userId, LocalDateTime baseTime) {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user " +
                                "where vu.user.id = :userId " +
                                "and vu.isDeleted = :isDeleted " +
                                "and vu.startDate <= :baseTime", VacationUsage.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("baseTime", baseTime)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findExpectedByUserIdAndBaseTime(String userId, LocalDateTime baseTime) {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "join fetch vu.user " +
                                "where vu.user.id = :userId " +
                                "and vu.isDeleted = :isDeleted " +
                                "and vu.startDate > :baseTime", VacationUsage.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .setParameter("baseTime", baseTime)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findByUserIdAndPeriod(String userId, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "where vu.user.id = :userId " +
                                "and vu.startDate between :startOfPeriod and :endOfPeriod " +
                                "and vu.isDeleted = :isDeleted", VacationUsage.class)
                .setParameter("userId", userId)
                .setParameter("startOfPeriod", startOfPeriod)
                .setParameter("endOfPeriod", endOfPeriod)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findByUserIdAndPeriodForDaily(String userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "where vu.user.id = :userId " +
                                "and vu.startDate >= :startDateTime " +
                                "and vu.startDate < :endDateTime " +
                                "and vu.isDeleted = :isDeleted", VacationUsage.class)
                .setParameter("userId", userId)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<VacationUsage> findByUserIdsAndPeriodForDaily(List<String> userIds, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return em.createQuery(
                        "select vu from VacationUsage vu " +
                                "where vu.user.id in :userIds " +
                                "and vu.startDate >= :startDateTime " +
                                "and vu.startDate < :endDateTime " +
                                "and vu.isDeleted = :isDeleted", VacationUsage.class)
                .setParameter("userIds", userIds)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }
}
