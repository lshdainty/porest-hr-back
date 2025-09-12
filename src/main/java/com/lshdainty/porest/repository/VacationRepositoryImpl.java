package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Vacation;
import com.lshdainty.porest.type.VacationType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VacationRepositoryImpl implements VacationRepository {
    private final EntityManager em;

    @Override
    public void save(Vacation vacation) {
        em.persist(vacation);
    }

    @Override
    public Optional<Vacation> findById(Long vacationId) {
        return Optional.ofNullable(em.find(Vacation.class, vacationId));
    }

    @Override
    public List<Vacation> findVacationsByUserId(String userId) {
        return em.createQuery("select v from Vacation v where v.user.id = :userId", Vacation.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public Optional<Vacation> findVacationByTypeWithYear(String userId, VacationType type, String year) {
        return em.createQuery("select v from Vacation v where v.user.id = :userId and v.type = :type and year(v.expiryDate) = :year", Vacation.class)
                .setParameter("userId", userId)
                .setParameter("type", type)
                .setParameter("year", year)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Vacation> findVacationsByBaseTime(String userId, LocalDateTime baseTime) {
        return em.createQuery("select v from Vacation v where v.user.id = :userId and :baseTime between v.occurDate and v.expiryDate", Vacation.class)
                .setParameter("userId", userId)
                .setParameter("baseTime", baseTime)
                .getResultList();
    }

    @Override
    public List<Vacation> findVacationsByBaseTimeWithHistory(String userId, LocalDateTime baseTime) {
        return em.createQuery("select v from Vacation v left join fetch v.historys s where v.user.id = :userId and :baseTime between v.occurDate and v.expiryDate", Vacation.class)
                .setParameter("userId", userId)
                .setParameter("baseTime", baseTime)
                .getResultList();
    }

    @Override
    public List<Vacation> findVacationsByIdsWithUser(List<Long> vacationIds) {
        return em.createQuery("select v from Vacation v join fetch v.user u where v.id in :vacationIds", Vacation.class)
                .setParameter("vacationIds", vacationIds)
                .getResultList();
    }
}
