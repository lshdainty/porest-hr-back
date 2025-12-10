package com.lshdainty.porest.notice.service;

import com.lshdainty.porest.notice.domain.Notice;
import com.lshdainty.porest.notice.service.dto.NoticeServiceDto;
import com.lshdainty.porest.notice.type.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 공지사항 서비스 인터페이스
 */
public interface NoticeService {

    /**
     * 공지사항 생성
     *
     * @param data 공지사항 생성 데이터
     * @return 생성된 공지사항 ID
     */
    Long createNotice(NoticeServiceDto data);

    /**
     * 공지사항 조회 (조회수 증가 없음)
     *
     * @param noticeId 공지사항 ID
     * @return 공지사항 정보
     */
    NoticeServiceDto searchNotice(Long noticeId);

    /**
     * 공지사항 조회 및 조회수 증가
     *
     * @param noticeId 공지사항 ID
     * @return 공지사항 정보
     */
    NoticeServiceDto searchNoticeAndIncreaseViewCount(Long noticeId);

    /**
     * 공지사항 목록 조회
     *
     * @param pageable 페이징 정보
     * @return 공지사항 목록
     */
    Page<NoticeServiceDto> searchNotices(Pageable pageable);

    /**
     * 유형별 공지사항 목록 조회
     *
     * @param type 공지사항 유형
     * @param pageable 페이징 정보
     * @return 공지사항 목록
     */
    Page<NoticeServiceDto> searchNoticesByType(NoticeType type, Pageable pageable);

    /**
     * 키워드 검색으로 공지사항 목록 조회
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 공지사항 목록
     */
    Page<NoticeServiceDto> searchNoticesByKeyword(String keyword, Pageable pageable);

    /**
     * 활성 공지사항 목록 조회 (현재 날짜 기준 표시 기간 내)
     *
     * @param pageable 페이징 정보
     * @return 공지사항 목록
     */
    Page<NoticeServiceDto> searchActiveNotices(Pageable pageable);

    /**
     * 고정 공지사항 목록 조회
     *
     * @param pageable 페이징 정보
     * @return 공지사항 목록
     */
    Page<NoticeServiceDto> searchPinnedNotices(Pageable pageable);

    /**
     * 공지사항 수정
     *
     * @param noticeId 공지사항 ID
     * @param data 수정할 공지사항 데이터
     */
    void updateNotice(Long noticeId, NoticeServiceDto data);

    /**
     * 공지사항 삭제 (Soft Delete)
     *
     * @param noticeId 공지사항 ID
     */
    void deleteNotice(Long noticeId);

    /**
     * 공지사항 존재 여부 확인
     *
     * @param noticeId 공지사항 ID
     * @return 공지사항 엔티티
     * @throws com.lshdainty.porest.common.exception.EntityNotFoundException 공지사항이 존재하지 않거나 삭제된 경우
     */
    Notice checkNoticeExist(Long noticeId);
}
