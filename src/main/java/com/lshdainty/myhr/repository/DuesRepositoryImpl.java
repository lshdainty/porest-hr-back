package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Dues;
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
    public void delete(Dues dues) {
        em.remove(dues);
    }
}
