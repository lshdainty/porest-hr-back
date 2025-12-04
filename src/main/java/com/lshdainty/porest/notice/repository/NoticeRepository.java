package com.lshdainty.porest.notice.repository;

import com.lshdainty.porest.notice.domain.Notice;
import com.lshdainty.porest.notice.type.NoticeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NoticeRepository {
    void save(Notice notice);

    Optional<Notice> findById(Long noticeId);

    Page<Notice> findNotices(Pageable pageable);

    Page<Notice> findNoticesByType(NoticeType type, Pageable pageable);

    Page<Notice> findNoticesByTitleContaining(String keyword, Pageable pageable);

    Page<Notice> findActiveNotices(LocalDateTime now, Pageable pageable);

    Page<Notice> findPinnedNotices(Pageable pageable);

    long countActiveNotices(LocalDateTime now);
}
