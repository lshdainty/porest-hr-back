package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.repository.ScheduleRepository;
import com.lshdainty.porest.schedule.service.ScheduleService;
import com.lshdainty.porest.schedule.service.ScheduleServiceImpl;
import com.lshdainty.porest.schedule.service.dto.ScheduleServiceDto;
import com.lshdainty.porest.schedule.type.ScheduleType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("스케줄 서비스 테스트")
class ScheduleServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Nested
    @DisplayName("스케줄 등록")
    class RegistSchedule {
        @Test
        @DisplayName("성공 - 스케줄이 정상적으로 저장된다")
        void registScheduleSuccess() {
            // given
            String userId = "test1";
            User user = User.createUser(userId);
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.plusHours(8);

            ScheduleServiceDto data = ScheduleServiceDto.builder()
                    .userId(userId)
                    .type(ScheduleType.EDUCATION)
                    .desc("교육")
                    .startDate(start)
                    .endDate(end)
                    .build();

            given(userService.checkUserExist(userId)).willReturn(user);
            willDoNothing().given(scheduleRepository).save(any(Schedule.class));

            // when
            scheduleService.registSchedule(data);

            // then
            then(userService).should().checkUserExist(userId);
            then(scheduleRepository).should().save(any(Schedule.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자로 등록하면 예외가 발생한다")
        void registScheduleFailUserNotFound() {
            // given
            String userId = "nonexistent";
            ScheduleServiceDto data = ScheduleServiceDto.builder()
                    .userId(userId)
                    .build();

            given(userService.checkUserExist(userId))
                    .willThrow(new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> scheduleService.registSchedule(data))
                    .isInstanceOf(EntityNotFoundException.class);
            then(userService).should().checkUserExist(userId);
        }

        @Test
        @DisplayName("실패 - 시작일이 종료일보다 늦으면 예외가 발생한다")
        void registScheduleFailStartAfterEnd() {
            // given
            String userId = "test1";
            User user = User.createUser(userId);
            LocalDateTime start = LocalDateTime.now().plusDays(2);
            LocalDateTime end = start.minusDays(1);

            ScheduleServiceDto data = ScheduleServiceDto.builder()
                    .userId(userId)
                    .type(ScheduleType.EDUCATION)
                    .desc("교육")
                    .startDate(start)
                    .endDate(end)
                    .build();

            given(userService.checkUserExist(userId)).willReturn(user);

            // when & then
            assertThatThrownBy(() -> scheduleService.registSchedule(data))
                    .isInstanceOf(InvalidValueException.class);
        }
    }

    @Nested
    @DisplayName("사용자별 스케줄 조회")
    class SearchSchedulesByUser {
        @Test
        @DisplayName("성공 - 사용자의 스케줄 목록을 반환한다")
        void searchSchedulesByUserSuccess() {
            // given
            String userId = "test1";
            User user = User.createUser(userId);
            List<Schedule> schedules = List.of(
                    Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)),
                    Schedule.createSchedule(user, "출장", ScheduleType.BUSINESSTRIP,
                            LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11))
            );

            given(scheduleRepository.findSchedulesByUserId(userId)).willReturn(schedules);

            // when
            List<Schedule> result = scheduleService.searchSchedulesByUser(userId);

            // then
            then(scheduleRepository).should().findSchedulesByUserId(userId);
            assertThat(result).hasSize(2);
            assertThat(result).extracting("desc").containsExactly("교육", "출장");
        }
    }

    @Nested
    @DisplayName("기간별 스케줄 조회")
    class SearchSchedulesByPeriod {
        @Test
        @DisplayName("성공 - 기간 내 스케줄 목록을 반환한다")
        void searchSchedulesByPeriodSuccess() {
            // given
            LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);
            User user = User.createUser("test1");

            List<Schedule> schedules = List.of(
                    Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                            LocalDateTime.of(2025, 4, 20, 0, 0), LocalDateTime.of(2025, 4, 20, 23, 59)),
                    Schedule.createSchedule(user, "출장", ScheduleType.BUSINESSTRIP,
                            LocalDateTime.of(2025, 7, 20, 0, 0), LocalDateTime.of(2025, 7, 20, 23, 59))
            );

            given(scheduleRepository.findSchedulesByPeriod(start, end)).willReturn(schedules);

            // when
            List<Schedule> result = scheduleService.searchSchedulesByPeriod(start, end);

            // then
            then(scheduleRepository).should().findSchedulesByPeriod(start, end);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("실패 - 시작일이 종료일보다 늦으면 예외가 발생한다")
        void searchSchedulesByPeriodFailStartAfterEnd() {
            // given
            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = start.minusDays(2);

            // when & then
            assertThatThrownBy(() -> scheduleService.searchSchedulesByPeriod(start, end))
                    .isInstanceOf(InvalidValueException.class);
        }
    }

    @Nested
    @DisplayName("스케줄 수정")
    class UpdateSchedule {
        @Test
        @DisplayName("성공 - 기존 스케줄을 삭제하고 새로운 스케줄을 등록한다")
        void updateScheduleSuccess() {
            // given
            Long scheduleId = 1L;
            String userId = "test1";
            User user = User.createUser(userId);
            LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
            LocalDateTime futureEnd = futureStart.plusHours(8);

            Schedule existingSchedule = Schedule.createSchedule(user, "기존 교육", ScheduleType.EDUCATION,
                    futureStart, futureEnd);
            setScheduleId(existingSchedule, scheduleId);

            ScheduleServiceDto data = ScheduleServiceDto.builder()
                    .userId(userId)
                    .type(ScheduleType.BUSINESSTRIP)
                    .desc("출장")
                    .startDate(futureStart.plusDays(5))
                    .endDate(futureEnd.plusDays(5))
                    .build();

            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(existingSchedule));
            given(userService.checkUserExist(userId)).willReturn(user);
            willDoNothing().given(scheduleRepository).save(any(Schedule.class));

            // when
            Long newScheduleId = scheduleService.updateSchedule(scheduleId, data);

            // then
            then(scheduleRepository).should().findById(scheduleId);
            then(userService).should().checkUserExist(userId);
            then(scheduleRepository).should().save(any(Schedule.class));
            assertThat(existingSchedule.getIsDeleted()).isEqualTo(YNType.Y);
        }
    }

    @Nested
    @DisplayName("스케줄 삭제")
    class DeleteSchedule {
        @Test
        @DisplayName("성공 - 미래 스케줄이 삭제된다")
        void deleteScheduleSuccess() {
            // given
            Long scheduleId = 1L;
            User user = User.createUser("test1");
            LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
            LocalDateTime futureEnd = futureStart.plusHours(8);

            Schedule schedule = Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                    futureStart, futureEnd);
            setScheduleId(schedule, scheduleId);

            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when
            scheduleService.deleteSchedule(scheduleId);

            // then
            then(scheduleRepository).should().findById(scheduleId);
            assertThat(schedule.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스케줄을 삭제하려 하면 예외가 발생한다")
        void deleteScheduleFailNotFound() {
            // given
            Long scheduleId = 999L;
            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.deleteSchedule(scheduleId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 이미 삭제된 스케줄을 삭제하려 하면 예외가 발생한다")
        void deleteScheduleFailAlreadyDeleted() {
            // given
            Long scheduleId = 1L;
            User user = User.createUser("test1");
            LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
            LocalDateTime futureEnd = futureStart.plusHours(8);

            Schedule schedule = Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                    futureStart, futureEnd);
            schedule.deleteSchedule();

            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when & then
            assertThatThrownBy(() -> scheduleService.deleteSchedule(scheduleId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 과거 스케줄을 삭제하려 하면 예외가 발생한다")
        void deleteScheduleFailPastSchedule() {
            // given
            Long scheduleId = 1L;
            User user = User.createUser("test1");
            LocalDateTime pastStart = LocalDateTime.now().minusDays(3);
            LocalDateTime pastEnd = pastStart.plusHours(8);

            Schedule schedule = Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                    pastStart, pastEnd);
            setScheduleId(schedule, scheduleId);

            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when & then
            assertThatThrownBy(() -> scheduleService.deleteSchedule(scheduleId))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("스케줄 존재 확인")
    class CheckScheduleExist {
        @Test
        @DisplayName("성공 - 존재하는 스케줄을 반환한다")
        void checkScheduleExistSuccess() {
            // given
            Long scheduleId = 1L;
            User user = User.createUser("test1");
            Schedule schedule = Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
            setScheduleId(schedule, scheduleId);

            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when
            Schedule result = scheduleService.checkScheduleExist(scheduleId);

            // then
            assertThat(result).isEqualTo(schedule);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스케줄이면 예외가 발생한다")
        void checkScheduleExistFailNotFound() {
            // given
            Long scheduleId = 999L;
            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.checkScheduleExist(scheduleId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 스케줄이면 예외가 발생한다")
        void checkScheduleExistFailDeleted() {
            // given
            Long scheduleId = 1L;
            User user = User.createUser("test1");
            Schedule schedule = Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION,
                    LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
            schedule.deleteSchedule();

            given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));

            // when & then
            assertThatThrownBy(() -> scheduleService.checkScheduleExist(scheduleId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    // 테스트 헬퍼 메서드
    private void setScheduleId(Schedule schedule, Long id) {
        try {
            java.lang.reflect.Field field = Schedule.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(schedule, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
