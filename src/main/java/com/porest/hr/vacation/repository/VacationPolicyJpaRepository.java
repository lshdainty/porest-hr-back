package com.porest.hr.vacation.repository;

import com.porest.core.type.YNType;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.type.VacationType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("vacationPolicyJpaRepository")
@RequiredArgsConstructor
public class VacationPolicyJpaRepository implements VacationPolicyRepository {
    private final EntityManager em;

    @Override
    public void save(VacationPolicy vacationPolicy) {
        em.persist(vacationPolicy);
    }

    @Override
    public Optional<VacationPolicy> findVacationPolicyById(Long vacationPolicyId) {
        return Optional.ofNullable(em.find(VacationPolicy.class, vacationPolicyId));
    }

    @Override
    public List<VacationPolicy> findVacationPolicies() {
        return em.createQuery(
                        "select vp from VacationPolicy vp where vp.isDeleted = :isDeleted", VacationPolicy.class)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public boolean existsByName(String name) {
        Long count = em.createQuery(
                        "select count(vp) from VacationPolicy vp where vp.name = :name and vp.isDeleted = :isDeleted", Long.class)
                .setParameter("name", name)
                .setParameter("isDeleted", YNType.N)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public List<VacationPolicy> findByVacationType(VacationType vacationType) {
        return em.createQuery(
                        "select vp from VacationPolicy vp where vp.vacationType = :vacationType and vp.isDeleted = :isDeleted", VacationPolicy.class)
                .setParameter("vacationType", vacationType)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }
}
