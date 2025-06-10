package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.ScheduleRepositoryImpl;
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
        Long userNo = 1L;
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Schedule.createSchedule(user, desc, type, start, end, 0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        willDoNothing().given(scheduleRepositoryImpl).save(any(Schedule.class));

        // When
        scheduleService.registSchedule(userNo, type, desc, start, end, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(scheduleRepositoryImpl).should().save(any(Schedule.class));
    }

    @Test
    @DisplayName("스케줄 추가 테스트 - 실패 (사용자 없음)")
    void registScheduleFailTestNotFoundUser() {
        // Given
        Long userNo = 900L;
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        given(userService.checkUserExist(userNo)).willThrow(new IllegalArgumentException(""));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.registSchedule(userNo, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
    }

    @Test
    @DisplayName("스케줄 추가 테스트 - 실패 (start, end 반대)")
    void registScheduleFailTestReverseStartEndDate() {
        // Given
        Long userNo = 1L;
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(userService.checkUserExist(userNo)).willReturn(user);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.registSchedule(userNo, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
    }

    @Test
    @DisplayName("사용자별 스케줄 조회 테스트 - 성공")
    void findSchedulesByUserNoSuccessTest() {
        // Given
        Long userNo = 1L;
        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(scheduleRepositoryImpl.findSchedulesByUserNo(userNo)).willReturn(List.of(
                Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION, LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 23, 59, 59), 0L, "127.0.0.1"),
                Schedule.createSchedule(user, "예비군", ScheduleType.DEFENSE, LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 23, 59, 59), 0L, "127.0.0.1"),
                Schedule.createSchedule(user, "건강검진", ScheduleType.HEALTHCHECK, LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 23, 59, 59), 0L, "127.0.0.1")
        ));

        // When
        List<Schedule> schedules = scheduleService.findSchedulesByUserNo(userNo);

        // Then
        assertThat(schedules).hasSize(3);
        assertThat(schedules)
                .extracting("desc")
                .containsExactlyInAnyOrder("교육", "예비군", "건강검진");
        then(scheduleRepositoryImpl).should().findSchedulesByUserNo(userNo);
    }

    @Test
    @DisplayName("스케줄 기간 조회 테스트 - 성공")
    void findSchedulesByPeriodSuccessTest() {
        // Given
        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59);

        given(scheduleRepositoryImpl.findSchedulesByPeriod(start, end)).willReturn(List.of(
                Schedule.createSchedule(user, "교육", ScheduleType.EDUCATION, LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 4, 20, 23, 59, 59), 0L, "127.0.0.1"),
                Schedule.createSchedule(user, "예비군", ScheduleType.DEFENSE, LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 7, 20, 23, 59, 59), 0L, "127.0.0.1"),
                Schedule.createSchedule(user, "건강검진", ScheduleType.HEALTHCHECK, LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 20, 23, 59, 59), 0L, "127.0.0.1")
        ));

        // When
        List<Schedule> schedules = scheduleService.findSchedulesByPeriod(start, end);

        // Then
        assertThat(schedules).hasSize(3);
        assertThat(schedules)
                .extracting("desc")
                .containsExactlyInAnyOrder("교육", "예비군", "건강검진");
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

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Schedule schedule = Schedule.createSchedule(user, desc, type, start, end, 0L, "127.0.0.1");

        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(Optional.of(schedule));

        // When
        scheduleService.deleteSchedule(scheduleId, 0L, "127.0.0.1");

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
                scheduleService.deleteSchedule(scheduleId, 0L, "127.0.0.1"));
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

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Schedule schedule = Schedule.createSchedule(user, desc, type, start, end, 0L, "127.0.0.1");

        schedule.deleteSchedule(0L, "127.0.0.1");
        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(Optional.of(schedule));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.deleteSchedule(scheduleId, 0L, "127.0.0.1"));
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

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Schedule schedule = Schedule.createSchedule(user, desc, type, start, end, 0L, "127.0.0.1");

        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(Optional.of(schedule));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.deleteSchedule(scheduleId, 0L, "127.0.0.1"));
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }
}
