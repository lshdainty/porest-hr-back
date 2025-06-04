package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.VacationHistory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VacationHistoryRepositoryImpl implements VacationHistoryRepository {
    private final EntityManager em;

    @Override
    public void save(VacationHistory vacationHistory) {
        em.persist(vacationHistory);
    }

    @Override
    public Optional<VacationHistory> findById(Long vacationHistoryId) {
        return Optional.ofNullable(em.find(VacationHistory.class, vacationHistoryId));
    }
}
