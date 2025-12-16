package com.lshdainty.porest.dues.repository;

import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.dues.domain.Dues;
import com.lshdainty.porest.dues.repository.dto.UsersMonthBirthDuesDto;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.dues.domain.QDues.dues;

@Repository
@Primary
@RequiredArgsConstructor
public class DuesQueryDslRepository implements DuesRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(Dues dues) {
        em.persist(dues);
    }

    @Override
    public Optional<Dues> findById(Long id) {
        return Optional.ofNullable(query
                .selectFrom(dues)
                .where(dues.id.eq(id))
                .fetchOne());
    }

    @Override
    public List<Dues> findDues() {
        return query
                .selectFrom(dues)
                .orderBy(dues.date.asc())
                .fetch();
    }

    @Override
    public List<Dues> findDuesByYear(int year) {
        return query
                .selectFrom(dues)
                .where(dues.date.year().eq(year))
                .orderBy(dues.date.asc())
                .fetch();
    }

    @Override
    public List<Dues> findOperatingDuesByYear(int year) {
        return query
                .selectFrom(dues)
                .where(dues.date.year().eq(year)
                        .and(dues.type.ne(DuesType.BIRTH)))
                .orderBy(dues.date.asc())
                .fetch();
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
