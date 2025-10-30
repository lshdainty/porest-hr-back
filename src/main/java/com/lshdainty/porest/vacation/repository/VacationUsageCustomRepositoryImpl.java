package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.vacation.domain.QVacationUsage.vacationUsage;

@Repository
@RequiredArgsConstructor
public class VacationUsageCustomRepositoryImpl implements VacationUsageCustomRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(VacationUsage vacationUsage) {
        em.persist(vacationUsage);
    }

    @Override
    public void saveAll(List<VacationUsage> vacationUsages) {
        for (VacationUsage vu : vacationUsages) {
            em.persist(vu);
        }
    }

    @Override
    public Optional<VacationUsage> findById(Long vacationUsageId) {
        return Optional.ofNullable(query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.id.eq(vacationUsageId))
                .fetchOne()
        );
    }

    @Override
    public List<VacationUsage> findByUserId(String userId) {
        return query
                .selectFrom(vacationUsage)
                .join(vacationUsage.user).fetchJoin()
                .where(vacationUsage.user.id.eq(userId)
                        .and(vacationUsage.isDeleted.eq(YNType.N)))
                .fetch();
    }
}