package com.porest.hr.common.repository;

import com.porest.core.type.YNType;
import com.porest.hr.common.domain.CompanyCode;
import com.porest.hr.common.domain.QCompanyCode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class CompanyCodeQueryDslRepository implements CompanyCodeRepository {
    private final JPAQueryFactory queryFactory;

    private final QCompanyCode companyCode = QCompanyCode.companyCode;

    @Override
    public List<CompanyCode> findAllActive() {
        return queryFactory
                .selectFrom(companyCode)
                .where(companyCode.isDeleted.eq(YNType.N))
                .orderBy(companyCode.sortOrder.asc())
                .fetch();
    }

    @Override
    public Optional<CompanyCode> findByCode(String code) {
        CompanyCode result = queryFactory
                .selectFrom(companyCode)
                .where(
                        companyCode.code.eq(code),
                        companyCode.isDeleted.eq(YNType.N)
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
