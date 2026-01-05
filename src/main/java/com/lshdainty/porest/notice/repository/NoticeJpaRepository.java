package com.lshdainty.porest.notice.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.notice.domain.Notice;
import com.lshdainty.porest.notice.type.NoticeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository("noticeJpaRepository")
@RequiredArgsConstructor
public class NoticeJpaRepository implements NoticeRepository {
    private final EntityManager em;

    @Override
    public void save(Notice notice) {
        em.persist(notice);
    }

    @Override
    public Optional<Notice> findById(Long noticeId) {
        List<Notice> result = em.createQuery(
                        "select n from Notice n " +
                                "left join fetch n.writer " +
                                "where n.id = :noticeId and n.isDeleted = :isDeleted", Notice.class)
                .setParameter("noticeId", noticeId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Page<Notice> findNotices(Pageable pageable) {
        List<Notice> content = em.createQuery(
                        "select n from Notice n " +
                                "left join fetch n.writer " +
                                "where n.isDeleted = :isDeleted " +
                                "order by n.isPinned desc, n.createDate desc", Notice.class)
                .setParameter("isDeleted", YNType.N)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = em.createQuery(
                        "select count(n) from Notice n where n.isDeleted = :isDeleted", Long.class)
                .setParameter("isDeleted", YNType.N)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Notice> findNoticesByType(NoticeType type, Pageable pageable) {
        List<Notice> content = em.createQuery(
                        "select n from Notice n " +
                                "left join fetch n.writer " +
                                "where n.type = :type and n.isDeleted = :isDeleted " +
                                "order by n.isPinned desc, n.createDate desc", Notice.class)
                .setParameter("type", type)
                .setParameter("isDeleted", YNType.N)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = em.createQuery(
                        "select count(n) from Notice n where n.type = :type and n.isDeleted = :isDeleted", Long.class)
                .setParameter("type", type)
                .setParameter("isDeleted", YNType.N)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Notice> findNoticesByTitleContaining(String keyword, Pageable pageable) {
        List<Notice> content = em.createQuery(
                        "select n from Notice n " +
                                "left join fetch n.writer " +
                                "where lower(n.title) like lower(:keyword) and n.isDeleted = :isDeleted " +
                                "order by n.isPinned desc, n.createDate desc", Notice.class)
                .setParameter("keyword", "%" + keyword + "%")
                .setParameter("isDeleted", YNType.N)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = em.createQuery(
                        "select count(n) from Notice n where lower(n.title) like lower(:keyword) and n.isDeleted = :isDeleted", Long.class)
                .setParameter("keyword", "%" + keyword + "%")
                .setParameter("isDeleted", YNType.N)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Notice> findActiveNotices(LocalDate now, Pageable pageable) {
        List<Notice> content = em.createQuery(
                        "select n from Notice n " +
                                "left join fetch n.writer " +
                                "where n.startDate <= :now " +
                                "and (n.endDate >= :now or n.endDate is null) " +
                                "and n.isDeleted = :isDeleted " +
                                "order by n.isPinned desc, n.createDate desc", Notice.class)
                .setParameter("now", now)
                .setParameter("isDeleted", YNType.N)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = em.createQuery(
                        "select count(n) from Notice n " +
                                "where n.startDate <= :now " +
                                "and (n.endDate >= :now or n.endDate is null) " +
                                "and n.isDeleted = :isDeleted", Long.class)
                .setParameter("now", now)
                .setParameter("isDeleted", YNType.N)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Notice> findPinnedNotices(Pageable pageable) {
        List<Notice> content = em.createQuery(
                        "select n from Notice n " +
                                "left join fetch n.writer " +
                                "where n.isPinned = :isPinned and n.isDeleted = :isDeleted " +
                                "order by n.createDate desc", Notice.class)
                .setParameter("isPinned", YNType.Y)
                .setParameter("isDeleted", YNType.N)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = em.createQuery(
                        "select count(n) from Notice n where n.isPinned = :isPinned and n.isDeleted = :isDeleted", Long.class)
                .setParameter("isPinned", YNType.Y)
                .setParameter("isDeleted", YNType.N)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
