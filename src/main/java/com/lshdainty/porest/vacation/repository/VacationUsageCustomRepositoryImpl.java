package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.lshdainty.porest.vacation.domain.QVacationUsage.vacationUsage;

@Repository
@RequiredArgsConstructor
public class VacationUsageCustomRepositoryImpl implements VacationUsageCustomRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

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
        return Optional.ofNullable(query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.id.eq(vacationUsageId))
                .fetchOne()
        );
    }

    @Override
    public List<VacationUsage> findByUserId(String userId) {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.user.id.eq(userId)
                        .and(vacationUsage.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public List<VacationUsage> findAllWithUser() {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.isDeleted.eq(YNType.N))
                .orderBy(vacationUsage.user.id.asc(), vacationUsage.startDate.desc())
                .fetch();
    }

    @Override
    public List<VacationUsage> findByPeriodWithUser(LocalDateTime startDate, LocalDateTime endDate) {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.isDeleted.eq(YNType.N)
                        .and(vacationUsage.startDate.goe(startDate))
                        .and(vacationUsage.startDate.loe(endDate)))
                .orderBy(vacationUsage.startDate.asc())
                .fetch();
    }

    @Override
    public List<VacationUsage> findByUserIdAndPeriodWithUser(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.user.id.eq(userId)
                        .and(vacationUsage.isDeleted.eq(YNType.N))
                        .and(vacationUsage.startDate.goe(startDate))
                        .and(vacationUsage.startDate.loe(endDate)))
                .orderBy(vacationUsage.startDate.asc())
                .fetch();
    }

    @Override
    public List<VacationUsage> findUsedByUserIdAndBaseTime(String userId, LocalDateTime baseTime) {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.user.id.eq(userId)
                        .and(vacationUsage.isDeleted.eq(YNType.N))
                        .and(vacationUsage.startDate.loe(baseTime)))
                .fetch();
    }

    @Override
    public List<VacationUsage> findExpectedByUserIdAndBaseTime(String userId, LocalDateTime baseTime) {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.user.id.eq(userId)
                        .and(vacationUsage.isDeleted.eq(YNType.N))
                        .and(vacationUsage.startDate.gt(baseTime)))
                .fetch();
    }

    @Override
    public List<VacationUsage> findByUserIdAndPeriod(String userId, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {
        return query
                .selectFrom(vacationUsage)
                .where(vacationUsage.user.id.eq(userId)
                        .and(vacationUsage.startDate.between(startOfPeriod, endOfPeriod))
                        .and(vacationUsage.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public Map<LocalDate, BigDecimal> findDailyVacationHoursByUserAndPeriod(String userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<VacationUsage> usages = query
                .selectFrom(vacationUsage)
                .where(
                        vacationUsage.user.id.eq(userId),
                        vacationUsage.startDate.goe(startDateTime),
                        vacationUsage.startDate.lt(endDateTime),
                        vacationUsage.isDeleted.eq(YNType.N)
                )
                .fetch();

        Map<LocalDate, BigDecimal> dailyHoursMap = new HashMap<>();
        for (VacationUsage usage : usages) {
            LocalDate date = usage.getStartDate().toLocalDate();
            BigDecimal usedTime = usage.getUsedTime() != null ? usage.getUsedTime() : BigDecimal.ZERO;
            dailyHoursMap.merge(date, usedTime, BigDecimal::add);
        }
        return dailyHoursMap;
    }

    @Override
    public Map<String, Map<LocalDate, BigDecimal>> findDailyVacationHoursByUsersAndPeriod(List<String> userIds, LocalDate startDate, LocalDate endDate) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<VacationUsage> usages = query
                .selectFrom(vacationUsage)
                .where(
                        vacationUsage.user.id.in(userIds),
                        vacationUsage.startDate.goe(startDateTime),
                        vacationUsage.startDate.lt(endDateTime),
                        vacationUsage.isDeleted.eq(YNType.N)
                )
                .fetch();

        Map<String, Map<LocalDate, BigDecimal>> result = new HashMap<>();
        for (VacationUsage usage : usages) {
            String usrId = usage.getUser().getId();
            LocalDate date = usage.getStartDate().toLocalDate();
            BigDecimal usedTime = usage.getUsedTime() != null ? usage.getUsedTime() : BigDecimal.ZERO;

            result.computeIfAbsent(usrId, k -> new HashMap<>())
                    .merge(date, usedTime, BigDecimal::add);
        }
        return result;
    }
}