package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.notice.domain.Notice;
import com.lshdainty.porest.notice.repository.NoticeQueryDslRepository;
import com.lshdainty.porest.notice.type.NoticeType;
import com.lshdainty.porest.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({NoticeQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 공지사항 레포지토리 테스트")
class NoticeQueryDslRepositoryTest {
    @Autowired
    private NoticeQueryDslRepository noticeRepository;

    @Autowired
    private TestEntityManager em;

    private User writer;

    @BeforeEach
    void setUp() {
        writer = User.createUser("admin");
        em.persist(writer);
    }

    @Nested
    @DisplayName("공지사항 저장 및 조회")
    class SaveAndFind {
        @Test
        @DisplayName("공지사항 저장 및 단건 조회")
        void saveAndFindById() {
            // given
            Notice notice = Notice.createNotice(
                    writer, "테스트 공지", "테스트 내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );

            // when
            noticeRepository.save(notice);
            em.flush();
            em.clear();

            // then
            Optional<Notice> findNotice = noticeRepository.findById(notice.getId());
            assertThat(findNotice.isPresent()).isTrue();
            assertThat(findNotice.get().getTitle()).isEqualTo("테스트 공지");
            assertThat(findNotice.get().getContent()).isEqualTo("테스트 내용");
            assertThat(findNotice.get().getType()).isEqualTo(NoticeType.GENERAL);
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 조회시 빈 Optional 반환")
        void findByIdEmpty() {
            // when
            Optional<Notice> findNotice = noticeRepository.findById(999L);

            // then
            assertThat(findNotice.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("삭제된 공지사항 조회시 빈 Optional 반환")
        void findByIdDeletedNotice() {
            // given
            Notice notice = Notice.createNotice(
                    writer, "삭제될 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(notice);
            notice.deleteNotice();
            em.flush();
            em.clear();

            // when
            Optional<Notice> findNotice = noticeRepository.findById(notice.getId());

            // then
            assertThat(findNotice.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("공지사항 목록 조회")
    class FindNotices {
        @Test
        @DisplayName("전체 공지사항 목록 조회 - 고정 공지 먼저 정렬")
        void findNoticesWithPinnedFirst() {
            // given
            Notice normalNotice = Notice.createNotice(
                    writer, "일반 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            Notice pinnedNotice = Notice.createNotice(
                    writer, "고정 공지", "내용", NoticeType.URGENT,
                    YNType.Y, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(normalNotice);
            noticeRepository.save(pinnedNotice);
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> notices = noticeRepository.findNotices(pageable);

            // then
            assertThat(notices.getTotalElements()).isEqualTo(2);
            assertThat(notices.getContent().get(0).getIsPinned()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("삭제된 공지사항 제외하고 조회")
        void findNoticesExcludesDeleted() {
            // given
            Notice activeNotice = Notice.createNotice(
                    writer, "활성 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            Notice deletedNotice = Notice.createNotice(
                    writer, "삭제 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(activeNotice);
            noticeRepository.save(deletedNotice);
            deletedNotice.deleteNotice();
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> notices = noticeRepository.findNotices(pageable);

            // then
            assertThat(notices.getTotalElements()).isEqualTo(1);
            assertThat(notices.getContent().get(0).getTitle()).isEqualTo("활성 공지");
        }
    }

    @Nested
    @DisplayName("유형별 공지사항 조회")
    class FindNoticesByType {
        @Test
        @DisplayName("특정 유형의 공지사항만 조회")
        void findNoticesByType() {
            // given
            Notice generalNotice = Notice.createNotice(
                    writer, "일반 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            Notice urgentNotice = Notice.createNotice(
                    writer, "긴급 공지", "내용", NoticeType.URGENT,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(generalNotice);
            noticeRepository.save(urgentNotice);
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> notices = noticeRepository.findNoticesByType(NoticeType.URGENT, pageable);

            // then
            assertThat(notices.getTotalElements()).isEqualTo(1);
            assertThat(notices.getContent().get(0).getType()).isEqualTo(NoticeType.URGENT);
        }
    }

    @Nested
    @DisplayName("키워드 검색")
    class FindNoticesByKeyword {
        @Test
        @DisplayName("제목에 키워드를 포함하는 공지사항 검색")
        void findNoticesByTitleContaining() {
            // given
            Notice notice1 = Notice.createNotice(
                    writer, "시스템 점검 안내", "내용", NoticeType.MAINTENANCE,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            Notice notice2 = Notice.createNotice(
                    writer, "이벤트 안내", "내용", NoticeType.EVENT,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(notice1);
            noticeRepository.save(notice2);
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> notices = noticeRepository.findNoticesByTitleContaining("점검", pageable);

            // then
            assertThat(notices.getTotalElements()).isEqualTo(1);
            assertThat(notices.getContent().get(0).getTitle()).contains("점검");
        }

        @Test
        @DisplayName("키워드 검색 대소문자 무시")
        void findNoticesByTitleContainingIgnoreCase() {
            // given
            Notice notice = Notice.createNotice(
                    writer, "System Maintenance", "내용", NoticeType.MAINTENANCE,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(notice);
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> notices = noticeRepository.findNoticesByTitleContaining("system", pageable);

            // then
            assertThat(notices.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("활성 공지사항 조회")
    class FindActiveNotices {
        @Test
        @DisplayName("현재 노출 기간 내의 공지사항만 조회")
        void findActiveNotices() {
            // given
            LocalDate now = LocalDate.now();
            Notice activeNotice = Notice.createNotice(
                    writer, "활성 공지", "내용", NoticeType.GENERAL,
                    YNType.N, now.minusDays(1), now.plusDays(30)
            );
            Notice futureNotice = Notice.createNotice(
                    writer, "미래 공지", "내용", NoticeType.GENERAL,
                    YNType.N, now.plusDays(10), now.plusDays(30)
            );
            Notice expiredNotice = Notice.createNotice(
                    writer, "만료 공지", "내용", NoticeType.GENERAL,
                    YNType.N, now.minusDays(30), now.minusDays(1)
            );
            noticeRepository.save(activeNotice);
            noticeRepository.save(futureNotice);
            noticeRepository.save(expiredNotice);
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> notices = noticeRepository.findActiveNotices(now, pageable);

            // then
            assertThat(notices.getTotalElements()).isEqualTo(1);
            assertThat(notices.getContent().get(0).getTitle()).isEqualTo("활성 공지");
        }
    }

    @Nested
    @DisplayName("고정 공지사항 조회")
    class FindPinnedNotices {
        @Test
        @DisplayName("고정된 공지사항만 조회")
        void findPinnedNotices() {
            // given
            Notice pinnedNotice = Notice.createNotice(
                    writer, "고정 공지", "내용", NoticeType.URGENT,
                    YNType.Y, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            Notice normalNotice = Notice.createNotice(
                    writer, "일반 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(pinnedNotice);
            noticeRepository.save(normalNotice);
            em.flush();
            em.clear();

            // when
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> notices = noticeRepository.findPinnedNotices(pageable);

            // then
            assertThat(notices.getTotalElements()).isEqualTo(1);
            assertThat(notices.getContent().get(0).getIsPinned()).isEqualTo(YNType.Y);
        }
    }

    @Nested
    @DisplayName("활성 공지사항 수 조회")
    class CountActiveNotices {
        @Test
        @DisplayName("현재 활성화된 공지사항 수 반환")
        void countActiveNotices() {
            // given
            LocalDate now = LocalDate.now();
            Notice activeNotice1 = Notice.createNotice(
                    writer, "활성 공지1", "내용", NoticeType.GENERAL,
                    YNType.N, now.minusDays(1), now.plusDays(30)
            );
            Notice activeNotice2 = Notice.createNotice(
                    writer, "활성 공지2", "내용", NoticeType.GENERAL,
                    YNType.N, now.minusDays(1), now.plusDays(30)
            );
            Notice expiredNotice = Notice.createNotice(
                    writer, "만료 공지", "내용", NoticeType.GENERAL,
                    YNType.N, now.minusDays(30), now.minusDays(1)
            );
            noticeRepository.save(activeNotice1);
            noticeRepository.save(activeNotice2);
            noticeRepository.save(expiredNotice);
            em.flush();
            em.clear();

            // when
            long count = noticeRepository.countActiveNotices(now);

            // then
            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("공지사항 수정 및 삭제")
    class UpdateAndDelete {
        @Test
        @DisplayName("공지사항 수정")
        void updateNotice() {
            // given
            Notice notice = Notice.createNotice(
                    writer, "원본 제목", "원본 내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(notice);
            em.flush();
            em.clear();

            // when
            Notice foundNotice = noticeRepository.findById(notice.getId()).orElseThrow();
            foundNotice.updateNotice("수정 제목", "수정 내용", NoticeType.URGENT, YNType.Y, null, null);
            em.flush();
            em.clear();

            // then
            Notice updatedNotice = noticeRepository.findById(notice.getId()).orElseThrow();
            assertThat(updatedNotice.getTitle()).isEqualTo("수정 제목");
            assertThat(updatedNotice.getContent()).isEqualTo("수정 내용");
            assertThat(updatedNotice.getType()).isEqualTo(NoticeType.URGENT);
            assertThat(updatedNotice.getIsPinned()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("공지사항 삭제 (소프트 딜리트)")
        void deleteNotice() {
            // given
            Notice notice = Notice.createNotice(
                    writer, "삭제할 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(notice);
            em.flush();
            em.clear();

            // when
            Notice foundNotice = noticeRepository.findById(notice.getId()).orElseThrow();
            foundNotice.deleteNotice();
            em.flush();
            em.clear();

            // then
            Optional<Notice> deletedNotice = noticeRepository.findById(notice.getId());
            assertThat(deletedNotice.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("조회수 증가")
        void increaseViewCount() {
            // given
            Notice notice = Notice.createNotice(
                    writer, "조회수 테스트", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            noticeRepository.save(notice);
            em.flush();
            em.clear();

            // when
            Notice foundNotice = noticeRepository.findById(notice.getId()).orElseThrow();
            foundNotice.increaseViewCount();
            foundNotice.increaseViewCount();
            em.flush();
            em.clear();

            // then
            Notice updatedNotice = noticeRepository.findById(notice.getId()).orElseThrow();
            assertThat(updatedNotice.getViewCount()).isEqualTo(2L);
        }
    }
}
