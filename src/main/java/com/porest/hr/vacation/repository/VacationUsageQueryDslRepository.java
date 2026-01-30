package com.porest.hr.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.common.type.DefaultCompanyType;
import com.porest.hr.vacation.domain.VacationUsage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.porest.hr.vacation.domain.QVacationUsage.vacationUsage;

@Repository
@Primary
@RequiredArgsConstructor
public class VacationUsageQueryDslRepository implements VacationUsageRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(VacationUsage vacationUsage) {
        em.persist(vacationUsage);
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
    public List<VacationUsage> findByUserIdAndYear(String userId, int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.user.id.eq(userId)
                        .and(vacationUsage.isDeleted.eq(YNType.N))
                        .and(vacationUsage.startDate.between(startOfYear, endOfYear)))
                .orderBy(vacationUsage.startDate.asc())
                .fetch();
    }

    @Override
    public List<VacationUsage> findAllWithUser() {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.isDeleted.eq(YNType.N)
                        .and(vacationUsage.user.company.ne(DefaultCompanyType.SYSTEM)))
                .orderBy(vacationUsage.user.id.asc(), vacationUsage.startDate.desc())
                .fetch();
    }

    @Override
    public List<VacationUsage> findByPeriodWithUser(LocalDateTime startDate, LocalDateTime endDate) {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.isDeleted.eq(YNType.N)
                        .and(vacationUsage.user.company.ne(DefaultCompanyType.SYSTEM))
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
    public List<VacationUsage> findByUserIdAndPeriodForDaily(String userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return query
                .selectFrom(vacationUsage)
                .where(
                        vacationUsage.user.id.eq(userId),
                        vacationUsage.startDate.goe(startDateTime),
                        vacationUsage.startDate.lt(endDateTime),
                        vacationUsage.isDeleted.eq(YNType.N)
                )
                .fetch();
    }

    @Override
    public List<VacationUsage> findByUserIdsAndPeriodForDaily(List<String> userIds, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return query
                .selectFrom(vacationUsage)
                .where(
                        vacationUsage.user.id.in(userIds),
                        vacationUsage.startDate.goe(startDateTime),
                        vacationUsage.startDate.lt(endDateTime),
                        vacationUsage.isDeleted.eq(YNType.N)
                )
                .fetch();
    }

    @Override
    public List<VacationUsage> findByUserIdsAndPeriod(List<String> userIds, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.user.id.in(userIds)
                        .and(vacationUsage.startDate.between(startOfPeriod, endOfPeriod))
                        .and(vacationUsage.isDeleted.eq(YNType.N)))
                .fetch();
    }
}