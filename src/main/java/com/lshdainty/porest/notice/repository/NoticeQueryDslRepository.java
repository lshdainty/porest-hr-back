package com.lshdainty.porest.notice.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.notice.domain.Notice;
import com.lshdainty.porest.notice.type.NoticeType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.notice.domain.QNotice.notice;

@Repository
@Primary
@RequiredArgsConstructor
public class NoticeQueryDslRepository implements NoticeRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(Notice notice) {
        em.persist(notice);
    }

    @Override
    public Optional<Notice> findById(Long noticeId) {
        return Optional.ofNullable(
                query
                        .selectFrom(notice)
                        .leftJoin(notice.writer).fetchJoin()
                        .where(notice.id.eq(noticeId)
                                .and(isNotDeleted()))
                        .fetchOne()
        );
    }

    @Override
    public Page<Notice> findNotices(Pageable pageable) {
        List<Notice> content = query
                .selectFrom(notice)
                .leftJoin(notice.writer).fetchJoin()
                .where(isNotDeleted())
                .orderBy(notice.isPinned.desc(), notice.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(notice.count())
                .from(notice)
                .where(isNotDeleted());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Notice> findNoticesByType(NoticeType type, Pageable pageable) {
        List<Notice> content = query
                .selectFrom(notice)
                .leftJoin(notice.writer).fetchJoin()
                .where(notice.type.eq(type)
                        .and(isNotDeleted()))
                .orderBy(notice.isPinned.desc(), notice.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(notice.count())
                .from(notice)
                .where(notice.type.eq(type)
                        .and(isNotDeleted()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Notice> findNoticesByTitleContaining(String keyword, Pageable pageable) {
        List<Notice> content = query
                .selectFrom(notice)
                .leftJoin(notice.writer).fetchJoin()
                .where(notice.title.containsIgnoreCase(keyword)
                        .and(isNotDeleted()))
                .orderBy(notice.isPinned.desc(), notice.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(notice.count())
                .from(notice)
                .where(notice.title.containsIgnoreCase(keyword)
                        .and(isNotDeleted()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Notice> findActiveNotices(LocalDate now, Pageable pageable) {
        List<Notice> content = query
                .selectFrom(notice)
                .leftJoin(notice.writer).fetchJoin()
                .where(isActiveNotice(now)
                        .and(isNotDeleted()))
                .orderBy(notice.isPinned.desc(), notice.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(notice.count())
                .from(notice)
                .where(isActiveNotice(now)
                        .and(isNotDeleted()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Notice> findPinnedNotices(Pageable pageable) {
        List<Notice> content = query
                .selectFrom(notice)
                .leftJoin(notice.writer).fetchJoin()
                .where(notice.isPinned.eq(YNType.Y)
                        .and(isNotDeleted()))
                .orderBy(notice.createDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query
                .select(notice.count())
                .from(notice)
                .where(notice.isPinned.eq(YNType.Y)
                        .and(isNotDeleted()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public long countActiveNotices(LocalDate now) {
        Long count = query
                .select(notice.count())
                .from(notice)
                .where(isActiveNotice(now)
                        .and(isNotDeleted()))
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression isNotDeleted() {
        return notice.isDeleted.eq(YNType.N);
    }

    private BooleanExpression isActiveNotice(LocalDate now) {
        return notice.startDate.loe(now)
                .and(notice.endDate.goe(now).or(notice.endDate.isNull()));
    }
}
