package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.ScheduleRepositoryImpl;
import com.lshdainty.myhr.service.dto.ScheduleServiceDto;
import com.lshdainty.myhr.type.ScheduleType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("스케줄 서비스 테스트")
class ScheduleServiceTest {
    // 삭제하지 말 것 (NullpointException 발생)
    @Mock
    private MessageSource ms;
    @Mock
    private ScheduleRepositoryImpl scheduleRepositoryImpl;
    @Mock
    private UserService userService;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("스케줄 추가 테스트 - 성공")
    void registScheduleSuccessTest() {
        // Given
        String userId = "test1";
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        User user = User.createUser(userId);

        given(userService.checkUserExist(userId)).willReturn(user);
        willDoNothing().given(scheduleRepositoryImpl).save(any(Schedule.class));

        // When
        scheduleService.registSchedule(ScheduleServiceDto.builder()
                .userId(userId)
                .type(type)
                .desc(desc)
                .startDate(start)
                .endDate(end)
                .build(),
                "",
                "127.0.0.1"
        );

        // Then
        then(userService).should().checkUserExist(userId);
        then(scheduleRepositoryImpl).should().save(any(Schedule.class));
    }

    @Test
    @DisplayName("스케줄 추가 테스트 - 실패 (사용자 없음)")
    void registScheduleFailTestNotFoundUser() {
        // Given
        String userId = "test2";

        given(userService.checkUserExist(userId)).willThrow(new IllegalArgumentException(""));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> scheduleService.registSchedule(
                ScheduleServiceDto.builder().userId(userId).build(), "", "127.0.0.1")
        );
        then(userService).should().checkUserExist(userId);
    }

    @Test
    @DisplayName("스케줄 추가 테스트 - 실패 (start, end 반대)")
    void registScheduleFailTestReverseStartEndDate() {
        // Given
        String userId = "test1";
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);

        User user = User.createUser(userId);

        given(userService.checkUserExist(userId)).willReturn(user);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> scheduleService.registSchedule(
                ScheduleServiceDto.builder()
                        .userId(userId)
                        .type(type)
                        .desc(desc)
                        .startDate(start)
                        .endDate(end)
                        .build(),
                "",
                "127.0.0.1")
        );
        then(userService).should().checkUserExist(userId);
    }

    @Test
    @DisplayName("사용자별 스케줄 조회 테스트 - 성공")
    void findSchedulesByUserNoSuccessTest() {
        // Given
        String userId = "test1";
        User user = User.createUser(userId);

        given(scheduleRepositoryImpl.findSchedulesByUserId(userId)).willReturn(List.of(
                Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION, LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 23, 59, 59), "", "127.0.0.1"),
                Schedule.createSchedule(user, "예비군", ScheduleType.DEFENSE, LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 23, 59, 59), "", "127.0.0.1"),
                Schedule.createSchedule(user, "건강검진(반차)", ScheduleType.HEALTHCHECKHALF, LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 9, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 14, 0, 0), "", "127.0.0.1")
        ));

        // When
        List<Schedule> schedules = scheduleService.findSchedulesByUserId(userId);

        // Then
        assertThat(schedules).hasSize(3);
        assertThat(schedules)
                .extracting("desc")
                .containsExactlyInAnyOrder("교육", "예비군", "건강검진(반차)");
        then(scheduleRepositoryImpl).should().findSchedulesByUserId(userId);
    }

    @Test
    @DisplayName("스케줄 기간 조회 테스트 - 성공")
    void findSchedulesByPeriodSuccessTest() {
        // Given
        User user = User.createUser("test1");

        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59);

        given(scheduleRepositoryImpl.findSchedulesByPeriod(start, end)).willReturn(List.of(
                Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION, LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 23, 59, 59), "", "127.0.0.1"),
                Schedule.createSchedule(user, "예비군", ScheduleType.DEFENSE, LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 23, 59, 59), "", "127.0.0.1"),
                Schedule.createSchedule(user, "건강검진(반차)", ScheduleType.HEALTHCHECKHALF, LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 9, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 14, 0, 0), "", "127.0.0.1")
        ));

        // When
        List<Schedule> schedules = scheduleService.findSchedulesByPeriod(start, end);

        // Then
        assertThat(schedules).hasSize(3);
        assertThat(schedules)
                .extracting("desc")
                .containsExactlyInAnyOrder("교육", "예비군", "건강검진(반차)");
        then(scheduleRepositoryImpl).should().findSchedulesByPeriod(start, end);
    }

    @Test
    @DisplayName("스케줄 기간 조회 테스트 - 실패 (start, end 반대)")
    void findSchedulesByPeriodFailTestReverseStartEndDate() {
        // Given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.findSchedulesByPeriod(start, end));
    }

    @Test
    @DisplayName("스케줄 삭제 테스트 - 성공")
    void deleteScheduleSuccessTest() {
        // Given
        Long scheduleId = 1L;
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        User user = User.createUser("test1");
        Schedule schedule = Schedule.createSchedule(user, desc, type, start, end, "", "127.0.0.1");

        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(Optional.of(schedule));

        // When
        scheduleService.deleteSchedule(scheduleId, "", "127.0.0.1");

        // Then
        assertThat(schedule.getDelYN()).isEqualTo("Y");
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }

    @Test
    @DisplayName("스케줄 삭제 테스트 - 실패 (스케줄 없음)")
    void deleteScheduleFailTestNotFoundSchedule() {
        // Given
        Long scheduleId = 900L;
        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.deleteSchedule(scheduleId, "", "127.0.0.1"));
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }

    @Test
    @DisplayName("스케줄 삭제 테스트 - 실패 (이미 삭제된 스케줄)")
    void deleteScheduleFailTestDeletedSchedule() {
        // Given
        Long scheduleId = 1L;
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        User user = User.createUser("test1");
        Schedule schedule = Schedule.createSchedule(user, desc, type, start, end, "", "127.0.0.1");

        schedule.deleteSchedule("", "127.0.0.1");
        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(Optional.of(schedule));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.deleteSchedule(scheduleId, "", "127.0.0.1"));
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }

    @Test
    @DisplayName("스케줄 삭제 테스트 - 실패 (과거 스케줄)")
    void deleteScheduleFailTestPassedSchedule() {
        // Given
        Long scheduleId = 1L;
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = start.plusDays(1);

        User user = User.createUser("test1");
        Schedule schedule = Schedule.createSchedule(user, desc, type, start, end, "", "127.0.0.1");

        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(Optional.of(schedule));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.deleteSchedule(scheduleId, "", "127.0.0.1"));
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }
}
