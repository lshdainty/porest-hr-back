package com.porest.hr.vacation.repository;

import com.porest.core.type.YNType;
import com.porest.hr.vacation.domain.VacationGrant;
import com.porest.hr.vacation.type.GrantMethod;
import com.porest.hr.vacation.type.GrantStatus;
import com.porest.hr.vacation.type.VacationType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.porest.hr.vacation.domain.QVacationGrant.vacationGrant;

@Repository
@Primary
@RequiredArgsConstructor
public class VacationGrantQueryDslRepository implements VacationGrantRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

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
    public List<VacationGrant> findByUserIdAndYear(String userId, int year) {
        LocalDateTime startOfYear = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N))
                        .and(vacationGrant.status.in(GrantStatus.ACTIVE, GrantStatus.EXHAUSTED, GrantStatus.EXPIRED))
                        .and(vacationGrant.grantDate.loe(endOfYear))
                        .and(vacationGrant.expiryDate.goe(startOfYear)))
                .orderBy(vacationGrant.grantDate.asc())
                .fetch();
    }

    @Override
    public List<VacationGrant> findByPolicyId(Long policyId) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.policy.rowId.eq(policyId)
                        .and(vacationGrant.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public List<VacationGrant> findAvailableGrantsByUserIdAndTypeAndDate(String userId, VacationType vacationType, LocalDateTime usageStartDate) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N))
                        .and(vacationGrant.status.eq(GrantStatus.ACTIVE))
                        .and(vacationGrant.type.eq(vacationType))
                        .and(vacationGrant.remainTime.gt(BigDecimal.ZERO))
                        .and(vacationGrant.grantDate.loe(usageStartDate))
                        .and(vacationGrant.expiryDate.goe(usageStartDate)))
                .orderBy(vacationGrant.expiryDate.asc(), vacationGrant.grantDate.asc())
                .fetch();
    }

    @Override
    public List<VacationGrant> findAllWithUser() {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.isDeleted.eq(YNType.N)
                        .and(vacationGrant.status.eq(GrantStatus.ACTIVE))
                        .and(vacationGrant.user.company.ne("SYSTEM")))
                .orderBy(vacationGrant.user.id.asc(), vacationGrant.expiryDate.asc())
                .fetch();
    }

    @Override
    public List<VacationGrant> findAvailableGrantsByUserIdAndDate(String userId, LocalDateTime usageStartDate) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N))
                        .and(vacationGrant.status.eq(GrantStatus.ACTIVE))
                        .and(vacationGrant.remainTime.gt(BigDecimal.ZERO))
                        .and(vacationGrant.grantDate.loe(usageStartDate))
                        .and(vacationGrant.expiryDate.goe(usageStartDate)))
                .orderBy(vacationGrant.expiryDate.asc(), vacationGrant.grantDate.asc())
                .fetch();
    }

    @Override
    public List<VacationGrant> findValidGrantsByUserIdAndBaseTime(String userId, LocalDateTime baseTime) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N))
                        .and(vacationGrant.status.in(GrantStatus.ACTIVE, GrantStatus.EXHAUSTED, GrantStatus.EXPIRED))
                        .and(vacationGrant.grantDate.loe(baseTime))
                        .and(vacationGrant.expiryDate.goe(baseTime)))
                .fetch();
    }

    @Override
    public List<VacationGrant> findExpiredTargets(LocalDateTime currentDate) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.isDeleted.eq(YNType.N)
                        .and(vacationGrant.status.eq(GrantStatus.ACTIVE))
                        .and(vacationGrant.expiryDate.lt(currentDate)))
                .fetch();
    }

    @Override
    public Optional<VacationGrant> findById(Long id) {
        return Optional.ofNullable(query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.rowId.eq(id))
                .fetchOne());
    }

    @Override
    public List<VacationGrant> findAllRequestedVacationsByUserIdAndYear(String userId, Integer year) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N))
                        .and(vacationGrant.policy.grantMethod.eq(com.porest.hr.vacation.type.GrantMethod.ON_REQUEST))
                        .and(vacationGrant.createAt.year().eq(year)))
                .orderBy(vacationGrant.requestStartTime.desc())
                .fetch();
    }

    @Override
    public List<VacationGrant> findByIdsWithUserAndPolicy(List<Long> vacationGrantIds) {
        if (vacationGrantIds == null || vacationGrantIds.isEmpty()) {
            return List.of();
        }

        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.rowId.in(vacationGrantIds)
                        .and(vacationGrant.isDeleted.eq(YNType.N)))
                .orderBy(vacationGrant.createAt.desc())
                .fetch();
    }

    @Override
    public List<VacationGrant> findByUserIdsAndValidPeriod(List<String> userIds, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .where(vacationGrant.user.id.in(userIds)
                        .and(vacationGrant.grantDate.loe(endOfPeriod))
                        .and(vacationGrant.expiryDate.goe(startOfPeriod))
                        .and(vacationGrant.status.in(GrantStatus.ACTIVE, GrantStatus.EXHAUSTED))
                        .and(vacationGrant.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public List<VacationGrant> findByUserIdsAndStatusesAndPeriod(List<String> userIds, List<GrantStatus> statuses, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .where(vacationGrant.user.id.in(userIds)
                        .and(vacationGrant.status.in(statuses))
                        .and(vacationGrant.requestStartTime.between(startOfPeriod, endOfPeriod))
                        .and(vacationGrant.isDeleted.eq(YNType.N)))
                .fetch();
    }
}