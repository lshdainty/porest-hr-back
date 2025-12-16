package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Repository("workHistoryJpaRepository")
@RequiredArgsConstructor
public class WorkHistoryJpaRepository implements WorkHistoryRepository {
    private final EntityManager em;

    @Override
    public void save(WorkHistory workHistory) {
        em.persist(workHistory);
    }

    @Override
    public void saveAll(List<WorkHistory> workHistories) {
        for (WorkHistory workHistory : workHistories) {
            em.persist(workHistory);
        }
    }

    @Override
    public Optional<WorkHistory> findById(Long id) {
        List<WorkHistory> result = em.createQuery(
                        "select wh from WorkHistory wh " +
                                "join fetch wh.user " +
                                "left join fetch wh.group " +
                                "left join fetch wh.part " +
                                "left join fetch wh.division " +
                                "where wh.id = :id", WorkHistory.class)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<WorkHistory> findAll(WorkHistorySearchCondition condition) {
        StringBuilder jpql = new StringBuilder(
                "select wh from WorkHistory wh " +
                        "join fetch wh.user " +
                        "left join fetch wh.group " +
                        "left join fetch wh.part " +
                        "left join fetch wh.division " +
                        "where wh.isDeleted = :isDeleted and wh.user.company != :systemCompany");

        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", YNType.N);
        params.put("systemCompany", OriginCompanyType.SYSTEM);

        appendConditions(jpql, params, condition);

        if ("OLDEST".equalsIgnoreCase(condition.getSortType())) {
            jpql.append(" order by wh.date asc, wh.id desc");
        } else {
            jpql.append(" order by wh.date desc, wh.id desc");
        }

        TypedQuery<WorkHistory> query = em.createQuery(jpql.toString(), WorkHistory.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    @Override
    public void delete(WorkHistory workHistory) {
        workHistory.deleteWorkHistory();
    }

    @Override
    public Stream<WorkHistory> findAllStream(WorkHistorySearchCondition condition) {
        return findAll(condition).stream();
    }

    @Override
    public List<WorkHistory> findByUserAndDate(String userId, LocalDate date) {
        return em.createQuery(
                        "select wh from WorkHistory wh " +
                                "where wh.user.id = :userId " +
                                "and wh.date = :date " +
                                "and wh.isDeleted = :isDeleted", WorkHistory.class)
                .setParameter("userId", userId)
                .setParameter("date", date)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public Map<LocalDate, BigDecimal> findDailyWorkHoursByUserAndPeriod(String userId, LocalDate startDate, LocalDate endDate) {
        List<WorkHistory> histories = em.createQuery(
                        "select wh from WorkHistory wh " +
                                "where wh.user.id = :userId " +
                                "and wh.date between :startDate and :endDate " +
                                "and wh.isDeleted = :isDeleted", WorkHistory.class)
                .setParameter("userId", userId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("isDeleted", YNType.N)
                .getResultList();

        Map<LocalDate, BigDecimal> dailyHoursMap = new HashMap<>();
        for (WorkHistory history : histories) {
            LocalDate date = history.getDate();
            BigDecimal hours = history.getHours() != null ? history.getHours() : BigDecimal.ZERO;
            dailyHoursMap.merge(date, hours, BigDecimal::add);
        }
        return dailyHoursMap;
    }

    @Override
    public Map<String, Map<LocalDate, BigDecimal>> findDailyWorkHoursByUsersAndPeriod(List<String> userIds, LocalDate startDate, LocalDate endDate) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        List<WorkHistory> histories = em.createQuery(
                        "select wh from WorkHistory wh " +
                                "where wh.user.id in :userIds " +
                                "and wh.date between :startDate and :endDate " +
                                "and wh.isDeleted = :isDeleted", WorkHistory.class)
                .setParameter("userIds", userIds)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("isDeleted", YNType.N)
                .getResultList();

        Map<String, Map<LocalDate, BigDecimal>> result = new HashMap<>();
        for (WorkHistory history : histories) {
            String usrId = history.getUser().getId();
            LocalDate date = history.getDate();
            BigDecimal hours = history.getHours() != null ? history.getHours() : BigDecimal.ZERO;

            result.computeIfAbsent(usrId, k -> new HashMap<>())
                    .merge(date, hours, BigDecimal::add);
        }
        return result;
    }

    private void appendConditions(StringBuilder jpql, Map<String, Object> params, WorkHistorySearchCondition condition) {
        if (condition.getStartDate() != null && condition.getEndDate() != null) {
            jpql.append(" and wh.date between :startDate and :endDate");
            params.put("startDate", condition.getStartDate());
            params.put("endDate", condition.getEndDate());
        } else if (condition.getStartDate() != null) {
            jpql.append(" and wh.date >= :startDate");
            params.put("startDate", condition.getStartDate());
        } else if (condition.getEndDate() != null) {
            jpql.append(" and wh.date <= :endDate");
            params.put("endDate", condition.getEndDate());
        }

        if (StringUtils.hasText(condition.getUserName())) {
            jpql.append(" and wh.user.name like :userName");
            params.put("userName", "%" + condition.getUserName() + "%");
        }

        if (StringUtils.hasText(condition.getUserId())) {
            jpql.append(" and wh.user.id = :userId");
            params.put("userId", condition.getUserId());
        }

        if (condition.getGroupSeq() != null) {
            jpql.append(" and wh.group.id = :groupSeq");
            params.put("groupSeq", condition.getGroupSeq());
        }

        if (condition.getPartSeq() != null) {
            jpql.append(" and wh.part.id = :partSeq");
            params.put("partSeq", condition.getPartSeq());
        }

        if (condition.getDivisionSeq() != null) {
            jpql.append(" and wh.division.id = :divisionSeq");
            params.put("divisionSeq", condition.getDivisionSeq());
        }
    }
}
