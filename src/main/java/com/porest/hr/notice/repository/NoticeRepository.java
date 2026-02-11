package com.porest.hr.notice.repository;

import com.porest.hr.notice.domain.Notice;
import com.porest.hr.notice.type.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Notice Repository Interface
 */
public interface NoticeRepository {
    /**
     * 신규 공지사항 저장
     *
     * @param notice 저장할 공지사항
     */
    void save(Notice notice);

    /**
     * 단건 공지사항 조회
     *
     * @param rowId 공지사항 rowId
     * @return Optional&lt;Notice&gt;
     */
    Optional<Notice> findByRowId(Long rowId);

    /**
     * 전체 공지사항 페이징 조회
     *
     * @param pageable 페이징 정보
     * @return Page&lt;Notice&gt;
     */
    Page<Notice> findNotices(Pageable pageable);

    /**
     * 공지사항 타입별 페이징 조회
     *
     * @param type 공지사항 타입
     * @param pageable 페이징 정보
     * @return Page&lt;Notice&gt;
     */
    Page<Notice> findNoticesByType(NoticeType type, Pageable pageable);

    /**
     * 제목 키워드로 공지사항 검색
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return Page&lt;Notice&gt;
     */
    Page<Notice> findNoticesByTitleContaining(String keyword, Pageable pageable);

    /**
     * 활성 공지사항 페이징 조회 (게시 기간 내)
     *
     * @param now 현재 날짜
     * @param pageable 페이징 정보
     * @return Page&lt;Notice&gt;
     */
    Page<Notice> findActiveNotices(LocalDate now, Pageable pageable);

    /**
     * 상단 고정 공지사항 페이징 조회
     *
     * @param pageable 페이징 정보
     * @return Page&lt;Notice&gt;
     */
    Page<Notice> findPinnedNotices(Pageable pageable);
}
