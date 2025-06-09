package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.*;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.repository.VacationHistoryRepositoryImpl;
import com.lshdainty.myhr.repository.VacationRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("휴가 서비스 테스트")
class VacationServiceTest {
    // 삭제하지 말 것 (NullpointException 발생)
    @Mock
    private MessageSource ms;
    @Mock
    private VacationRepositoryImpl vacationRepositoryImpl;
    @Mock
    private VacationHistoryRepositoryImpl vacationHistoryRepositoryImpl;
    @Mock
    private UserRepositoryImpl userRepositoryImpl;
    @Mock
    private UserService userService;

    @InjectMocks
    private VacationService vacationService;

    @Test
    @DisplayName("연차 추가 테스트 - 성공 (기존 휴가 업데이트)")
    void registExistAnnualSuccessTest() {
        // Given
        Long userNo = 1L;
        String desc = "1분기 정기 휴가";
        VacationType type = VacationType.ANNUAL;
        BigDecimal grantTime = new BigDecimal("4.0000");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, type, grantTime,
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear())))
                .willReturn(Optional.of(vacation));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationRepositoryImpl).should().findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear()));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));

        assertThat(vacation.getRemainTime()).isEqualTo(grantTime.multiply(new BigDecimal("2")));
    }

    @Test
    @DisplayName("연차 추가 테스트 - 성공 (신규 휴가 등록)")
    void registNewAnnualSuccessTest() {
        // Given
        Long userNo = 1L;
        String desc = "1분기 정기 휴가";
        VacationType type = VacationType.ANNUAL;
        BigDecimal grantTime = new BigDecimal("4.0000");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear())))
                .willReturn(Optional.empty());
        willDoNothing().given(vacationRepositoryImpl).save(any(Vacation.class));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationRepositoryImpl).should().findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear()));
        then(vacationRepositoryImpl).should().save(argThat(vacation -> {
            assertThat(vacation.getOccurDate().getYear()).isEqualTo(occurDate.getYear());
            return true;
        }));
        then(vacationRepositoryImpl).should().save(argThat(vacation -> {
            assertThat(vacation.getExpiryDate().getYear()).isEqualTo(expiryDate.getYear());
            return true;
        }));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("출산 추가 테스트 - 성공")
    void registNewMaternitySuccessTest() {
        // Given
        Long userNo = 1L;
        String desc = "출산 휴가";
        VacationType type = VacationType.MATERNITY;
        BigDecimal grantTime = new BigDecimal("10.0000");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 6, 1, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(userService.checkUserExist(userNo)).willReturn(user);
        willDoNothing().given(vacationRepositoryImpl).save(any(Vacation.class));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("결혼 추가 테스트 - 성공")
    void registNewWeddingSuccessTest() {
        // Given
        Long userNo = 1L;
        String desc = "결혼 휴가";
        VacationType type = VacationType.WEDDING;
        BigDecimal grantTime = new BigDecimal("5.0000");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 6, 1, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(userService.checkUserExist(userNo)).willReturn(user);
        willDoNothing().given(vacationRepositoryImpl).save(any(Vacation.class));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("상조 추가 테스트 - 성공")
    void registNewBereavementSuccessTest() {
        // Given
        Long userNo = 1L;
        String desc = "상조 휴가";
        VacationType type = VacationType.BEREAVEMENT;
        BigDecimal grantTime = new BigDecimal("3.0000");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 6, 1, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(userService.checkUserExist(userNo)).willReturn(user);
        willDoNothing().given(vacationRepositoryImpl).save(any(Vacation.class));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationRepositoryImpl).should().save(any(Vacation.class));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("OT 추가 테스트 - 성공 (기존 휴가 업데이트)")
    void registExistOvertimeSuccessTest() {
        // Given
        Long userNo = 1L;
        String desc = "연장근무 휴가추가";
        VacationType type = VacationType.OVERTIME;
        BigDecimal grantTime = new BigDecimal("0.1250");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        Vacation vacation = Vacation.createVacation(user, type, grantTime,
                LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0),
                LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59),
                0L, "127.0.0.1");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear())))
                .willReturn(Optional.of(vacation));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationRepositoryImpl).should().findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear()));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));

        assertThat(vacation.getRemainTime()).isEqualTo(grantTime.multiply(new BigDecimal("2")));
    }

    @Test
    @DisplayName("OT 추가 테스트 - 성공 (신규 휴가 등록)")
    void registNewOvertimeSuccessTest() {
        // Given
        Long userNo = 1L;
        String desc = "연장근무 휴가추가";
        VacationType type = VacationType.OVERTIME;
        BigDecimal grantTime = new BigDecimal("0.1250");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationRepositoryImpl.findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear())))
                .willReturn(Optional.empty());
        willDoNothing().given(vacationRepositoryImpl).save(any(Vacation.class));
        willDoNothing().given(vacationHistoryRepositoryImpl).save(any(VacationHistory.class));

        // When
        vacationService.registVacation(userNo, desc, type, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");

        // Then
        then(userService).should().checkUserExist(userNo);
        then(vacationRepositoryImpl).should().findVacationByTypeWithYear(userNo, type, String.valueOf(now.getYear()));
        then(vacationRepositoryImpl).should().save(argThat(vacation -> {
            assertThat(vacation.getOccurDate().getYear()).isEqualTo(occurDate.getYear());
            return true;
        }));
        then(vacationRepositoryImpl).should().save(argThat(vacation -> {
            assertThat(vacation.getExpiryDate().getYear()).isEqualTo(expiryDate.getYear());
            return true;
        }));
        then(vacationHistoryRepositoryImpl).should().save(any(VacationHistory.class));
    }

    @Test
    @DisplayName("단일 유저 휴가 조회 테스트 - 성공")
    void getVacationsByUserSuccessTest() {
        // Given
        Long userNo = 1L;
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");

        LocalDateTime now = LocalDateTime.now();
        given(vacationRepositoryImpl.findVacationsByUserNo(userNo)).willReturn(List.of(
                Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1"),
                Vacation.createVacation(user, VacationType.MATERNITY, new BigDecimal("10.0000"), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59).plusMonths(6), 0L, "127.0.0.1"),
                Vacation.createVacation(user, VacationType.OVERTIME, new BigDecimal("0.3750"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1")
        ));

        // When
        List<Vacation> vacations = vacationService.findVacationsByUser(userNo);

        // Then
        then(vacationRepositoryImpl).should().findVacationsByUserNo(userNo);
        assertThat(vacations).hasSize(3);
        assertThat(vacations)
                .extracting("type")
                .containsExactlyInAnyOrder(VacationType.ANNUAL, VacationType.MATERNITY, VacationType.OVERTIME);
    }

    @Test
    @DisplayName("유저별 휴가 조회 테스트 - 성공")
    void getVacationsByUserGroupSuccessTest() {
        // Given
        User userA = User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N");
        User userB = User.createUser("김서연", "19701026", "BP", "8 ~ 5", "N");
        User userC = User.createUser("김지후", "19740115", "BP", "10 ~ 7", "Y");

        LocalDateTime now = LocalDateTime.now();
        Vacation v1 = Vacation.createVacation(userA, VacationType.ANNUAL, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
        Vacation v2 = Vacation.createVacation(userA, VacationType.MATERNITY, new BigDecimal("10.0000"), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59).plusMonths(6), 0L, "127.0.0.1");
        Vacation v3 = Vacation.createVacation(userA, VacationType.OVERTIME, new BigDecimal("0.3750"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
        Vacation v4 = Vacation.createVacation(userB, VacationType.ANNUAL, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");

        given(userRepositoryImpl.findUsersWithVacations()).willReturn(List.of(
                userA, userB, userC
        ));

        // When
        List<User> users = vacationService.findVacationsByUserGroup();

        // Then
        then(userRepositoryImpl).should().findUsersWithVacations();
        assertThat(users).hasSize(3);
        assertThat(users)
                .filteredOn(u -> u.getName().equals("이서준"))
                .filteredOn(v -> v.getVacations().size() == 3);
        assertThat(users)
                .filteredOn(u -> u.getName().equals("김서연"))
                .filteredOn(v -> v.getVacations().size() == 1);
        assertThat(users)
                .filteredOn(u -> u.getName().equals("김지후"))
                .filteredOn(v -> v.getVacations().isEmpty());
    }

    @Test
    @DisplayName("등록 가능 휴가 목록 조회 테스트 - 성공")
    void getAvailableVacationSuccessTest() {
        // Given
        Long userNo = 1L;
        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        LocalDateTime now = LocalDateTime.now();

        given(userService.checkUserExist(userNo)).willReturn(user);
        given(vacationRepositoryImpl.findVacationsByBaseTime(userNo, now)).willReturn(List.of(
                Vacation.createVacation(user, VacationType.ANNUAL, new BigDecimal("4.0000"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1"),
                Vacation.createVacation(user, VacationType.MATERNITY, new BigDecimal("10.0000"), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0), LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59).plusMonths(6), 0L, "127.0.0.1"),
                Vacation.createVacation(user, VacationType.OVERTIME, new BigDecimal("0.3750"), LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1")
        ));

        // When
        List<Vacation> vacations = vacationService.getAvailableVacation(userNo, now);

        // Then
        then(userService).should().checkUserExist(userNo);
        assertThat(vacations).hasSize(3);
        assertThat(vacations)
                .extracting("type")
                .containsExactlyInAnyOrder(VacationType.ANNUAL, VacationType.MATERNITY, VacationType.OVERTIME);
    }

    @Test
    @DisplayName("휴가 등록 내역 삭제 테스트 - 성공")
    void deleteRegistVacationHistorySuccessTest() {
        // Given
        Long historyId = 1L;
        Long vacationId = 1L;

        String desc = "1분기 정기 휴가";
        VacationType type = VacationType.ANNUAL;
        BigDecimal grantTime = new BigDecimal("4.0000");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");
        VacationHistory history = VacationHistory.createRegistVacationHistory(vacation, desc, grantTime, 0L, "127.0.0.1");

        setVacationId(vacation, vacationId);
        given(vacationHistoryRepositoryImpl.findById(historyId)).willReturn(Optional.of(history));
        given(vacationRepositoryImpl.findById(vacationId)).willReturn(Optional.of(vacation));

        // When
        vacationService.deleteVacationHistory(historyId,0L, "127.0.0.1");

        // Then
        then(vacationHistoryRepositoryImpl).should().findById(historyId);
        then(vacationRepositoryImpl).should().findById(vacationId);

        assertThat(history.getDelYN()).isEqualTo("Y");
        assertThat(vacation.getRemainTime()).isEqualTo(new BigDecimal("0.0000"));
    }

    @Test
    @DisplayName("휴가 삭제 테스트 - 성공 (사용 휴가 삭제)")
    void deleteUseVacationSuccessTest() {
        // Given
        Long historyId = 1L;
        Long vacationId = 1L;

        String desc = "연차";
        VacationTimeType timeType = VacationTimeType.DAYOFF;
        BigDecimal grantTime = new BigDecimal("4.0000");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime occurDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);

        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
        Vacation vacation = Vacation.createVacation(user, VacationType.ANNUAL, grantTime, occurDate, expiryDate, 0L, "127.0.0.1");
        VacationHistory history = VacationHistory.createUseVacationHistory(vacation, desc, timeType, 0L, "127.0.0.1");

        given(vacationRepositoryImpl.findById(vacationId)).willReturn(vacation);

        // When
        vacationService.deleteVacation(vacationId, 0L, "127.0.0.1");

        // Then
        then(vacationRepositoryImpl).should().findById(vacationId);
        assertThat(vacation.getDelYN()).isEqualTo("Y");
    }
//
//    @Test
//    @DisplayName("휴가 삭제 테스트 - 실패 (휴가 없음)")
//    void deleteVacationFailVacationNotFoundTest() {
//        // Given
//        Long vacationId = 900L;
//
//        given(vacationRepositoryImpl.findById(vacationId)).willReturn(null);
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () ->
//                vacationService.deleteVacation(vacationId, 0L, "127.0.0.1"));
//
//        then(vacationRepositoryImpl).should().findById(vacationId);
//    }
//
//    @Test
//    @DisplayName("등록 가능한 휴가 조회 테스트 - 성공")
//    void checkPossibleVacationSuccessTest() {
//        // given
//        LocalDateTime standardTime = LocalDateTime.now();
//
//        Long userNo = 1L;
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//
//        Vacation v1 = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(standardTime.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
//        Vacation v2 = Vacation.createVacation(user, "출산 휴가", "출산 추가 휴가 부여", VacationType.ADDED, new BigDecimal("10.0000"), LocalDateTime.of(standardTime.getYear(), standardTime.getMonth(), standardTime.getDayOfMonth(), 0, 0, 0), LocalDateTime.of(standardTime.getYear(), standardTime.getMonth(), standardTime.getDayOfMonth(), 23, 59, 59).plusMonths(6), 0L, "127.0.0.1");
//        Vacation v3 = Vacation.createVacation(user, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("0.3750"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(2025, 1, 31, 23, 59, 59), 0L, "127.0.0.1");
//        Vacation v4 = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(standardTime.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
//
//        Schedule s1 = Schedule.createSchedule(user, v1, "휴가", ScheduleType.DAYOFF, LocalDateTime.of(LocalDateTime.now().getYear(), 1, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 20, 23, 59, 59), 0L, "127.0.0.1");
//
//        ScheduleServiceDto scheduleDto = ScheduleServiceDto.builder()
//                .user(user)
//                .vacation(v1)
//                .type(ScheduleType.DAYOFF)
//                .realUsedTime(new BigDecimal("1.0000"))
//                .build();
//
//        given(userService.checkUserExist(userNo)).willReturn(user);
//        given(vacationRepositoryImpl.findVacationsByParameterTimeWithSchedules(userNo, standardTime)).willReturn(List.of(v1, v2, v3, v4));
//        setVacationSchedules(v1, List.of(s1));
//        given(scheduleService.convertRealUsedTimeDto(any())).willReturn(List.of(scheduleDto));
//
//        // when
//        List<VacationServiceDto> findVacation = vacationService.checkPossibleVacations(userNo, standardTime);
//
//        // then
//        then(userService).should().checkUserExist(userNo);
//        then(vacationRepositoryImpl).should().findVacationsByParameterTimeWithSchedules(userNo, standardTime);
//        assertThat(findVacation).hasSize(4);
//        assertThat(findVacation.get(0).getGrantTime()).isEqualTo(new BigDecimal("4.0000"));
//        assertThat(findVacation.get(0).getUsedTime()).isEqualTo(new BigDecimal("1.0000"));
//        assertThat(findVacation.get(0).getRemainTime()).isEqualTo(new BigDecimal("3.0000"));
//    }
//
//    @Test
//    @DisplayName("등록 가능한 휴가 조회 테스트 - 성공 (사용 가능 시간 없음)")
//    void checkPossibleVacationsNoAvailableTest() {
//        // given
//        LocalDateTime standardTime = LocalDateTime.now();
//
//        Long userNo = 1L;
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//
//        Vacation v1 = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(standardTime.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
//        Vacation v2 = Vacation.createVacation(user, "출산 휴가", "출산 추가 휴가 부여", VacationType.ADDED, new BigDecimal("10.0000"), LocalDateTime.of(standardTime.getYear(), standardTime.getMonth(), standardTime.getDayOfMonth(), 0, 0, 0), LocalDateTime.of(standardTime.getYear(), standardTime.getMonth(), standardTime.getDayOfMonth(), 23, 59, 59).plusMonths(6), 0L, "127.0.0.1");
//        Vacation v3 = Vacation.createVacation(user, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("0.3750"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(2025, 1, 31, 23, 59, 59), 0L, "127.0.0.1");
//        Vacation v4 = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(standardTime.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
//
//        Schedule s1 = Schedule.createSchedule(user, v1, "휴가", ScheduleType.DAYOFF, LocalDateTime.of(LocalDateTime.now().getYear(), 1, 20, 0, 0, 0), LocalDateTime.of(LocalDateTime.now().getYear(), 1, 23, 23, 59, 59), 0L, "127.0.0.1");
//
//        ScheduleServiceDto scheduleDto = ScheduleServiceDto.builder()
//                .user(user)
//                .vacation(v1)
//                .type(ScheduleType.DAYOFF)
//                .realUsedTime(new BigDecimal("4.0000"))
//                .build();
//
//        given(userService.checkUserExist(userNo)).willReturn(user);
//        given(vacationRepositoryImpl.findVacationsByParameterTimeWithSchedules(userNo, standardTime)).willReturn(List.of(v1, v2, v3, v4));
//        setVacationSchedules(v1, List.of(s1));
//        given(scheduleService.convertRealUsedTimeDto(any())).willReturn(List.of(scheduleDto));
//
//        // When
//        List<VacationServiceDto> findVacation = vacationService.checkPossibleVacations(userNo, standardTime);
//
//        // Then
//        then(userService).should().checkUserExist(userNo);
//        then(vacationRepositoryImpl).should().findVacationsByParameterTimeWithSchedules(userNo, standardTime);
//        assertThat(findVacation).hasSize(3);
//        assertThat(findVacation.get(0).getGrantTime()).isEqualTo(new BigDecimal("10.0000"));
//        assertThat(findVacation.get(0).getUsedTime()).isEqualTo(new BigDecimal("0.0000"));
//        assertThat(findVacation.get(0).getRemainTime()).isEqualTo(new BigDecimal("10.0000"));
//    }
//
//    @Test
//    @DisplayName("등록 가능한 휴가 조회 테스트 - 성공 (스케줄 없음)")
//    void checkPossibleVacationSuccessNoSchedulesTest() {
//        // Given
//        LocalDateTime standardTime = LocalDateTime.now();
//
//        Long userNo = 1L;
//        User user = User.createUser("이서준", "19700723", "9 ~ 6", "ADMIN", "N");
//
//        Vacation v1 = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(standardTime.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
//        Vacation v2 = Vacation.createVacation(user, "출산 휴가", "출산 추가 휴가 부여", VacationType.ADDED, new BigDecimal("10.0000"), LocalDateTime.of(standardTime.getYear(), standardTime.getMonth(), standardTime.getDayOfMonth(), 0, 0, 0), LocalDateTime.of(standardTime.getYear(), standardTime.getMonth(), standardTime.getDayOfMonth(), 23, 59, 59).plusMonths(6), 0L, "127.0.0.1");
//        Vacation v3 = Vacation.createVacation(user, "OT 정산", "월마감 지원", VacationType.ADDED, new BigDecimal("0.3750"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(2025, 1, 31, 23, 59, 59), 0L, "127.0.0.1");
//        Vacation v4 = Vacation.createVacation(user, "정기 휴가", "25년 1분기 정기 휴가", VacationType.BASIC, new BigDecimal("4.0000"), LocalDateTime.of(standardTime.getYear(), 1, 1, 0, 0, 0), LocalDateTime.of(standardTime.getYear(), 12, 31, 23, 59, 59), 0L, "127.0.0.1");
//
//        given(userService.checkUserExist(userNo)).willReturn(user);
//        given(vacationRepositoryImpl.findVacationsByParameterTimeWithSchedules(userNo, standardTime)).willReturn(List.of(v1, v2, v3, v4));
//
//        // When
//        List<VacationServiceDto> findVacation = vacationService.checkPossibleVacations(userNo, standardTime);
//
//        // Then
//        then(userService).should().checkUserExist(userNo);
//        then(vacationRepositoryImpl).should().findVacationsByParameterTimeWithSchedules(userNo, standardTime);
//        assertThat(findVacation).hasSize(4);
//        assertThat(findVacation.get(0).getGrantTime()).isEqualTo(new BigDecimal("4.0000"));
//        assertThat(findVacation.get(0).getUsedTime()).isEqualTo(new BigDecimal("0.0000"));
//        assertThat(findVacation.get(0).getRemainTime()).isEqualTo(new BigDecimal("4.0000"));
//    }
//
//    // 테스트 헬퍼 메서드
//    private void setUserNo(User user, Long no) {
//        try {
//            java.lang.reflect.Field field = User.class.getDeclaredField("id");
//            field.setAccessible(true);
//            field.set(user, no);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
    private void setVacationId(Vacation vacation, Long id) {
        try {
            java.lang.reflect.Field field = Vacation.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(vacation, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    private void setVacationSchedules(Vacation vacation, List<Schedule> schedules) {
//        try {
//            java.lang.reflect.Field field = Vacation.class.getDeclaredField("schedules");
//            field.setAccessible(true);
//            field.set(vacation, schedules);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
