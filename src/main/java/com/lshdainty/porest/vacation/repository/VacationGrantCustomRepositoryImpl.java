package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.lshdainty.porest.vacation.domain.QVacationGrant.vacationGrant;

@Repository
@RequiredArgsConstructor
public class VacationGrantCustomRepositoryImpl implements VacationGrantCustomRepository {
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
    public List<VacationGrant> findByUserId(String userId) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public List<VacationGrant> findByPolicyId(Long policyId) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.policy.id.eq(policyId)
                        .and(vacationGrant.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public List<VacationGrant> findAvailableGrantsByUserIdOrderByExpiryDate(String userId) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N))
                        .and(vacationGrant.remainTime.gt(java.math.BigDecimal.ZERO)))
                .orderBy(vacationGrant.expiryDate.asc(), vacationGrant.grantDate.asc())
                .fetch();
    }

    @Override
    public List<VacationGrant> findAvailableGrantsByUserIdAndTypeAndDate(
            String userId,
            com.lshdainty.porest.vacation.type.VacationType vacationType,
            java.time.LocalDateTime usageStartDate) {
        return query
                .selectFrom(vacationGrant)
                .join(vacationGrant.user).fetchJoin()
                .join(vacationGrant.policy).fetchJoin()
                .where(vacationGrant.user.id.eq(userId)
                        .and(vacationGrant.isDeleted.eq(YNType.N))
                        .and(vacationGrant.type.eq(vacationType))
                        .and(vacationGrant.remainTime.gt(java.math.BigDecimal.ZERO))
                        .and(vacationGrant.grantDate.loe(usageStartDate))
                        .and(vacationGrant.expiryDate.goe(usageStartDate)))
                .orderBy(vacationGrant.expiryDate.asc(), vacationGrant.grantDate.asc())
                .fetch();
    }
}