package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
import com.lshdainty.myhr.repository.ScheduleRepositoryImpl;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.repository.VacationRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.realm.AuthenticatedUserRealm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("스케줄 서비스 테스트")
class ScheduleServiceTest {
    @Mock
    private MessageSource ms;
    @Mock
    private ScheduleRepositoryImpl scheduleRepositoryImpl;
    @Mock
    private HolidayRepositoryImpl holidayRepositoryImpl;
    @Mock
    private UserService userService;
    @Mock
    private VacationService vacationService;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("스케줄(휴가) 추가 테스트 - 성공")
    void addScheduleWithVacationSuccessTest() {
        // Given
        Long userNo = 1L;
        Long vacationId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
        Schedule schedule = Schedule.createSchedule(user, vacation, desc, type, start, end, 0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationService.checkVacationExist(vacationId)).willReturn(vacation);
        given(scheduleRepositoryImpl.findCountByVacation(any(Vacation.class))).willReturn(Collections.emptyList());
        given(holidayRepositoryImpl.findHolidaysByStartEndDate(any(), any())).willReturn(Collections.emptyList());
        willDoNothing().given(scheduleRepositoryImpl).save(any(Schedule.class));

        // When
        Long scheduleId = scheduleService.addSchedule(userNo, vacationId, type, desc, start, end, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationService).should().checkVacationExist(vacationId);
        then(scheduleRepositoryImpl).should().findCountByVacation(any(Vacation.class));
        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(any(), any());
        then(scheduleRepositoryImpl).should().save(any(Schedule.class));

    }

    @Test
    @DisplayName("스케줄(휴가) 추가 테스트 - 실패 (사용자 없음)")
    void addScheduleWithVacationFailUserNotFoundTest() {
        // Given
        Long userNo = 900L;
        Long vacationId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 59, 59);

        given(userService.checkUserExist(userNo)).willThrow(new IllegalArgumentException(""));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(userNo, vacationId, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
    }

    @Test
    @DisplayName("스케줄(휴가) 추가 테스트 - 실패 (휴가 없음)")
    void addScheduleWithVacationFailVacationNotFoundTest() {
        // Given
        Long userNo = 1L;
        Long vacationId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationService.checkVacationExist(vacationId)).willThrow(new IllegalArgumentException(""));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(userNo, vacationId, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
        then(vacationService).should().checkVacationExist(vacationId);
    }

    @Test
    @DisplayName("스케줄(휴가) 추가 테스트 - 실패 (만료된 휴가)")
    void addScheduleWithVacationFailExpiredVacationTest() {
        // Given
        Long userNo = 1L;
        Long vacationId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear() - 1, 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear() - 1, 12, 31, 23, 59, 59), 0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationService.checkVacationExist(vacationId)).willReturn(vacation);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(userNo, vacationId, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
        then(vacationService).should().checkVacationExist(vacationId);
    }

    @Test
    @DisplayName("스케줄(휴가) 추가 테스트 - 실패 (휴가 부족)")
    void addScheduleWithVacationFailNotEnoughVacationTest() {
        // Given
        Long userNo = 1L;
        Long vacationId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 59, 59).plusDays(10);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationService.checkVacationExist(vacationId)).willReturn(vacation);
        given(scheduleRepositoryImpl.findCountByVacation(any(Vacation.class))).willReturn(Collections.emptyList());
        given(holidayRepositoryImpl.findHolidaysByStartEndDate(any(), any())).willReturn(Collections.emptyList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(userNo, vacationId, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
        then(vacationService).should().checkVacationExist(vacationId);
        then(scheduleRepositoryImpl).should().findCountByVacation(any(Vacation.class));
        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(any(), any());
    }

    @Test
    @DisplayName("스케줄(휴가) 추가 테스트 - 실패 (start, end 반대)")
    void addScheduleWithVacationFailStartEndTest() {
        // Given
        Long userNo = 1L;
        Long vacationId = 1L;
        ScheduleType type = ScheduleType.TWOTIMEOFF;
        String desc = "2시간 휴가";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 8, 0, 0);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationService.checkVacationExist(vacationId)).willReturn(vacation);
        given(scheduleRepositoryImpl.findCountByVacation(any(Vacation.class))).willReturn(Collections.emptyList());
        given(holidayRepositoryImpl.findHolidaysByStartEndDate(any(), any())).willReturn(Collections.emptyList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(userNo, vacationId, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
        then(vacationService).should().checkVacationExist(vacationId);
        then(scheduleRepositoryImpl).should().findCountByVacation(any(Vacation.class));
        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(any(), any());
    }

    @Test
    @DisplayName("스케줄(휴가) 추가 테스트 - 실패 (workTime 미충족)")
    void addScheduleWithVacationFailUserWorkTimeTest() {
        // Given
        Long userNo = 1L;
        Long vacationId = 1L;
        ScheduleType type = ScheduleType.TWOTIMEOFF;
        String desc = "2시간 휴가";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 8, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 10, 0, 0);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationService.checkVacationExist(vacationId)).willReturn(vacation);
        given(scheduleRepositoryImpl.findCountByVacation(any(Vacation.class))).willReturn(Collections.emptyList());
        given(holidayRepositoryImpl.findHolidaysByStartEndDate(any(), any())).willReturn(Collections.emptyList());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.addSchedule(userNo, vacationId, type, desc, start, end, 0L, "127.0.0.1"));
        then(userService).should().checkUserExist(userNo);
        then(vacationService).should().checkVacationExist(vacationId);
        then(scheduleRepositoryImpl).should().findCountByVacation(any(Vacation.class));
        then(holidayRepositoryImpl).should().findHolidaysByStartEndDate(any(), any());
    }

    @Test
    @DisplayName("스케줄(비휴가) 추가 테스트 - 성공")
    void addScheduleWithoutVacationSuccessTest() {
        // Given
        Long userNo = 1L;
        ScheduleType type = ScheduleType.EDUCATION;
        String desc = "교육";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Schedule mockSchedule = Schedule.createSchedule(user,null,  desc, type, start, end, 0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        willDoNothing().given(scheduleRepositoryImpl).save(any(Schedule.class));

        // When
        Long scheduleId = scheduleService.addSchedule(userNo, type, desc, start, end, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(scheduleRepositoryImpl).should().save(any(Schedule.class));
    }

    @Test
    @DisplayName("사용자별 스케줄 조회 테스트 - 성공")
    void findSchedulesByUserNoTest() {
        // Given
        Long userNo = 1L;
        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");

        given(scheduleRepositoryImpl.findSchedulesByUserNo(userNo)).willReturn(List.of(
                Schedule.createSchedule(user, vacation, "휴가", ScheduleType.DAYOFF, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 0L, "127.0.0.1"),
                Schedule.createSchedule(user, null, "교육", ScheduleType.EDUCATION, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3), 0L, "127.0.0.1")
        ));

        // When
        List<Schedule> schedules = scheduleService.findSchedulesByUserNo(userNo);

        // Then
        assertThat(schedules).hasSize(2);
        assertThat(schedules)
                .extracting("desc")
                .containsExactlyInAnyOrder("휴가", "교육");
        then(scheduleRepositoryImpl).should().findSchedulesByUserNo(userNo);
    }

    @Test
    @DisplayName("스케줄 삭제 테스트 - 성공")
    void deleteScheduleSuccessTest() {
        // Given
        Long scheduleId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
        Schedule schedule = Schedule.createSchedule(user, vacation, desc, type, start, end, 0L, "127.0.0.1");

        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(schedule);

        // When
        scheduleService.deleteSchedule(scheduleId, 0L, "127.0.0.1");

        // Then
        assertThat(schedule.getDelYN()).isEqualTo("Y");
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }

    @Test
    @DisplayName("스케줄 삭제 테스트 - 실패 (스케줄 없음)")
    void deleteScheduleFailScheduleNotFoundTest() {
        // Given
        Long scheduleId = 900L;
        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.deleteSchedule(scheduleId, 0L, "127.0.0.1"));
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }

    @Test
    @DisplayName("스케줄 삭제 테스트 - 실패 (과거 스케줄)")
    void deleteScheduleFailPastScheduleTest() {
        // Given
        Long scheduleId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0, 0, 0).minusDays(3);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23, 59, 59).minusDays(2);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
        Schedule schedule = Schedule.createSchedule(user, vacation, desc, type, start, end, 0L, "127.0.0.1");

        given(scheduleRepositoryImpl.findById(scheduleId)).willReturn(schedule);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                scheduleService.deleteSchedule(scheduleId, 0L, "127.0.0.1"));
        then(scheduleRepositoryImpl).should().findById(scheduleId);
    }

    @Test
    @DisplayName("실제 사용 휴가 시간 계산 테스트")
    void calculateRealUsedTest() {
        // Given
        Long scheduleId = 1L;
        ScheduleType type = ScheduleType.DAYOFF;
        String desc = "연차";
        LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 5, 5, 0, 0, 0);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("32"), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
        Schedule schedule = Schedule.createSchedule(user, vacation, desc, type, start, end, 0L, "127.0.0.1");

        List<LocalDate> holidays = List.of(LocalDate.of(2025, 5, 5));

        // When
        BigDecimal realUsed = scheduleService.calculateRealUsed(schedule, holidays);

        // Then
        assertThat(realUsed).isEqualTo(new BigDecimal("16"));
    }
}
