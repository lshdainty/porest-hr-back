package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.UserVacationPolicy;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.VacationType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("userVacationPolicyJpaRepository")
@RequiredArgsConstructor
public class UserVacationPolicyJpaRepository implements UserVacationPolicyRepository {
    private final EntityManager em;

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
        return em.createQuery(
                        "select uvp from UserVacationPolicy uvp " +
                                "join fetch uvp.vacationPolicy " +
                                "where uvp.user.id = :userId and uvp.isDeleted = :isDeleted", UserVacationPolicy.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public boolean existsByUserIdAndVacationPolicyId(String userId, Long vacationPolicyId) {
        Long count = em.createQuery(
                        "select count(uvp) from UserVacationPolicy uvp " +
                                "where uvp.user.id = :userId and uvp.vacationPolicy.id = :vacationPolicyId", Long.class)
                .setParameter("userId", userId)
                .setParameter("vacationPolicyId", vacationPolicyId)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public Optional<UserVacationPolicy> findById(Long userVacationPolicyId) {
        List<UserVacationPolicy> result = em.createQuery(
                        "select uvp from UserVacationPolicy uvp " +
                                "join fetch uvp.vacationPolicy " +
                                "join fetch uvp.user " +
                                "where uvp.id = :id", UserVacationPolicy.class)
                .setParameter("id", userVacationPolicyId)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<UserVacationPolicy> findByUserIdAndVacationPolicyId(String userId, Long vacationPolicyId) {
        List<UserVacationPolicy> result = em.createQuery(
                        "select uvp from UserVacationPolicy uvp " +
                                "join fetch uvp.vacationPolicy " +
                                "join fetch uvp.user " +
                                "where uvp.user.id = :userId and uvp.vacationPolicy.id = :vacationPolicyId", UserVacationPolicy.class)
                .setParameter("userId", userId)
                .setParameter("vacationPolicyId", vacationPolicyId)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<UserVacationPolicy> findByVacationPolicyId(Long vacationPolicyId) {
        return em.createQuery(
                        "select uvp from UserVacationPolicy uvp " +
                                "join fetch uvp.vacationPolicy " +
                                "join fetch uvp.user " +
                                "where uvp.vacationPolicy.id = :vacationPolicyId", UserVacationPolicy.class)
                .setParameter("vacationPolicyId", vacationPolicyId)
                .getResultList();
    }

    @Override
    public List<UserVacationPolicy> findRepeatGrantTargetsForToday(LocalDate today) {
        return em.createQuery(
                        "select uvp from UserVacationPolicy uvp " +
                                "join fetch uvp.vacationPolicy " +
                                "join fetch uvp.user " +
                                "where uvp.isDeleted = :isDeleted " +
                                "and uvp.vacationPolicy.grantMethod = :grantMethod " +
                                "and (uvp.nextGrantDate is null or uvp.nextGrantDate <= :today) " +
                                "and uvp.vacationPolicy.isDeleted = :isDeleted", UserVacationPolicy.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("grantMethod", GrantMethod.REPEAT_GRANT)
                .setParameter("today", today)
                .getResultList();
    }

    @Override
    public List<UserVacationPolicy> findByUserIdWithFilters(String userId, VacationType vacationType, GrantMethod grantMethod) {
        StringBuilder jpql = new StringBuilder(
                "select uvp from UserVacationPolicy uvp " +
                        "join fetch uvp.vacationPolicy " +
                        "where uvp.user.id = :userId and uvp.isDeleted = :isDeleted");

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("isDeleted", YNType.N);

        if (vacationType != null) {
            jpql.append(" and uvp.vacationPolicy.vacationType = :vacationType");
            params.put("vacationType", vacationType);
        }

        if (grantMethod != null) {
            jpql.append(" and uvp.vacationPolicy.grantMethod = :grantMethod");
            params.put("grantMethod", grantMethod);
        }

        TypedQuery<UserVacationPolicy> query = em.createQuery(jpql.toString(), UserVacationPolicy.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}
