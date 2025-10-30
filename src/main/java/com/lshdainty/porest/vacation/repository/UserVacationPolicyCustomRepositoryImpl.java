package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.UserVacationPolicy;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.vacation.domain.QUserVacationPolicy.userVacationPolicy;

@Repository
@RequiredArgsConstructor
public class UserVacationPolicyCustomRepositoryImpl implements UserVacationPolicyCustomRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(UserVacationPolicy userVacationPolicy) {
        em.persist(userVacationPolicy);
    }

    @Override
    public void saveAll(List<UserVacationPolicy> userVacationPolicies) {
        for (UserVacationPolicy uvp : userVacationPolicies) {
            em.persist(uvp);
        }
    }

    @Override
    public List<UserVacationPolicy> findByUserId(String userId) {
        return query
                .selectFrom(userVacationPolicy)
                .join(userVacationPolicy.vacationPolicy).fetchJoin()
                .where(userVacationPolicy.user.id.eq(userId))
                .fetch();
    }

    @Override
    public boolean existsByUserIdAndVacationPolicyId(String userId, Long vacationPolicyId) {
        Integer count = query
                .selectOne()
                .from(userVacationPolicy)
                .where(userVacationPolicy.user.id.eq(userId)
                        .and(userVacationPolicy.vacationPolicy.id.eq(vacationPolicyId)))
                .fetchFirst();
        return count != null;
    }

    @Override
    public Optional<UserVacationPolicy> findById(Long userVacationPolicyId) {
        return Optional.ofNullable(query
                .selectFrom(userVacationPolicy)
                .join(userVacationPolicy.vacationPolicy).fetchJoin()
                .join(userVacationPolicy.user).fetchJoin()
                .where(userVacationPolicy.id.eq(userVacationPolicyId))
                .fetchOne()
        );
    }

    @Override
    public Optional<UserVacationPolicy> findByUserIdAndVacationPolicyId(String userId, Long vacationPolicyId) {
        return Optional.ofNullable(query
                .selectFrom(userVacationPolicy)
                .join(userVacationPolicy.vacationPolicy).fetchJoin()
                .join(userVacationPolicy.user).fetchJoin()
                .where(userVacationPolicy.user.id.eq(userId)
                        .and(userVacationPolicy.vacationPolicy.id.eq(vacationPolicyId)))
                .fetchOne()
        );
    }

    @Override
    public List<UserVacationPolicy> findByVacationPolicyId(Long vacationPolicyId) {
        return query
                .selectFrom(userVacationPolicy)
                .join(userVacationPolicy.vacationPolicy).fetchJoin()
                .join(userVacationPolicy.user).fetchJoin()
                .where(userVacationPolicy.vacationPolicy.id.eq(vacationPolicyId))
                .fetch();
    }

    @Override
    public List<UserVacationPolicy> findRepeatGrantTargetsForToday(LocalDate today) {
        return query
                .selectFrom(userVacationPolicy)
                .join(userVacationPolicy.vacationPolicy).fetchJoin()
                .join(userVacationPolicy.user).fetchJoin()
                .where(
                        // 삭제되지 않은 정책
                        userVacationPolicy.isDeleted.eq(YNType.N)
                                // 반복 부여 타입
                                .and(userVacationPolicy.vacationPolicy.grantMethod.eq(GrantMethod.REPEAT_GRANT))
                                // nextGrantDate가 null이거나 오늘 이전
                                .and(
                                        userVacationPolicy.nextGrantDate.isNull()
                                                .or(userVacationPolicy.nextGrantDate.loe(today))
                                )
                                // 정책 자체가 삭제되지 않음
                                .and(userVacationPolicy.vacationPolicy.isDeleted.eq(YNType.N))
                )
                .fetch();
    }
}
