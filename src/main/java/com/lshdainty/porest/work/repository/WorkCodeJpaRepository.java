package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.type.CodeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("workCodeJpaRepository")
@RequiredArgsConstructor
public class WorkCodeJpaRepository implements WorkCodeRepository {
    private final EntityManager em;

    @Override
    public void save(WorkCode workCode) {
        em.persist(workCode);
    }

    @Override
    public Optional<WorkCode> findByCode(String code) {
        List<WorkCode> result = em.createQuery(
                        "select wc from WorkCode wc where wc.code = :code and wc.isDeleted = :isDeleted", WorkCode.class)
                .setParameter("code", code)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<WorkCode> findBySeq(Long seq) {
        List<WorkCode> result = em.createQuery(
                        "select wc from WorkCode wc where wc.seq = :seq and wc.isDeleted = :isDeleted", WorkCode.class)
                .setParameter("seq", seq)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<WorkCode> findAllByConditions(String parentWorkCode, Long parentWorkCodeSeq, Boolean parentIsNull, CodeType type) {
        StringBuilder jpql = new StringBuilder("select wc from WorkCode wc where wc.isDeleted = :isDeleted");

        // 부모 코드 조건
        if (parentIsNull != null && parentIsNull) {
            jpql.append(" and wc.parent is null");
        } else if (parentWorkCodeSeq != null) {
            jpql.append(" and wc.parent.seq = :parentSeq");
        } else if (parentWorkCode != null && !parentWorkCode.isEmpty()) {
            jpql.append(" and wc.parent.code = :parentCode");
        }

        // 코드 타입 조건
        if (type != null) {
            jpql.append(" and wc.type = :type");
        }

        jpql.append(" order by wc.orderSeq asc");

        TypedQuery<WorkCode> query = em.createQuery(jpql.toString(), WorkCode.class)
                .setParameter("isDeleted", YNType.N);

        if (parentWorkCodeSeq != null && (parentIsNull == null || !parentIsNull)) {
            query.setParameter("parentSeq", parentWorkCodeSeq);
        } else if (parentWorkCode != null && !parentWorkCode.isEmpty() && (parentIsNull == null || !parentIsNull)) {
            query.setParameter("parentCode", parentWorkCode);
        }

        if (type != null) {
            query.setParameter("type", type);
        }

        return query.getResultList();
    }
}
