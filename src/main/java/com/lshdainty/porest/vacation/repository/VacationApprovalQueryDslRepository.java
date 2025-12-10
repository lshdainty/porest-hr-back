package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationApproval;
import com.lshdainty.porest.vacation.type.ApprovalStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.vacation.domain.QVacationApproval.vacationApproval;

@Repository
@Primary
@RequiredArgsConstructor
public class VacationApprovalQueryDslRepository implements VacationApprovalRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(VacationApproval vacationApproval) {
        em.persist(vacationApproval);
    }

    @Override
    public void saveAll(List<VacationApproval> vacationApprovals) {
        for (VacationApproval va : vacationApprovals) {
            em.persist(va);
        }
    }

    @Override
    public List<VacationApproval> findByVacationGrantId(Long vacationGrantId) {
        return query
                .selectFrom(vacationApproval)
                .join(vacationApproval.vacationGrant).fetchJoin()
                .join(vacationApproval.approver).fetchJoin()
                .where(vacationApproval.vacationGrant.id.eq(vacationGrantId)
                        .and(vacationApproval.isDeleted.eq(YNType.N)))
                .fetch();
    }

    @Override
    public List<Long> findAllVacationGrantIdsByApproverId(String approverId) {
        return query
                .select(vacationApproval.vacationGrant.id)
                .from(vacationApproval)
                .where(vacationApproval.approver.id.eq(approverId)
                        .and(vacationApproval.isDeleted.eq(YNType.N)))
                .distinct()
                .fetch();
    }

    @Override
    public List<Long> findAllVacationGrantIdsByApproverIdAndYear(String approverId, Integer year) {
        return query
                .select(vacationApproval.vacationGrant.id)
                .from(vacationApproval)
                .where(vacationApproval.approver.id.eq(approverId)
                        .and(vacationApproval.isDeleted.eq(YNType.N))
                        .and(vacationApproval.vacationGrant.createDate.year().eq(year)))
                .distinct()
                .fetch();
    }

    @Override
    public Optional<VacationApproval> findById(Long id) {
        VacationApproval result = query
                .selectFrom(vacationApproval)
                .where(vacationApproval.id.eq(id)
                        .and(vacationApproval.isDeleted.eq(YNType.N)))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<VacationApproval> findByIdWithVacationGrantAndUser(Long id) {
        VacationApproval result = query
                .selectFrom(vacationApproval)
                .join(vacationApproval.vacationGrant).fetchJoin()
                .join(vacationApproval.vacationGrant.user).fetchJoin()
                .join(vacationApproval.vacationGrant.policy).fetchJoin()
                .join(vacationApproval.approver).fetchJoin()
                .where(vacationApproval.id.eq(id)
                        .and(vacationApproval.isDeleted.eq(YNType.N)))
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
