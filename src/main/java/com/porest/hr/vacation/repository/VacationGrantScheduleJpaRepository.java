package com.porest.hr.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.porest.hr.vacation.domain.VacationGrantSchedule;
import com.porest.hr.vacation.type.GrantMethod;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * VacationGrantSchedule Repository 구현체 (JPQL)<br>
 * JPQL을 활용한 휴가 부여 스케줄 조회 구현 (백업용)
 */
@Repository("vacationGrantScheduleJpaRepository")
@RequiredArgsConstructor
public class VacationGrantScheduleJpaRepository implements VacationGrantScheduleRepository {
    private final EntityManager em;

    @Override
    public void save(VacationGrantSchedule schedule) {
        em.persist(schedule);
    }

    @Override
    public Optional<VacationGrantSchedule> findByUserIdAndPolicyId(String userId, Long policyId) {
        List<VacationGrantSchedule> result = em.createQuery(
                "select s from VacationGrantSchedule s " +
                "left join fetch s.user u " +
                "left join fetch s.vacationPolicy p " +
                "where s.user.id = :userId and s.vacationPolicy.id = :policyId " +
                "and s.isDeleted = :isDeleted", VacationGrantSchedule.class)
                .setParameter("userId", userId)
                .setParameter("policyId", policyId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public boolean existsByUserIdAndPolicyId(String userId, Long policyId) {
        List<VacationGrantSchedule> result = em.createQuery(
                "select s from VacationGrantSchedule s " +
                "where s.user.id = :userId and s.vacationPolicy.id = :policyId " +
                "and s.isDeleted = :isDeleted", VacationGrantSchedule.class)
                .setParameter("userId", userId)
                .setParameter("policyId", policyId)
                .setParameter("isDeleted", YNType.N)
                .setMaxResults(1)
                .getResultList();
        return !result.isEmpty();
    }

    @Override
    public List<VacationGrantSchedule> findRepeatGrantTargetsForToday(LocalDate today) {
        return em.createQuery(
                "select s from VacationGrantSchedule s " +
                "left join fetch s.user u " +
                "left join fetch s.vacationPolicy p " +
                "where s.isDeleted = :isDeleted " +
                "and p.grantMethod = :grantMethod " +
                "and p.isDeleted = :isDeleted " +
                "and u.isDeleted = :isDeleted " +
                "and (s.nextGrantDate is null or s.nextGrantDate <= :today)", VacationGrantSchedule.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("grantMethod", GrantMethod.REPEAT_GRANT)
                .setParameter("today", today)
                .getResultList();
    }
}
