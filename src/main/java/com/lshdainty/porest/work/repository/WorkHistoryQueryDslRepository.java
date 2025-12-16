package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.lshdainty.porest.work.domain.QWorkHistory.workHistory;

@Repository
@Primary
@RequiredArgsConstructor
public class WorkHistoryQueryDslRepository implements WorkHistoryRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

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
        return Optional.ofNullable(query
                .selectFrom(workHistory)
                .join(workHistory.user).fetchJoin()
                .leftJoin(workHistory.group).fetchJoin()
                .leftJoin(workHistory.part).fetchJoin()
                .leftJoin(workHistory.division).fetchJoin()
                .where(workHistory.id.eq(id))
                .fetchOne());
    }

    @Override
    public List<WorkHistory> findAll(WorkHistorySearchCondition condition) {
        return query
                .selectFrom(workHistory)
                .join(workHistory.user).fetchJoin()
                .leftJoin(workHistory.group).fetchJoin()
                .leftJoin(workHistory.part).fetchJoin()
                .leftJoin(workHistory.division).fetchJoin()
                .where(
                        workHistory.isDeleted.eq(YNType.N),
                        workHistory.user.company.ne(OriginCompanyType.SYSTEM),
                        dateBetween(condition.getStartDate(), condition.getEndDate()),
                        userNameLike(condition.getUserName()),
                        userIdEq(condition.getUserId()),
                        groupEq(condition.getGroupSeq()),
                        partEq(condition.getPartSeq()),
                        divisionEq(condition.getDivisionSeq()))
                .orderBy(
                        "OLDEST".equalsIgnoreCase(condition.getSortType()) ? workHistory.date.asc()
                                : workHistory.date.desc(),
                        workHistory.id.desc())
                .fetch();
    }

    @Override
    public void delete(WorkHistory workHistory) {
        workHistory.deleteWorkHistory();
    }

    @Override
    public Stream<WorkHistory> findAllStream(WorkHistorySearchCondition condition) {
        return query
                .selectFrom(workHistory)
                .join(workHistory.user).fetchJoin()
                .leftJoin(workHistory.group).fetchJoin()
                .leftJoin(workHistory.part).fetchJoin()
                .leftJoin(workHistory.division).fetchJoin()
                .where(
                        workHistory.isDeleted.eq(YNType.N),
                        workHistory.user.company.ne(OriginCompanyType.SYSTEM),
                        dateBetween(condition.getStartDate(), condition.getEndDate()),
                        userNameLike(condition.getUserName()),
                        userIdEq(condition.getUserId()),
                        groupEq(condition.getGroupSeq()),
                        partEq(condition.getPartSeq()),
                        divisionEq(condition.getDivisionSeq()))
                .orderBy(
                        "OLDEST".equalsIgnoreCase(condition.getSortType()) ? workHistory.date.asc()
                                : workHistory.date.desc(),
                        workHistory.id.desc())
                .stream();
    }

    private BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate != null && endDate != null) {
            return workHistory.date.between(startDate, endDate);
        }
        if (startDate != null) {
            return workHistory.date.goe(startDate);
        }
        return workHistory.date.loe(endDate);
    }

    private BooleanExpression userNameLike(String userName) {
        return StringUtils.hasText(userName) ? workHistory.user.name.contains(userName) : null;
    }

    private BooleanExpression userIdEq(String userId) {
        return StringUtils.hasText(userId) ? workHistory.user.id.eq(userId) : null;
    }

    private BooleanExpression groupEq(Long groupSeq) {
        return groupSeq != null ? workHistory.group.id.eq(groupSeq) : null;
    }

    private BooleanExpression partEq(Long partSeq) {
        return partSeq != null ? workHistory.part.id.eq(partSeq) : null;
    }

    private BooleanExpression divisionEq(Long divisionSeq) {
        return divisionSeq != null ? workHistory.division.id.eq(divisionSeq) : null;
    }

    @Override
    public List<WorkHistory> findByUserAndDate(String userId, LocalDate date) {
        return query
                .selectFrom(workHistory)
                .where(
                        workHistory.user.id.eq(userId),
                        workHistory.date.eq(date),
                        workHistory.isDeleted.eq(YNType.N)
                )
                .fetch();
    }

    @Override
    public Map<LocalDate, BigDecimal> findDailyWorkHoursByUserAndPeriod(String userId, LocalDate startDate, LocalDate endDate) {
        List<WorkHistory> histories = query
                .selectFrom(workHistory)
                .where(
                        workHistory.user.id.eq(userId),
                        workHistory.date.between(startDate, endDate),
                        workHistory.isDeleted.eq(YNType.N)
                )
                .fetch();

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

        List<WorkHistory> histories = query
                .selectFrom(workHistory)
                .where(
                        workHistory.user.id.in(userIds),
                        workHistory.date.between(startDate, endDate),
                        workHistory.isDeleted.eq(YNType.N)
                )
                .fetch();

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
}
