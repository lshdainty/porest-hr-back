package com.porest.hr.work.repository;

import com.porest.hr.work.domain.WorkSystemLog;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("workSystemLogJpaRepository")
@RequiredArgsConstructor
public class WorkSystemLogJpaRepository implements WorkSystemLogRepository {
    private final EntityManager em;

    @Override
    public void save(WorkSystemLog log) {
        em.persist(log);
    }

    @Override
    public Optional<WorkSystemLog> findByPeriodAndCode(LocalDateTime startDateTime, LocalDateTime endDateTime, String code) {
        List<WorkSystemLog> result = em.createQuery(
                        "select wsl from WorkSystemLog wsl " +
                                "where wsl.createAt >= :startDateTime " +
                                "and wsl.createAt < :endDateTime " +
                                "and wsl.code = :code", WorkSystemLog.class)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .setParameter("code", code)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<String> findCodesByPeriodAndCodes(LocalDateTime startDateTime, LocalDateTime endDateTime, List<String> codes) {
        return em.createQuery(
                        "select wsl.code from WorkSystemLog wsl " +
                                "where wsl.createAt >= :startDateTime " +
                                "and wsl.createAt < :endDateTime " +
                                "and wsl.code in :codes", String.class)
                .setParameter("startDateTime", startDateTime)
                .setParameter("endDateTime", endDateTime)
                .setParameter("codes", codes)
                .getResultList();
    }

    @Override
    public void delete(WorkSystemLog log) {
        em.remove(log);
    }
}
