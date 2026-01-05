package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.QUser;
import com.lshdainty.porest.vacation.domain.QVacationGrantSchedule;
import com.lshdainty.porest.vacation.domain.QVacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationGrantSchedule;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * VacationGrantSchedule Repository 구현체<br>
 * QueryDSL을 활용한 휴가 부여 스케줄 조회 구현
 */
@Repository
@Primary
@RequiredArgsConstructor
public class VacationGrantScheduleQueryDslRepository implements VacationGrantScheduleRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(VacationGrantSchedule schedule) {
        em.persist(schedule);
    }

    @Override
    public Optional<VacationGrantSchedule> findByUserIdAndPolicyId(String userId, Long policyId) {
        QVacationGrantSchedule schedule = QVacationGrantSchedule.vacationGrantSchedule;
        QUser user = QUser.user;
        QVacationPolicy policy = QVacationPolicy.vacationPolicy;

        VacationGrantSchedule result = queryFactory
                .selectFrom(schedule)
                .leftJoin(schedule.user, user).fetchJoin()
                .leftJoin(schedule.vacationPolicy, policy).fetchJoin()
                .where(
                        schedule.user.id.eq(userId),
                        schedule.vacationPolicy.id.eq(policyId),
                        schedule.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByUserIdAndPolicyId(String userId, Long policyId) {
        QVacationGrantSchedule schedule = QVacationGrantSchedule.vacationGrantSchedule;

        Integer result = queryFactory
                .selectOne()
                .from(schedule)
                .where(
                        schedule.user.id.eq(userId),
                        schedule.vacationPolicy.id.eq(policyId),
                        schedule.isDeleted.eq(YNType.N)
                )
                .fetchFirst();

        return result != null;
    }

    @Override
    public List<VacationGrantSchedule> findRepeatGrantTargetsForToday(LocalDate today) {
        QVacationGrantSchedule schedule = QVacationGrantSchedule.vacationGrantSchedule;
        QUser user = QUser.user;
        QVacationPolicy policy = QVacationPolicy.vacationPolicy;

        return queryFactory
                .selectFrom(schedule)
                .leftJoin(schedule.user, user).fetchJoin()
                .leftJoin(schedule.vacationPolicy, policy).fetchJoin()
                .where(
                        schedule.isDeleted.eq(YNType.N),
                        policy.grantMethod.eq(GrantMethod.REPEAT_GRANT),
                        policy.isDeleted.eq(YNType.N),
                        user.isDeleted.eq(YNType.N),
                        schedule.nextGrantDate.isNull()
                                .or(schedule.nextGrantDate.loe(today))
                )
                .fetch();
    }
}
