package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.VacationPolicy;
import com.lshdainty.porest.type.YNType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.domain.QVacationPolicy.vacationPolicy;

@Repository
@RequiredArgsConstructor
public class VacationPolicyCustomRepositoryImpl implements VacationPolicyCustomRepository {
    private final EntityManager em;

    private final JPAQueryFactory query;

    @Override
    public void save(VacationPolicy vacationPolicy) {
        em.persist(vacationPolicy);
    }

    @Override
    public Optional<VacationPolicy> findVacationPolicyById(Long vacationPolicyId) {
        return Optional.ofNullable(query
                .selectFrom(vacationPolicy)
                .where(vacationPolicy.id.eq(vacationPolicyId))
                .fetchOne()
        );
    }

    @Override
    public List<VacationPolicy> findVacationPolicies() {
        return query
                .selectFrom(vacationPolicy)
                .where(vacationPolicy.isDeleted.eq(YNType.N))
                .fetch();
    }
}
