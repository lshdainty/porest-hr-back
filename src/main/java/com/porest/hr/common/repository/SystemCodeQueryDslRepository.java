package com.porest.hr.common.repository;

import com.porest.core.type.YNType;
import com.porest.hr.common.domain.SystemCode;
import com.porest.hr.common.domain.QSystemCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class SystemCodeQueryDslRepository implements SystemCodeRepository {
    private final JPAQueryFactory queryFactory;

    private final QSystemCode systemCode = QSystemCode.systemCode;

    @Override
    public List<SystemCode> findAllActive() {
        return queryFactory
                .selectFrom(systemCode)
                .where(systemCode.isDeleted.eq(YNType.N))
                .orderBy(systemCode.sortOrder.asc())
                .fetch();
    }

    @Override
    public Optional<SystemCode> findByCode(String code) {
        SystemCode result = queryFactory
                .selectFrom(systemCode)
                .where(
                        systemCode.code.eq(code),
                        systemCode.isDeleted.eq(YNType.N)
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
