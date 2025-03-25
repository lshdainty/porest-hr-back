package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Dues;
import com.lshdainty.myhr.domain.QDues;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.lshdainty.myhr.domain.QDues.dues;

@Repository
@RequiredArgsConstructor
public class DuesCustomRepositoryImpl implements DuesCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(Dues dues) {

    }

    @Override
    public Dues findById(Long id) {
        return jpaQueryFactory
                .selectFrom(dues)
                .where(dues.seq.eq(id))
                .fetchOne();
    }

    @Override
    public List<Dues> findDues() {
        return List.of();
    }

    @Override
    public List<Dues> findDuesByYear(String year) {
        return List.of();
    }

    @Override
    public void delete(Dues dues) {

    }
}
