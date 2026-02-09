package com.porest.hr.common.repository;

import com.porest.core.type.YNType;
import com.porest.hr.common.domain.SystemCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("systemCodeJpaRepository")
@RequiredArgsConstructor
public class SystemCodeJpaRepository implements SystemCodeRepository {
    private final EntityManager em;

    @Override
    public List<SystemCode> findAllActive() {
        return em.createQuery(
                        "SELECT s FROM SystemCode s WHERE s.isDeleted = :isDeleted ORDER BY s.sortOrder ASC",
                        SystemCode.class)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public Optional<SystemCode> findByCode(String code) {
        List<SystemCode> result = em.createQuery(
                        "SELECT s FROM SystemCode s WHERE s.code = :code AND s.isDeleted = :isDeleted",
                        SystemCode.class)
                .setParameter("code", code)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}
