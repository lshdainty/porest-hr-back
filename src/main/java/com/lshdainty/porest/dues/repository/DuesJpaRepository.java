package com.lshdainty.porest.dues.repository;

import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("duesJpaRepository")
@RequiredArgsConstructor
public class DuesJpaRepository implements DuesRepository {
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
    public List<Dues> findDuesByYear(int year) {
        return em.createQuery("select d from Dues d where year(d.date) = :year order by d.date", Dues.class)
                .setParameter("year", year)
                .getResultList();
    }


    @Override
    public List<Dues> findOperatingDuesByYear(int year) {
        return em.createQuery("select d from Dues d where year(d.date) = :year and d.type <> :type order by d.date", Dues.class)
                .setParameter("year", year)
                .setParameter("type", DuesType.BIRTH)
                .getResultList();
    }

    @Override
    public Long findBirthDuesByYearAndMonth(int year, int month) {
        return em.createQuery("select sum(d.amount) from Dues d where year(d.date) = :year and month(d.date) = :month and d.type = :type and d.calc = :calc", Long.class)
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("type", DuesType.BIRTH)
                .setParameter("calc", DuesCalcType.PLUS)
                .getSingleResult();
    }

    @Override
    public List<UsersMonthBirthDuesDto> findUsersMonthBirthDues(int year) {
        return em.createQuery(
                        "select new com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto(d.userName, month(d.date), sum(d.amount), d.detail) " +
                                "from Dues d, User u " +
                                "where d.userName = u.name and u.company != :systemCompany " +
                                "and year(d.date) = :year and d.type = :type and d.calc = :calc " +
                                "group by d.userName, month(d.date), d.detail", UsersMonthBirthDuesDto.class)
                .setParameter("systemCompany", OriginCompanyType.SYSTEM)
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
