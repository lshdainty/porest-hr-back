package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.notice.domain.Notice;
import com.lshdainty.porest.notice.repository.NoticeRepository;
import com.lshdainty.porest.notice.service.NoticeService;
import com.lshdainty.porest.notice.service.NoticeServiceImpl;
import com.lshdainty.porest.notice.service.dto.NoticeServiceDto;
import com.lshdainty.porest.notice.type.NoticeType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("공지사항 서비스 테스트")
class NoticeServiceTest {
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private NoticeServiceImpl noticeService;

    @Nested
    @DisplayName("공지사항 등록")
    class CreateNotice {
        @Test
        @DisplayName("성공 - 공지사항이 정상적으로 저장된다")
        void createNoticeSuccess() {
            // given
            String writerId = "admin";
            User writer = User.createUser(writerId);
            LocalDate start = LocalDate.now();
            LocalDate end = start.plusDays(30);

            NoticeServiceDto data = NoticeServiceDto.builder()
                    .writerId(writerId)
                    .title("시스템 점검 안내")
                    .content("점검 내용입니다.")
                    .type(NoticeType.MAINTENANCE)
                    .isPinned(YNType.N)
                    .startDate(start)
                    .endDate(end)
                    .build();

            given(userService.checkUserExist(writerId)).willReturn(writer);
            willDoNothing().given(noticeRepository).save(any(Notice.class));

            // when
            noticeService.createNotice(data);

            // then
            then(userService).should().checkUserExist(writerId);
            then(noticeRepository).should().save(any(Notice.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자로 등록하면 예외가 발생한다")
        void createNoticeFailUserNotFound() {
            // given
            String writerId = "nonexistent";
            NoticeServiceDto data = NoticeServiceDto.builder()
                    .writerId(writerId)
                    .title("테스트")
                    .content("내용")
                    .build();

            given(userService.checkUserExist(writerId))
                    .willThrow(new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> noticeService.createNotice(data))
                    .isInstanceOf(EntityNotFoundException.class);
            then(userService).should().checkUserExist(writerId);
        }

        @Test
        @DisplayName("실패 - 시작일이 종료일보다 늦으면 예외가 발생한다")
        void createNoticeFailStartAfterEnd() {
            // given
            String writerId = "admin";
            User writer = User.createUser(writerId);
            LocalDate start = LocalDate.now().plusDays(30);
            LocalDate end = LocalDate.now();

            NoticeServiceDto data = NoticeServiceDto.builder()
                    .writerId(writerId)
                    .title("테스트")
                    .content("내용")
                    .startDate(start)
                    .endDate(end)
                    .build();

            given(userService.checkUserExist(writerId)).willReturn(writer);

            // when & then
            assertThatThrownBy(() -> noticeService.createNotice(data))
                    .isInstanceOf(InvalidValueException.class);
        }
    }

    @Nested
    @DisplayName("공지사항 조회")
    class SearchNotice {
        @Test
        @DisplayName("성공 - 공지사항 상세 조회")
        void searchNoticeSuccess() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "테스트 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            setNoticeId(notice, noticeId);

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when
            NoticeServiceDto result = noticeService.searchNotice(noticeId);

            // then
            then(noticeRepository).should().findById(noticeId);
            assertThat(result.getTitle()).isEqualTo("테스트 공지");
            assertThat(result.getType()).isEqualTo(NoticeType.GENERAL);
        }

        @Test
        @DisplayName("성공 - 조회수 증가와 함께 조회")
        void searchNoticeAndIncreaseViewCountSuccess() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "테스트 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            setNoticeId(notice, noticeId);

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when
            NoticeServiceDto result = noticeService.searchNoticeAndIncreaseViewCount(noticeId);

            // then
            assertThat(result.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공지사항 조회시 예외가 발생한다")
        void searchNoticeFailNotFound() {
            // given
            Long noticeId = 999L;
            given(noticeRepository.findById(noticeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.searchNotice(noticeId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 공지사항 조회시 예외가 발생한다")
        void searchNoticeFailDeleted() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "삭제된 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            notice.deleteNotice();

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when & then
            assertThatThrownBy(() -> noticeService.searchNotice(noticeId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("공지사항 목록 조회")
    class SearchNotices {
        @Test
        @DisplayName("성공 - 공지사항 목록 페이지네이션 조회")
        void searchNoticesSuccess() {
            // given
            User writer = User.createUser("admin");
            Notice notice1 = Notice.createNotice(
                    writer, "공지1", "내용1", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            Notice notice2 = Notice.createNotice(
                    writer, "공지2", "내용2", NoticeType.URGENT,
                    YNType.Y, LocalDate.now(), LocalDate.now().plusDays(30)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(List.of(notice2, notice1), pageable, 2);

            given(noticeRepository.findNotices(pageable)).willReturn(noticePage);

            // when
            Page<NoticeServiceDto> result = noticeService.searchNotices(pageable);

            // then
            then(noticeRepository).should().findNotices(pageable);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 유형별 공지사항 조회")
        void searchNoticesByTypeSuccess() {
            // given
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "긴급 공지", "내용", NoticeType.URGENT,
                    YNType.Y, LocalDate.now(), LocalDate.now().plusDays(30)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(List.of(notice), pageable, 1);

            given(noticeRepository.findNoticesByType(NoticeType.URGENT, pageable)).willReturn(noticePage);

            // when
            Page<NoticeServiceDto> result = noticeService.searchNoticesByType(NoticeType.URGENT, pageable);

            // then
            then(noticeRepository).should().findNoticesByType(NoticeType.URGENT, pageable);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getType()).isEqualTo(NoticeType.URGENT);
        }

        @Test
        @DisplayName("성공 - 키워드로 공지사항 검색")
        void searchNoticesByKeywordSuccess() {
            // given
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "시스템 점검 안내", "내용", NoticeType.MAINTENANCE,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(List.of(notice), pageable, 1);

            given(noticeRepository.findNoticesByTitleContaining("점검", pageable)).willReturn(noticePage);

            // when
            Page<NoticeServiceDto> result = noticeService.searchNoticesByKeyword("점검", pageable);

            // then
            then(noticeRepository).should().findNoticesByTitleContaining("점검", pageable);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 활성 공지사항 조회")
        void searchActiveNoticesSuccess() {
            // given
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "활성 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(List.of(notice), pageable, 1);

            given(noticeRepository.findActiveNotices(any(LocalDate.class), any(Pageable.class)))
                    .willReturn(noticePage);

            // when
            Page<NoticeServiceDto> result = noticeService.searchActiveNotices(pageable);

            // then
            then(noticeRepository).should().findActiveNotices(any(LocalDate.class), any(Pageable.class));
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 고정 공지사항 조회")
        void searchPinnedNoticesSuccess() {
            // given
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "고정 공지", "내용", NoticeType.URGENT,
                    YNType.Y, LocalDate.now(), LocalDate.now().plusDays(30)
            );

            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(List.of(notice), pageable, 1);

            given(noticeRepository.findPinnedNotices(pageable)).willReturn(noticePage);

            // when
            Page<NoticeServiceDto> result = noticeService.searchPinnedNotices(pageable);

            // then
            then(noticeRepository).should().findPinnedNotices(pageable);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getIsPinned()).isEqualTo(YNType.Y);
        }
    }

    @Nested
    @DisplayName("공지사항 수정")
    class UpdateNotice {
        @Test
        @DisplayName("성공 - 공지사항이 정상적으로 수정된다")
        void updateNoticeSuccess() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "원본 제목", "원본 내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            setNoticeId(notice, noticeId);

            NoticeServiceDto updateData = NoticeServiceDto.builder()
                    .title("수정 제목")
                    .content("수정 내용")
                    .type(NoticeType.URGENT)
                    .isPinned(YNType.Y)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(60))
                    .build();

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when
            noticeService.updateNotice(noticeId, updateData);

            // then
            assertThat(notice.getTitle()).isEqualTo("수정 제목");
            assertThat(notice.getContent()).isEqualTo("수정 내용");
            assertThat(notice.getType()).isEqualTo(NoticeType.URGENT);
            assertThat(notice.getIsPinned()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공지사항 수정시 예외가 발생한다")
        void updateNoticeFailNotFound() {
            // given
            Long noticeId = 999L;
            NoticeServiceDto updateData = NoticeServiceDto.builder()
                    .title("수정 제목")
                    .build();

            given(noticeRepository.findById(noticeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.updateNotice(noticeId, updateData))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 시작일이 종료일보다 늦으면 예외가 발생한다")
        void updateNoticeFailInvalidDate() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "원본 제목", "원본 내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            setNoticeId(notice, noticeId);

            NoticeServiceDto updateData = NoticeServiceDto.builder()
                    .title("수정 제목")
                    .startDate(LocalDate.now().plusDays(30))
                    .endDate(LocalDate.now())
                    .build();

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when & then
            assertThatThrownBy(() -> noticeService.updateNotice(noticeId, updateData))
                    .isInstanceOf(InvalidValueException.class);
        }
    }

    @Nested
    @DisplayName("공지사항 삭제")
    class DeleteNotice {
        @Test
        @DisplayName("성공 - 공지사항이 정상적으로 삭제된다")
        void deleteNoticeSuccess() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "삭제할 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            setNoticeId(notice, noticeId);

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when
            noticeService.deleteNotice(noticeId);

            // then
            assertThat(notice.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공지사항 삭제시 예외가 발생한다")
        void deleteNoticeFailNotFound() {
            // given
            Long noticeId = 999L;
            given(noticeRepository.findById(noticeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.deleteNotice(noticeId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 이미 삭제된 공지사항 삭제시 예외가 발생한다")
        void deleteNoticeFailAlreadyDeleted() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "삭제된 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            notice.deleteNotice();

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when & then
            assertThatThrownBy(() -> noticeService.deleteNotice(noticeId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("공지사항 존재 확인")
    class CheckNoticeExist {
        @Test
        @DisplayName("성공 - 존재하는 공지사항을 반환한다")
        void checkNoticeExistSuccess() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "테스트 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            setNoticeId(notice, noticeId);

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when
            Notice result = noticeService.checkNoticeExist(noticeId);

            // then
            assertThat(result).isEqualTo(notice);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공지사항이면 예외가 발생한다")
        void checkNoticeExistFailNotFound() {
            // given
            Long noticeId = 999L;
            given(noticeRepository.findById(noticeId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.checkNoticeExist(noticeId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 공지사항이면 예외가 발생한다")
        void checkNoticeExistFailDeleted() {
            // given
            Long noticeId = 1L;
            User writer = User.createUser("admin");
            Notice notice = Notice.createNotice(
                    writer, "삭제된 공지", "내용", NoticeType.GENERAL,
                    YNType.N, LocalDate.now(), LocalDate.now().plusDays(30)
            );
            notice.deleteNotice();

            given(noticeRepository.findById(noticeId)).willReturn(Optional.of(notice));

            // when & then
            assertThatThrownBy(() -> noticeService.checkNoticeExist(noticeId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // 테스트 헬퍼 메서드
    private void setNoticeId(Notice notice, Long id) {
        try {
            java.lang.reflect.Field field = Notice.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(notice, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
