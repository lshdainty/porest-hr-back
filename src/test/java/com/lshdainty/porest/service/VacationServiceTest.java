package com.lshdainty.porest.service;

import com.lshdainty.porest.domain.*;
import com.lshdainty.porest.repository.HolidayRepositoryImpl;
import com.lshdainty.porest.repository.UserRepositoryImpl;
import com.lshdainty.porest.repository.VacationHistoryRepositoryImpl;
import com.lshdainty.porest.repository.VacationRepositoryImpl;
import com.lshdainty.porest.service.dto.VacationServiceDto;
import com.lshdainty.porest.type.*;
import com.lshdainty.porest.util.PorestTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mockStatic;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("휴가 서비스 테스트")
class VacationServiceTest {

    @Mock
    private MessageSource ms;

    @Mock
    private VacationRepositoryImpl vacationRepositoryImpl;

    @Mock
    private VacationHistoryRepositoryImpl vacationHistoryRepositoryImpl;

    @Mock
    private UserRepositoryImpl userRepositoryImpl;

    @Mock
    private HolidayRepositoryImpl holidayRepositoryImpl;

    @Mock
    private UserService userService;

    @InjectMocks
    private VacationService vacationService;

    @Test
    @DisplayName("휴가 등록 테스트 - ANNUAL 타입")
    void registVacationAnnualTest() {
        // Given
        VacationServiceDto dto = VacationServiceDto.builder()
                .type(VacationType.ANNUAL)
                .userId("user1")
                .grantTime(new BigDecimal("15.0000"))
                .desc("연차 휴가")
                .occurDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiryDate(LocalDateTime.of(2025, 12, 31, 23, 59))
                .build();

        User user = createTestUser("user1", "9 ~ 6");
        Vacation existingVacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("10.0000"));
        setVacationId(existingVacation, 1L);

        given(userService.checkUserExist("user1")).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(eq("user1"), eq(VacationType.ANNUAL), eq("2025")))
                .willReturn(Optional.of(existingVacation));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        Long result = vacationService.registVacation(dto, "testUser", "127.0.0.1");

        // Then
        assertThat(result).isEqualTo(1L);
        then(userService).should().checkUserExist("user1");
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("휴가 등록 테스트 - ANNUAL 타입 (신규)")
    void registVacationAnnualNewTest() {
        // Given
        VacationServiceDto dto = VacationServiceDto.builder()
                .type(VacationType.ANNUAL)
                .userId("user1")
                .grantTime(new BigDecimal("15.0000"))
                .desc("연차 휴가")
                .occurDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiryDate(LocalDateTime.of(2025, 12, 31, 23, 59))
                .build();

        User user = createTestUser("user1", "9 ~ 6");

        given(userService.checkUserExist("user1")).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(eq("user1"), eq(VacationType.ANNUAL), eq("2025")))
                .willReturn(Optional.empty());

        // Answer를 사용하여 저장된 Vacation의 ID를 설정
        willAnswer(invocation -> {
            Vacation vacation = invocation.getArgument(0);
            setVacationId(vacation, 1L);
            return null;
        }).given(vacationRepositoryImpl).save(any(Vacation.class));

        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        Long result = vacationService.registVacation(dto, "testUser", "127.0.0.1");

        // Then
        assertThat(result).isEqualTo(1L);
        then(userService).should().checkUserExist("user1");
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("휴가 등록 테스트 - MATERNITY 타입")
    void registVacationMaternityTest() {
        // Given
        VacationServiceDto dto = VacationServiceDto.builder()
                .type(VacationType.MATERNITY)
                .userId("user1")
                .grantTime(new BigDecimal("90.0000"))
                .desc("출산 휴가")
                .occurDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiryDate(LocalDateTime.of(2025, 6, 30, 23, 59))
                .build();

        User user = createTestUser("user1", "9 ~ 6");

        given(userService.checkUserExist("user1")).willReturn(user);

        // Answer를 사용하여 저장된 Vacation의 ID를 설정
        willAnswer(invocation -> {
            Vacation vacation = invocation.getArgument(0);
            setVacationId(vacation, 2L);
            return null;
        }).given(vacationRepositoryImpl).save(any(Vacation.class));

        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        Long result = vacationService.registVacation(dto, "testUser", "127.0.0.1");

        // Then
        assertThat(result).isEqualTo(2L);
        then(userService).should().checkUserExist("user1");
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("휴가 등록 테스트 - WEDDING 타입")
    void registVacationWeddingTest() {
        // Given
        VacationServiceDto dto = VacationServiceDto.builder()
                .type(VacationType.WEDDING)
                .userId("user1")
                .grantTime(new BigDecimal("5.0000"))
                .desc("결혼 휴가")
                .occurDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiryDate(LocalDateTime.of(2025, 3, 31, 23, 59))
                .build();

        User user = createTestUser("user1", "9 ~ 6");

        given(userService.checkUserExist("user1")).willReturn(user);

        // Answer를 사용하여 저장된 Vacation의 ID를 설정
        willAnswer(invocation -> {
            Vacation vacation = invocation.getArgument(0);
            setVacationId(vacation, 3L);
            return null;
        }).given(vacationRepositoryImpl).save(any(Vacation.class));

        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        Long result = vacationService.registVacation(dto, "testUser", "127.0.0.1");

        // Then
        assertThat(result).isEqualTo(3L);
        then(userService).should().checkUserExist("user1");
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("휴가 등록 테스트 - BEREAVEMENT 타입")
    void registVacationBereavementTest() {
        // Given
        VacationServiceDto dto = VacationServiceDto.builder()
                .type(VacationType.BEREAVEMENT)
                .userId("user1")
                .grantTime(new BigDecimal("3.0000"))
                .desc("상조 휴가")
                .occurDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiryDate(LocalDateTime.of(2025, 2, 28, 23, 59))
                .build();

        User user = createTestUser("user1", "9 ~ 6");

        given(userService.checkUserExist("user1")).willReturn(user);

        // Answer를 사용하여 저장된 Vacation의 ID를 설정
        willAnswer(invocation -> {
            Vacation vacation = invocation.getArgument(0);
            setVacationId(vacation, 4L);
            return null;
        }).given(vacationRepositoryImpl).save(any(Vacation.class));

        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        Long result = vacationService.registVacation(dto, "testUser", "127.0.0.1");

        // Then
        assertThat(result).isEqualTo(4L);
        then(userService).should().checkUserExist("user1");
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("휴가 등록 테스트 - OVERTIME 타입")
    void registVacationOvertimeTest() {
        // Given
        VacationServiceDto dto = VacationServiceDto.builder()
                .type(VacationType.OVERTIME)
                .userId("user1")
                .grantTime(new BigDecimal("0.1250"))
                .desc("연장근무 휴가")
                .occurDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiryDate(LocalDateTime.of(2025, 12, 31, 23, 59))
                .build();

        User user = createTestUser("user1", "9 ~ 6");
        Vacation existingVacation = createTestVacation(user, VacationType.OVERTIME, new BigDecimal("0.5000"));
        setVacationId(existingVacation, 5L);

        given(userService.checkUserExist("user1")).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(eq("user1"), eq(VacationType.OVERTIME), eq("2025")))
                .willReturn(Optional.of(existingVacation));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        Long result = vacationService.registVacation(dto, "testUser", "127.0.0.1");

        // Then
        assertThat(result).isEqualTo(5L);
        then(userService).should().checkUserExist("user1");
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("휴가 등록 테스트 - OVERTIME 타입 (신규)")
    void registVacationOvertimeNewTest() {
        // Given
        VacationServiceDto dto = VacationServiceDto.builder()
                .type(VacationType.OVERTIME)
                .userId("user1")
                .grantTime(new BigDecimal("0.1250"))
                .desc("연장근무 휴가")
                .occurDate(LocalDateTime.of(2025, 1, 1, 0, 0))
                .expiryDate(LocalDateTime.of(2025, 12, 31, 23, 59))
                .build();

        User user = createTestUser("user1", "9 ~ 6");

        given(userService.checkUserExist("user1")).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(eq("user1"), eq(VacationType.OVERTIME), eq("2025")))
                .willReturn(Optional.empty());

        // Answer를 사용하여 저장된 Vacation의 ID를 설정
        willAnswer(invocation -> {
            Vacation vacation = invocation.getArgument(0);
            setVacationId(vacation, 6L);
            return null;
        }).given(vacationRepositoryImpl).save(any(Vacation.class));

        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        Long result = vacationService.registVacation(dto, "testUser", "127.0.0.1");

        // Then
        assertThat(result).isEqualTo(6L);
        then(userService).should().checkUserExist("user1");
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("휴가 사용 테스트 - 성공")
    void useVacationSuccessTest() {
        // Given
        String userId = "user1";
        Long vacationId = 1L;

        User user = createTestUser(userId, "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("5.0000"));
        setVacationId(vacation, vacationId);

        VacationServiceDto dto = VacationServiceDto.builder()
                .id(vacationId)
                .userId(userId)
                .timeType(VacationTimeType.DAYOFF)
                .startDate(LocalDateTime.of(2025, 1, 6, 9, 0)) // 월요일
                .endDate(LocalDateTime.of(2025, 1, 6, 18, 0))   // 같은 날
                .desc("개인사유")
                .build();

        given(userService.checkUserExist(userId)).willReturn(user);
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));
        given(holidayRepositoryImpl.findHolidaysByStartEndDateWithType(anyString(), anyString(), eq(HolidayType.PUBLIC)))
                .willReturn(Collections.emptyList());
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            // PorestTime 메서드들 모킹
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(false);
            mockedTime.when(() -> PorestTime.getBetweenDatesByDayOfWeek(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList()); // 주말 없음
            mockedTime.when(() -> PorestTime.addAllDates(any(), any())).thenReturn(Collections.emptyList());
            mockedTime.when(() -> PorestTime.getBetweenDates(any(), any(), any()))
                    .thenReturn(List.of(LocalDate.of(2025, 1, 6))); // 1일
            mockedTime.when(() -> PorestTime.removeAllDates(any(), any()))
                    .thenReturn(List.of(LocalDate.of(2025, 1, 6))); // 1일

            // When
            Long result = vacationService.useVacation(dto, "testUser", "127.0.0.1");

            // Then
            assertThat(result).isEqualTo(vacationId);
            then(userService).should().checkUserExist(userId);
            then(vacationRepositoryImpl).should().findById(vacationId);
            then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
        }
    }

    @Test
    @DisplayName("휴가 사용 테스트 - 실패 (시작일이 종료일보다 늦음)")
    void useVacationFailStartAfterEndTest() {
        // Given
        String userId = "user1";
        Long vacationId = 1L;

        User user = createTestUser(userId, "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("5.0000"));
        setVacationId(vacation, vacationId);

        VacationServiceDto dto = VacationServiceDto.builder()
                .id(vacationId)
                .userId(userId)
                .startDate(LocalDateTime.of(2025, 1, 10, 9, 0))
                .endDate(LocalDateTime.of(2025, 1, 8, 18, 0))
                .build();

        given(userService.checkUserExist(userId)).willReturn(user);
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));
        given(ms.getMessage("error.validate.startIsAfterThanEnd", null, null))
                .willReturn("Start date cannot be after end date");

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> vacationService.useVacation(dto, "testUser", "127.0.0.1"));

            assertThat(exception.getMessage()).isEqualTo("Start date cannot be after end date");
        }
    }

    @Test
    @DisplayName("휴가 사용 테스트 - 실패 (유연근무제 시간 위반)")
    void useVacationFailWorkTimeViolationTest() {
        // Given
        String userId = "user1";
        Long vacationId = 1L;

        User user = createTestUser(userId, "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("5.0000"));
        setVacationId(vacation, vacationId);

        VacationServiceDto dto = VacationServiceDto.builder()
                .id(vacationId)
                .userId(userId)
                .timeType(VacationTimeType.ONETIMEOFF) // 시간단위 휴가
                .startDate(LocalDateTime.of(2025, 1, 6, 7, 0)) // 근무시간 전
                .endDate(LocalDateTime.of(2025, 1, 6, 8, 0))
                .build();

        given(userService.checkUserExist(userId)).willReturn(user);
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));
        given(ms.getMessage("error.validate.worktime.startEndTime", null, null))
                .willReturn("Time is outside work hours");

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(false);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> vacationService.useVacation(dto, "testUser", "127.0.0.1"));

            assertThat(exception.getMessage()).isEqualTo("Time is outside work hours");
        }
    }

    @Test
    @DisplayName("휴가 사용 테스트 - 실패 (잔여 휴가 부족)")
    void useVacationFailNotEnoughRemainTimeTest() {
        // Given
        String userId = "user1";
        Long vacationId = 1L;

        User user = createTestUser(userId, "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("0.5000")); // 잔여 0.5일
        setVacationId(vacation, vacationId);

        VacationServiceDto dto = VacationServiceDto.builder()
                .id(vacationId)
                .userId(userId)
                .timeType(VacationTimeType.DAYOFF)
                .startDate(LocalDateTime.of(2025, 1, 6, 9, 0))
                .endDate(LocalDateTime.of(2025, 1, 6, 18, 0)) // 1일 신청
                .build();

        given(userService.checkUserExist(userId)).willReturn(user);
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));
        given(holidayRepositoryImpl.findHolidaysByStartEndDateWithType(anyString(), anyString(), eq(HolidayType.PUBLIC)))
                .willReturn(Collections.emptyList());
        given(ms.getMessage("error.validate.notEnoughRemainTime", null, null))
                .willReturn("Not enough vacation time");

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(false);
            mockedTime.when(() -> PorestTime.getBetweenDatesByDayOfWeek(any(), any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            mockedTime.when(() -> PorestTime.addAllDates(any(), any())).thenReturn(Collections.emptyList());
            mockedTime.when(() -> PorestTime.getBetweenDates(any(), any(), any()))
                    .thenReturn(List.of(LocalDate.of(2025, 1, 6)));
            mockedTime.when(() -> PorestTime.removeAllDates(any(), any()))
                    .thenReturn(List.of(LocalDate.of(2025, 1, 6)));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> vacationService.useVacation(dto, "testUser", "127.0.0.1"));

            assertThat(exception.getMessage()).isEqualTo("Not enough vacation time");
        }
    }

    @Test
    @DisplayName("사용자 휴가 목록 조회 테스트 - 성공")
    void getUserVacationsSuccessTest() {
        // Given
        String userId = "user1";
        User user = createTestUser(userId, "9 ~ 6");
        List<Vacation> vacations = List.of(
                createTestVacation(user, VacationType.ANNUAL, new BigDecimal("10.0000")),
                createTestVacation(user, VacationType.WEDDING, new BigDecimal("5.0000"))
        );

        given(vacationRepositoryImpl.findVacationsByUserId(userId)).willReturn(vacations);

        // When
        List<Vacation> result = vacationService.getUserVacations(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("type").containsExactly(VacationType.ANNUAL, VacationType.WEDDING);
        then(vacationRepositoryImpl).should().findVacationsByUserId(userId);
    }

    @Test
    @DisplayName("그룹 휴가 조회 테스트 - 성공")
    void getUserGroupVacationsSuccessTest() {
        // Given
        List<User> users = List.of(
                createTestUser("user1", "9 ~ 6"),
                createTestUser("user2", "10 ~ 7")
        );

        given(userRepositoryImpl.findUsersWithVacations()).willReturn(users);

        // When
        List<User> result = vacationService.getUserGroupVacations();

        // Then
        assertThat(result).hasSize(2);
        then(userRepositoryImpl).should().findUsersWithVacations();
    }

    @Test
    @DisplayName("사용 가능한 휴가 조회 테스트 - 성공")
    void getAvailableVacationsSuccessTest() {
        // Given
        String userId = "user1";
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        User user = createTestUser(userId, "9 ~ 6");
        List<Vacation> vacations = List.of(
                createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"))
        );

        given(userService.checkUserExist(userId)).willReturn(user);
        given(vacationRepositoryImpl.findVacationsByBaseTime(userId, startDate)).willReturn(vacations);

        // When
        List<Vacation> result = vacationService.getAvailableVacations(userId, startDate);

        // Then
        assertThat(result).hasSize(1);
        then(userService).should().checkUserExist(userId);
        then(vacationRepositoryImpl).should().findVacationsByBaseTime(userId, startDate);
    }

    @Test
    @DisplayName("휴가 히스토리 삭제 테스트 - 성공 (등록 내역)")
    void deleteVacationHistorySuccessRegistTest() {
        // Given
        Long historyId = 1L;
        Long vacationId = 1L;

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, vacationId);

        VacationHistory history = VacationHistory.createRegistVacationHistory(
                vacation, "등록 내역", new BigDecimal("5.0000"), "testUser", "127.0.0.1");

        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.of(history));
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(false);

            // When
            vacationService.deleteVacationHistory(historyId, "testUser", "127.0.0.1");

            // Then
            then(vacationHistoryRepositoryImpl).should().findById(historyId);
            then(vacationRepositoryImpl).should().findById(vacationId);
        }
    }

    @Test
    @DisplayName("휴가 히스토리 삭제 테스트 - 성공 (사용 내역)")
    void deleteVacationHistorySuccessUseTest() {
        // Given
        Long historyId = 1L;
        Long vacationId = 1L;

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, vacationId);

        VacationHistory history = VacationHistory.createUseVacationHistory(
                vacation, "사용 내역", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 2, 1, 9, 0), "testUser", "127.0.0.1");

        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.of(history));
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(false);

            // When
            vacationService.deleteVacationHistory(historyId, "testUser", "127.0.0.1");

            // Then
            then(vacationHistoryRepositoryImpl).should().findById(historyId);
            then(vacationRepositoryImpl).should().findById(vacationId);
        }
    }

    @Test
    @DisplayName("휴가 히스토리 삭제 테스트 - 실패 (만료일 지남)")
    void deleteVacationHistoryFailExpiredTest() {
        // Given
        Long historyId = 1L;
        Long vacationId = 1L;

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, vacationId);

        VacationHistory history = VacationHistory.createRegistVacationHistory(
                vacation, "등록 내역", new BigDecimal("5.0000"), "testUser", "127.0.0.1");

        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.of(history));
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));
        given(ms.getMessage("error.validate.expiry.isBeforeThanNow", null, null))
                .willReturn("Cannot delete expired vacation");

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(true); // 만료됨

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> vacationService.deleteVacationHistory(historyId, "testUser", "127.0.0.1"));

            assertThat(exception.getMessage()).isEqualTo("Cannot delete expired vacation");
        }
    }

    @Test
    @DisplayName("휴가 히스토리 삭제 테스트 - 실패 (잔여 휴가 부족)")
    void deleteVacationHistoryFailNotEnoughRemainTimeTest() {
        // Given
        Long historyId = 1L;
        Long vacationId = 1L;

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("2.0000")); // 잔여 2일
        setVacationId(vacation, vacationId);

        VacationHistory history = VacationHistory.createRegistVacationHistory(
                vacation, "등록 내역", new BigDecimal("5.0000"), "testUser", "127.0.0.1"); // 5일 등록 내역

        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.of(history));
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));
        given(ms.getMessage("error.validate.notEnoughRemainTime", null, null))
                .willReturn("Not enough remain time");

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(false);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> vacationService.deleteVacationHistory(historyId, "testUser", "127.0.0.1"));

            assertThat(exception.getMessage()).isEqualTo("Not enough remain time");
        }
    }

    @Test
    @DisplayName("휴가 히스토리 삭제 테스트 - 실패 (사용일 지남)")
    void deleteVacationHistoryFailUsedDatePassedTest() {
        // Given
        Long historyId = 1L;
        Long vacationId = 1L;

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, vacationId);

        VacationHistory history = VacationHistory.createUseVacationHistory(
                vacation, "사용 내역", VacationTimeType.DAYOFF,
                LocalDateTime.of(2024, 12, 1, 9, 0), "testUser", "127.0.0.1"); // 과거 날짜

        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.of(history));
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));
        given(ms.getMessage("error.validate.delete.isBeforeThanNow", null, null))
                .willReturn("Cannot delete past vacation");

        try (MockedStatic<PorestTime> mockedTime = mockStatic(PorestTime.class)) {
            mockedTime.when(() -> PorestTime.isAfterThanEndDate(any(), any())).thenReturn(false).thenReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> vacationService.deleteVacationHistory(historyId, "testUser", "127.0.0.1"));

            assertThat(exception.getMessage()).isEqualTo("Cannot delete past vacation");
        }
    }

    @Test
    @DisplayName("휴가 존재 확인 테스트 - 성공")
    void checkVacationExistSuccessTest() {
        // Given
        Long vacationId = 1L;
        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, vacationId);

        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));

        // When
        Vacation result = vacationService.checkVacationExist(vacationId);

        // Then
        assertThat(result).isEqualTo(vacation);
        then(vacationRepositoryImpl).should().findById(vacationId);
    }

    @Test
    @DisplayName("휴가 존재 확인 테스트 - 실패 (휴가 없음)")
    void checkVacationExistFailTest() {
        // Given
        Long vacationId = 999L;
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.vacation", null, null)).willReturn("Vacation not found");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vacationService.checkVacationExist(vacationId));

        assertThat(exception.getMessage()).isEqualTo("Vacation not found");
        then(vacationRepositoryImpl).should().findById(vacationId);
    }

    @Test
    @DisplayName("휴가 히스토리 존재 확인 테스트 - 성공")
    void checkVacationHistoryExistSuccessTest() {
        // Given
        Long historyId = 1L;
        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));

        VacationHistory history = VacationHistory.createRegistVacationHistory(
                vacation, "등록 내역", new BigDecimal("5.0000"), "testUser", "127.0.0.1");

        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.of(history));

        // When
        VacationHistory result = vacationService.checkVacationHistoryExist(historyId);

        // Then
        assertThat(result).isEqualTo(history);
        then(vacationHistoryRepositoryImpl).should().findById(historyId);
    }

    @Test
    @DisplayName("휴가 히스토리 존재 확인 테스트 - 실패 (히스토리 없음)")
    void checkVacationHistoryExistFailTest() {
        // Given
        Long historyId = 999L;
        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.vacation.history", null, null))
                .willReturn("Vacation history not found");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vacationService.checkVacationHistoryExist(historyId));

        assertThat(exception.getMessage()).isEqualTo("Vacation history not found");
        then(vacationHistoryRepositoryImpl).should().findById(historyId);
    }

    @Test
    @DisplayName("사용자별 기간 휴가 사용 이력 조회 테스트 - 성공")
    void getUserPeriodVacationUseHistoriesSuccessTest() {
        // Given
        String userId = "user1";
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

        User user = createTestUser(userId, "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));

        List<VacationHistory> histories = List.of(
                VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                        LocalDateTime.of(2025, 1, 6, 9, 0), "testUser", "127.0.0.1")
        );

        given(vacationHistoryRepositoryImpl.findVacationUseHistorysByUserAndPeriod(userId, startDate, endDate))
                .willReturn(histories);

        // When
        List<VacationServiceDto> result = vacationService.getUserPeriodVacationUseHistories(userId, startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimeType()).isEqualTo(VacationTimeType.DAYOFF);
        then(vacationHistoryRepositoryImpl).should().findVacationUseHistorysByUserAndPeriod(userId, startDate, endDate);
    }

    @Test
    @DisplayName("사용자 월별 휴가 통계 조회 테스트 - 성공")
    void getUserMonthStatsVacationUseHistoriesSuccessTest() {
        // Given
        String userId = "user1";
        String year = "2025";

        User user = createTestUser(userId, "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));

        List<VacationHistory> histories = List.of(
                VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                        LocalDateTime.of(2025, 1, 6, 9, 0), "testUser", "127.0.0.1"),
                VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                        LocalDateTime.of(2025, 2, 7, 9, 0), "testUser", "127.0.0.1")
        );

        given(vacationHistoryRepositoryImpl.findVacationUseHistorysByUserAndPeriod(
                eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(histories);

        // When
        List<VacationServiceDto> result = vacationService.getUserMonthStatsVacationUseHistories(userId, year);

        // Then
        assertThat(result).hasSize(12); // 12개월
        assertThat(result.get(0).getMonth()).isEqualTo(1);
        assertThat(result.get(0).getUsedTime()).isEqualByComparingTo(new BigDecimal("1.0000"));
        assertThat(result.get(1).getMonth()).isEqualTo(2);
        assertThat(result.get(1).getUsedTime()).isEqualByComparingTo(new BigDecimal("1.0000"));
    }

    @Test
    @DisplayName("사용자 휴가 사용 통계 조회 테스트 - 성공")
    void getUserVacationUseStatsSuccessTest() {
        // Given
        String userId = "user1";
        LocalDateTime baseTime = LocalDateTime.of(2025, 6, 15, 0, 0);

        User user = createTestUser(userId, "9 ~ 6");
        List<Vacation> vacations = List.of(
                createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"))
        );

        given(vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(userId, baseTime))
                .willReturn(vacations);
        given(vacationRepositoryImpl.findVacationsByBaseTimeWithHistory(userId, baseTime.minusMonths(1)))
                .willReturn(vacations);

        // When
        VacationServiceDto result = vacationService.getUserVacationUseStats(userId, baseTime);

        // Then
        assertThat(result).isNotNull();
        then(vacationRepositoryImpl).should().findVacationsByBaseTimeWithHistory(userId, baseTime);
        then(vacationRepositoryImpl).should().findVacationsByBaseTimeWithHistory(userId, baseTime.minusMonths(1));
    }

    @Test
    @DisplayName("기간별 휴가 사용 이력 조회 테스트 - 성공")
    void getPeriodVacationUseHistoriesSuccessTest() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, 1L);

        // VacationHistory 생성 후 ID 설정
        VacationHistory history1 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 6, 9, 0), "testUser", "127.0.0.1");
        VacationHistory history2 = VacationHistory.createUseVacationHistory(vacation, "오전반차", VacationTimeType.MORNINGOFF,
                LocalDateTime.of(2025, 1, 7, 9, 0), "testUser", "127.0.0.1");

        // 리플렉션으로 ID 설정
        setVacationHistoryId(history1, 1L);
        setVacationHistoryId(history2, 2L);

        List<VacationHistory> histories = List.of(history1, history2);
        List<Vacation> vacations = List.of(vacation);

        given(vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(startDate, endDate))
                .willReturn(histories);
        given(vacationRepositoryImpl.findVacationsByIdsWithUser(anyList()))
                .willReturn(vacations);

        // When
        List<VacationServiceDto> result = vacationService.getPeriodVacationUseHistories(startDate, endDate);

        // Then
        assertThat(result).isNotEmpty();
        then(vacationHistoryRepositoryImpl).should().findVacationHistorysByPeriod(startDate, endDate);
        then(vacationRepositoryImpl).should().findVacationsByIdsWithUser(anyList());
    }

    @Test
    @DisplayName("기간별 휴가 사용 이력 조회 테스트 - 연속된 연차 (makeDayGroupDto 연속 분기 테스트)")
    void getPeriodVacationUseHistoriesConsecutiveDaysTest() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, 1L);

        // 연속된 날짜의 연차 히스토리 생성 (1월 6일, 7일, 8일)
        VacationHistory history1 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 6, 9, 0), "testUser", "127.0.0.1");
        VacationHistory history2 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 7, 9, 0), "testUser", "127.0.0.1");
        VacationHistory history3 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 8, 9, 0), "testUser", "127.0.0.1");

        setVacationHistoryId(history1, 1L);
        setVacationHistoryId(history2, 2L);
        setVacationHistoryId(history3, 3L);

        List<VacationHistory> histories = List.of(history1, history2, history3);
        List<Vacation> vacations = List.of(vacation);

        given(vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(startDate, endDate))
                .willReturn(histories);
        given(vacationRepositoryImpl.findVacationsByIdsWithUser(anyList()))
                .willReturn(vacations);

        // When
        List<VacationServiceDto> result = vacationService.getPeriodVacationUseHistories(startDate, endDate);

        // Then
        assertThat(result).hasSize(1); // 연속된 3일이 하나의 그룹으로 묶임
        assertThat(result.get(0).getHistoryIds()).hasSize(3);
        assertThat(result.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 1, 6, 9, 0));
        assertThat(result.get(0).getEndDate()).isEqualTo(LocalDateTime.of(2025, 1, 8, 9, 0).plusSeconds(VacationTimeType.DAYOFF.getSeconds()));
        then(vacationHistoryRepositoryImpl).should().findVacationHistorysByPeriod(startDate, endDate);
        then(vacationRepositoryImpl).should().findVacationsByIdsWithUser(anyList());
    }

    @Test
    @DisplayName("기간별 휴가 사용 이력 조회 테스트 - 연속되지 않은 연차 (makeDayGroupDto 비연속 분기 테스트)")
    void getPeriodVacationUseHistoriesNonConsecutiveDaysTest() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, 1L);

        // 연속되지 않은 날짜의 연차 히스토리 생성 (1월 6일, 7일, 10일)
        VacationHistory history1 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 6, 9, 0), "testUser", "127.0.0.1");
        VacationHistory history2 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 7, 9, 0), "testUser", "127.0.0.1");
        VacationHistory history3 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 10, 9, 0), "testUser", "127.0.0.1"); // 3일 차이 (비연속)

        setVacationHistoryId(history1, 1L);
        setVacationHistoryId(history2, 2L);
        setVacationHistoryId(history3, 3L);

        List<VacationHistory> histories = List.of(history1, history2, history3);
        List<Vacation> vacations = List.of(vacation);

        given(vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(startDate, endDate))
                .willReturn(histories);
        given(vacationRepositoryImpl.findVacationsByIdsWithUser(anyList()))
                .willReturn(vacations);

        // When
        List<VacationServiceDto> result = vacationService.getPeriodVacationUseHistories(startDate, endDate);

        // Then
        assertThat(result).hasSize(2); // 연속된 2일 + 비연속된 1일 = 2개 그룹

        // 첫 번째 그룹 (1월 6일-7일)
        assertThat(result.get(0).getHistoryIds()).hasSize(2);
        assertThat(result.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 1, 6, 9, 0));

        // 두 번째 그룹 (1월 10일)
        assertThat(result.get(1).getHistoryIds()).hasSize(1);
        assertThat(result.get(1).getStartDate()).isEqualTo(LocalDateTime.of(2025, 1, 10, 9, 0));

        then(vacationHistoryRepositoryImpl).should().findVacationHistorysByPeriod(startDate, endDate);
        then(vacationRepositoryImpl).should().findVacationsByIdsWithUser(anyList());
    }

    @Test
    @DisplayName("기간별 휴가 사용 이력 조회 테스트 - 복합 케이스 (연차 + 시간단위)")
    void getPeriodVacationUseHistoriesComplexTest() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

        User user = createTestUser("user1", "9 ~ 6");
        Vacation vacation = createTestVacation(user, VacationType.ANNUAL, new BigDecimal("15.0000"));
        setVacationId(vacation, 1L);

        // 연차와 시간단위 휴가 혼합
        VacationHistory dayHistory1 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 6, 9, 0), "testUser", "127.0.0.1");
        VacationHistory dayHistory2 = VacationHistory.createUseVacationHistory(vacation, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 1, 7, 9, 0), "testUser", "127.0.0.1");
        VacationHistory hourHistory1 = VacationHistory.createUseVacationHistory(vacation, "오전반차", VacationTimeType.MORNINGOFF,
                LocalDateTime.of(2025, 1, 10, 9, 0), "testUser", "127.0.0.1");
        VacationHistory hourHistory2 = VacationHistory.createUseVacationHistory(vacation, "1시간휴가", VacationTimeType.ONETIMEOFF,
                LocalDateTime.of(2025, 1, 15, 14, 0), "testUser", "127.0.0.1");

        setVacationHistoryId(dayHistory1, 1L);
        setVacationHistoryId(dayHistory2, 2L);
        setVacationHistoryId(hourHistory1, 3L);
        setVacationHistoryId(hourHistory2, 4L);

        List<VacationHistory> histories = List.of(dayHistory1, dayHistory2, hourHistory1, hourHistory2);
        List<Vacation> vacations = List.of(vacation);

        given(vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(startDate, endDate))
                .willReturn(histories);
        given(vacationRepositoryImpl.findVacationsByIdsWithUser(anyList()))
                .willReturn(vacations);

        // When
        List<VacationServiceDto> result = vacationService.getPeriodVacationUseHistories(startDate, endDate);

        // Then
        assertThat(result).hasSize(3); // 연속 연차 1그룹 + 시간단위 2개 = 3개
        then(vacationHistoryRepositoryImpl).should().findVacationHistorysByPeriod(startDate, endDate);
        then(vacationRepositoryImpl).should().findVacationsByIdsWithUser(anyList());
    }

    @Test
    @DisplayName("기간별 휴가 사용 이력 조회 테스트 - 빈 결과")
    void getPeriodVacationUseHistoriesEmptyTest() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);

        given(vacationHistoryRepositoryImpl.findVacationHistorysByPeriod(startDate, endDate))
                .willReturn(Collections.emptyList());
        given(vacationRepositoryImpl.findVacationsByIdsWithUser(anyList()))
                .willReturn(Collections.emptyList());

        // When
        List<VacationServiceDto> result = vacationService.getPeriodVacationUseHistories(startDate, endDate);

        // Then
        assertThat(result).isEmpty();
        then(vacationHistoryRepositoryImpl).should().findVacationHistorysByPeriod(startDate, endDate);
        then(vacationRepositoryImpl).should().findVacationsByIdsWithUser(anyList());
    }


    // 테스트 헬퍼 메서드들
    private User createTestUser(String userId, String workTime) {
        return User.createUser(userId, "password", "테스트유저", "test@test.com", "19900101",
                CompanyType.SKAX, DepartmentType.SKC, workTime, "N");
    }

    private Vacation createTestVacation(User user, VacationType type, BigDecimal remainTime) {
        return Vacation.createVacation(user, type, remainTime,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                LocalDateTime.of(2025, 12, 31, 23, 59),
                "testUser", "127.0.0.1");
    }

    private void setVacationId(Vacation vacation, Long id) {
        try {
            java.lang.reflect.Field field = Vacation.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(vacation, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set vacation id", e);
        }
    }

    private void setVacationHistoryId(VacationHistory history, Long id) {
        try {
            java.lang.reflect.Field field = VacationHistory.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(history, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set vacation history id", e);
        }
    }
}
