package com.lshdainty.porest.holiday.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.holiday.domain.QHoliday.holiday;

@Repository
@Primary
@RequiredArgsConstructor
public class HolidayQueryDslRepository implements HolidayRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(Holiday holiday) {
        em.persist(holiday);
    }

    @Override
    public Optional<Holiday> findById(Long id) {
        return Optional.ofNullable(em.find(Holiday.class, id));
    }

    @Override
    public List<Holiday> findHolidays(CountryCode countryCode) {
        return query
                .selectFrom(holiday)
                .where(holiday.countryCode.eq(countryCode))
                .orderBy(holiday.date.asc())
                .fetch();
    }

    @Override
    public List<Holiday> findHolidaysByStartEndDate(LocalDate start, LocalDate end, CountryCode countryCode) {
        return query
                .selectFrom(holiday)
                .where(holiday.date.between(start, end)
                        .and(holiday.countryCode.eq(countryCode)))
                .orderBy(holiday.date.asc())
                .fetch();
    }

    @Override
    public List<Holiday> findHolidaysByType(HolidayType type) {
        return query
                .selectFrom(holiday)
                .where(holiday.type.eq(type))
                .fetch();
    }

    @Override
    public List<Holiday> findHolidaysByStartEndDateWithType(LocalDate start, LocalDate end, HolidayType type, CountryCode countryCode) {
        return query
                .selectFrom(holiday)
                .where(holiday.date.between(start, end)
                        .and(holiday.type.eq(type))
                        .and(holiday.countryCode.eq(countryCode)))
                .orderBy(holiday.date.asc())
                .fetch();
    }

    @Override
    public void delete(Holiday holiday) {
        em.remove(holiday);
    }
}
