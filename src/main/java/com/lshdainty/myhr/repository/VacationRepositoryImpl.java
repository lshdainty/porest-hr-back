package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationType;
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
    public List<Vacation> findVacationsByUserNo(Long userNo) {
        return em.createQuery("select v from Vacation v where v.user.id = :userNo", Vacation.class)
                .setParameter("userNo", userNo)
                .getResultList();
    }

    @Override
    public Optional<Vacation> findVacationByTypeWithYear(Long userNo, VacationType type, String year) {
        return em.createQuery("select v from Vacation v where v.user.id = :userNo and v.type = :type and year(v.expiryDate) = :year", Vacation.class)
                .setParameter("userNo", userNo)
                .setParameter("type", type)
                .setParameter("year", year)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Vacation> findVacationsByBaseTime(Long userNo, LocalDateTime baseTime) {
        return em.createQuery("select v from Vacation v where v.user.id = :userNo and :baseTime between v.occurDate and v.expiryDate", Vacation.class)
                .setParameter("userNo", userNo)
                .setParameter("baseTime", baseTime)
                .getResultList();
    }

    @Override
    public List<Vacation> findVacationsByBaseTimeWithHistory(Long userNo, LocalDateTime baseTime) {
        return em.createQuery("select v from Vacation v left join fetch v.historys s where v.user.id = :userNo and :baseTime between v.occurDate and v.expiryDate", Vacation.class)
                .setParameter("userNo", userNo)
                .setParameter("baseTime", baseTime)
                .getResultList();
    }
}
