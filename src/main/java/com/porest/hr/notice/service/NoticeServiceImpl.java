package com.porest.hr.notice.service;

import com.porest.core.exception.EntityNotFoundException;
import com.porest.hr.common.exception.HrErrorCode;
import com.porest.core.exception.InvalidValueException;
import com.porest.core.type.YNType;
import com.porest.hr.notice.domain.Notice;
import com.porest.hr.notice.repository.NoticeRepository;
import com.porest.hr.notice.service.dto.NoticeServiceDto;
import com.porest.hr.notice.type.NoticeType;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Long createNotice(NoticeServiceDto data) {
        log.debug("공지사항 등록 시작: writerId={}, title={}", data.getWriterId(), data.getTitle());

        User writer = userService.checkUserExist(data.getWriterId());

        // 날짜 범위 검증은 @DateRange 어노테이션으로 Controller 레벨에서 처리

        Notice notice = Notice.createNotice(
                writer,
                data.getTitle(),
                data.getContent(),
                data.getType(),
                data.getIsPinned() != null ? data.getIsPinned() : YNType.N,
                data.getStartDate(),
                data.getEndDate()
        );

        noticeRepository.save(notice);
        log.info("공지사항 등록 완료: noticeId={}, writerId={}", notice.getRowId(), data.getWriterId());

        return notice.getRowId();
    }

    @Override
    public NoticeServiceDto searchNotice(Long noticeId) {
        log.debug("공지사항 조회: noticeId={}", noticeId);
        Notice notice = checkNoticeExist(noticeId);
        return convertToDto(notice);
    }

    @Override
    @Transactional
    public NoticeServiceDto searchNoticeAndIncreaseViewCount(Long noticeId) {
        log.debug("공지사항 조회 및 조회수 증가: noticeId={}", noticeId);
        Notice notice = checkNoticeExist(noticeId);
        notice.increaseViewCount();
        return convertToDto(notice);
    }

    @Override
    public Page<NoticeServiceDto> searchNotices(Pageable pageable) {
        log.debug("공지사항 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return noticeRepository.findNotices(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<NoticeServiceDto> searchNoticesByType(NoticeType type, Pageable pageable) {
        log.debug("공지사항 유형별 조회: type={}, page={}", type, pageable.getPageNumber());
        return noticeRepository.findNoticesByType(type, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<NoticeServiceDto> searchNoticesByKeyword(String keyword, Pageable pageable) {
        log.debug("공지사항 키워드 검색: keyword={}, page={}", keyword, pageable.getPageNumber());
        return noticeRepository.findNoticesByTitleContaining(keyword, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<NoticeServiceDto> searchActiveNotices(Pageable pageable) {
        log.debug("활성 공지사항 조회: page={}", pageable.getPageNumber());
        LocalDate now = LocalDate.now();
        return noticeRepository.findActiveNotices(now, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<NoticeServiceDto> searchPinnedNotices(Pageable pageable) {
        log.debug("고정 공지사항 조회: page={}", pageable.getPageNumber());
        return noticeRepository.findPinnedNotices(pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public void updateNotice(Long noticeId, NoticeServiceDto data) {
        log.debug("공지사항 수정 시작: noticeId={}", noticeId);

        Notice notice = checkNoticeExist(noticeId);

        // 날짜 범위 검증은 @DateRange 어노테이션으로 Controller 레벨에서 처리

        notice.updateNotice(
                data.getTitle(),
                data.getContent(),
                data.getType(),
                data.getIsPinned(),
                data.getStartDate(),
                data.getEndDate()
        );

        log.info("공지사항 수정 완료: noticeId={}", noticeId);
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        log.debug("공지사항 삭제 시작: noticeId={}", noticeId);

        Notice notice = checkNoticeExist(noticeId);
        notice.deleteNotice();

        log.info("공지사항 삭제 완료: noticeId={}", noticeId);
    }

    @Override
    public Notice checkNoticeExist(Long noticeId) {
        Optional<Notice> notice = noticeRepository.findByRowId(noticeId);
        if (notice.isEmpty() || YNType.isY(notice.get().getIsDeleted())) {
            log.warn("공지사항 조회 실패 - 존재하지 않는 공지사항: noticeId={}", noticeId);
            throw new EntityNotFoundException(HrErrorCode.NOTICE_NOT_FOUND);
        }
        return notice.get();
    }

    private NoticeServiceDto convertToDto(Notice notice) {
        return NoticeServiceDto.builder()
                .id(notice.getRowId())
                .writerId(notice.getWriter() != null ? notice.getWriter().getId() : null)
                .writerName(notice.getWriter() != null ? notice.getWriter().getName() : null)
                .title(notice.getTitle())
                .content(notice.getContent())
                .type(notice.getType())
                .isPinned(notice.getIsPinned())
                .viewCount(notice.getViewCount())
                .startDate(notice.getStartDate())
                .endDate(notice.getEndDate())
                .createDate(notice.getCreateAt())
                .modifyDate(notice.getModifyAt())
                .build();
    }
}
