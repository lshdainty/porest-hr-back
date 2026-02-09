package com.porest.hr.common.repository;

import com.porest.core.type.YNType;
import com.porest.hr.common.domain.CompanyCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("companyCodeJpaRepository")
@RequiredArgsConstructor
public class CompanyCodeJpaRepository implements CompanyCodeRepository {
    private final EntityManager em;

    @Override
    public List<CompanyCode> findAllActive() {
        return em.createQuery(
                        "SELECT c FROM CompanyCode c WHERE c.isDeleted = :isDeleted ORDER BY c.sortOrder ASC",
                        CompanyCode.class)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public Optional<CompanyCode> findByCode(String code) {
        List<CompanyCode> result = em.createQuery(
                        "SELECT c FROM CompanyCode c WHERE c.code = :code AND c.isDeleted = :isDeleted",
                        CompanyCode.class)
                .setParameter("code", code)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
