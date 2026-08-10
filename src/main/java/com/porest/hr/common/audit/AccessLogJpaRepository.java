package com.porest.hr.common.audit;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AccessLog JPQL Repository 구현체 (백업용)<br>
 * QueryDSL 장애 시 대비
 *
 * @see AccessLogRepository 인터페이스
 * @see AccessLogQueryDslRepository QueryDSL 구현체 ({@code @Primary})
 */
@Repository("accessLogJpaRepository")
@RequiredArgsConstructor
public class AccessLogJpaRepository implements AccessLogRepository {

    private final EntityManager em;

    @Override
    public void save(AccessLog accessLog) {
        em.persist(accessLog);
    }

    @Override
    public List<AccessLog> findByActor(String actorId, int limit) {
        return em.createQuery(
                        "SELECT a FROM AccessLog a WHERE a.actorId = :actorId ORDER BY a.createAt DESC",
                        AccessLog.class)
                .setParameter("actorId", actorId)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<AccessLog> findByTarget(String targetType, String targetId, int limit) {
        return em.createQuery(
                        "SELECT a FROM AccessLog a "
                                + "WHERE a.targetType = :targetType AND a.targetId = :targetId "
                                + "ORDER BY a.createAt DESC",
                        AccessLog.class)
                .setParameter("targetType", targetType)
                .setParameter("targetId", targetId)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<AccessLog> findByPeriod(LocalDateTime from, LocalDateTime to, int limit) {
        return em.createQuery(
                        "SELECT a FROM AccessLog a WHERE a.createAt BETWEEN :from AND :to "
                                + "ORDER BY a.createAt DESC",
                        AccessLog.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(limit)
                .getResultList();
    }
}
