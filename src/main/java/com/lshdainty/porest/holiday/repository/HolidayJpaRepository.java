package com.lshdainty.porest.holiday.repository;

import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.type.HolidayType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("holidayJpaRepository")
@RequiredArgsConstructor
public class HolidayJpaRepository implements HolidayRepository {
    private final EntityManager em;

    @Override
    public void save(Holiday holiday) {
        em.persist(holiday);
    }

    @Override
    public Optional<Holiday> findById(Long seq) {
        return Optional.ofNullable(em.find(Holiday.class, seq));
    }

    @Override
    public List<Holiday> findHolidays(CountryCode countryCode) {
        return em.createQuery("select h from Holiday h where h.countryCode = :countryCode order by h.date", Holiday.class)
                .setParameter("countryCode", countryCode)
                .getResultList();
    }

    @Override
    public List<Holiday> findHolidaysByStartEndDate(LocalDate start, LocalDate end, CountryCode countryCode) {
        return em.createQuery("select h from Holiday h where h.date between :start and :end and h.countryCode = :countryCode order by h.date", Holiday.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("countryCode", countryCode)
                .getResultList();
    }

    @Override
    public List<Holiday> findHolidaysByType(HolidayType type) {
        return em.createQuery("select h from Holiday h where h.type = :type", Holiday.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public List<Holiday> findHolidaysByStartEndDateWithType(LocalDate start, LocalDate end, HolidayType type) {
        return em.createQuery("select h from Holiday h where h.date between :start and :end and h.type = :type order by h.date", Holiday.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public void delete(Holiday holiday) {
        em.remove(holiday);
    }
}
