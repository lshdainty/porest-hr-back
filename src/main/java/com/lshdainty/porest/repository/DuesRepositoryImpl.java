package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Dues;
import com.lshdainty.porest.type.DuesCalcType;
import com.lshdainty.porest.type.DuesType;
import com.lshdainty.porest.repository.dto.UsersMonthBirthDuesDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DuesRepositoryImpl implements DuesRepository {
    private final EntityManager em;

    @Override
    public void save(Dues dues) {
        em.persist(dues);
    }

    @Override
    public Optional<Dues> findById(Long id) {
        return Optional.ofNullable(em.find(Dues.class, id));
    }

    @Override
    public List<Dues> findDues() {
        return em.createQuery("select d from Dues d order by d.date", Dues.class)
                .getResultList();
    }

    @Override
    public List<Dues> findDuesByYear(String year) {
        return em.createQuery("select d from Dues d where substring(d.date, 0, 4) = :year order by d.date", Dues.class)
                .setParameter("year", year)
                .getResultList();
    }

    @Override
    public List<Dues> findOperatingDuesByYear(String year) {
        return em.createQuery("select d from Dues d where substring(d.date, 0, 4) = :year and d.type <> :type order by d.date", Dues.class)
                .setParameter("year", year)
                .setParameter("type", DuesType.BIRTH)
                .getResultList();
    }

    @Override
    public Long findBirthDuesByYearAndMonth(String year, String month) {
        return em.createQuery("select sum(d.amount) from Dues d where substring(d.date, 0, 4) = :year and substring(d.date, 5, 2) = :month and d.type = :type and d.calc = :calc", Long.class)
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("type", DuesType.BIRTH)
                .setParameter("calc", DuesCalcType.PLUS)
                .getSingleResult();
    }

    @Override
    public List<UsersMonthBirthDuesDto> findUsersMonthBirthDues(String year) {
        return em.createQuery("select new com.lshdainty.porest.repository.dto.UsersMonthBirthDuesDto(d.userName, substring(d.date, 5, 2), sum(d.amount), d.detail) from Dues d where substring(d.date, 0, 4) = :year and d.type = :type and d.calc = :calc group by d.userName, substring(d.date, 5, 2), d.detail", UsersMonthBirthDuesDto.class)
                .setParameter("year", year)
                .setParameter("type", DuesType.BIRTH)
                .setParameter("calc", DuesCalcType.PLUS)
                .getResultList();
    }

    @Override
    public void delete(Dues dues) {
        em.remove(dues);
    }
}
