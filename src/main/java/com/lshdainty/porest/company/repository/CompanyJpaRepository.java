package com.lshdainty.porest.company.repository;

import com.lshdainty.porest.company.domain.Company;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("companyJpaRepository")
@RequiredArgsConstructor
public class CompanyJpaRepository implements CompanyRepository {
    private final EntityManager em;

    @Override
    public void save(Company company) {
        em.persist(company);
    }

    @Override
    public Optional<Company> find() {
        List<Company> result = em.createQuery("select c from Company c", Company.class)
                .setMaxResults(1)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Company> findById(String id) {
        return Optional.ofNullable(em.find(Company.class, id));
    }

    @Override
    public Optional<Company> findByIdWithDepartments(String id) {
        List<Company> result = em.createQuery(
                "select distinct c from Company c " +
                "left join fetch c.departments d " +
                "where c.id = :id and (d.parent is null or d is null)", Company.class)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
