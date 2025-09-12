package com.lshdainty.porest.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("porest Time Util 테스트")
class PorestTimeTest {
    @Mock
    private MessageSource ms;

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 성공")
    void isAfterThanEndDateSuccessTest() {
        // Given & When & Then
        assertThat(PorestTime.isAfterThanEndDate(
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0))
        ).isTrue();
    }

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 실패 (시작 시간이 종료 시간 이전임)")
    void isAfterThanEndDateFailTest() {
        // Given & When & Then
        assertThat(PorestTime.isAfterThanEndDate(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 2, 0, 0, 0))
        ).isFalse();
    }

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 실패 (시작 시간과 종료 시간 동일함)")
    void isAfterThanEndDateSuccessTestSameTime() {
        // Given & When & Then
        assertThat(PorestTime.isAfterThanEndDate(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0))
        ).isFalse();
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 날짜 목록 구하기 - 성공")
    void getBetweenDatesSuccessTest() {
        // Given & When & Then
        assertThat(PorestTime.getBetweenDates(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 5, 0, 0, 0),
                ms
        )).containsExactly(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3),
                LocalDate.of(2025, 1, 4),
                LocalDate.of(2025, 1, 5)
        );
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 날짜 목록 구하기 - 실패 (Start, End 시간 반대)")
    void getBetweenDatesSuccessTestReverseStartEndDate() {
        // Given
        when(ms.getMessage("error.validate.startIsAfterThanEnd", null, null)).thenReturn("");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            PorestTime.getBetweenDates(
                    LocalDateTime.of(2025, 1, 10, 0, 0, 0),
                    LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                    ms
            );
        });
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 요일에 해당하는 날짜 목록 구하기 - 성공")
    void getBetweenDatesByDayOfWeekSuccessTest() {
        // Given & When & Then
        assertThat(PorestTime.getBetweenDatesByDayOfWeek(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 10, 0, 0, 0),
                new int[]{6, 7},
                ms
        )).containsExactly(
                LocalDate.of(2025, 1, 4),
                LocalDate.of(2025, 1, 5)
        );
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 요일에 해당하는 날짜 목록 구하기 - 실패 (잘못된 요일 입력)")
    void getBetweenDatesByDayOfWeekSuccessTestWrongDay() {
        // Given
        when(ms.getMessage("error.validate.dayOfWeek", null, null)).thenReturn("");

        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            PorestTime.getBetweenDatesByDayOfWeek(
                    LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2025, 1, 10, 0, 0, 0),
                    new int[]{8},
                    ms
            );
        });
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 요일 - 엣지 케이스 (같은 날)")
    void getBetweenDatesByDayOfWeekEdgeCase() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 1, 6, 0, 0, 0); // 월요일
        LocalDateTime end = LocalDateTime.of(2025, 1, 6, 23, 59, 59); // 같은 날
        int[] daysOfWeek = {1}; // 월요일만

        // When
        List<LocalDate> result = PorestTime.getBetweenDatesByDayOfWeek(start, end, daysOfWeek, ms);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(LocalDate.of(2025, 1, 6));
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 요일 - 경계값 테스트 (7일)")
    void getBetweenDatesByDayOfWeekBoundaryTest() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0, 0); // 수요일
        LocalDateTime end = LocalDateTime.of(2025, 1, 7, 0, 0, 0);   // 화요일
        int[] daysOfWeek = {7}; // 일요일

        // When
        List<LocalDate> result = PorestTime.getBetweenDatesByDayOfWeek(start, end, daysOfWeek, ms);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(LocalDate.of(2025, 1, 5)); // 2025-01-05는 일요일
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 요일 - 빈 결과")
    void getBetweenDatesByDayOfWeekEmptyResult() {
        // Given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0, 0); // 수요일
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 0, 0, 0);   // 목요일
        int[] daysOfWeek = {1}; // 월요일만

        // When
        List<LocalDate> result = PorestTime.getBetweenDatesByDayOfWeek(start, end, daysOfWeek, ms);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("날짜 목록 더하기 - 성공")
    void addAllDates() {
        // Given
        List<LocalDate> sourceDates = List.of(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3)
        );
        List<LocalDate> targetDates = List.of(
                LocalDate.of(2025, 1, 3),
                LocalDate.of(2025, 1, 4),
                LocalDate.of(2025, 1, 5)
        );

        // When & Then
        assertThat(PorestTime.addAllDates(sourceDates, targetDates)).containsExactlyInAnyOrder(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3),
                LocalDate.of(2025, 1, 4),
                LocalDate.of(2025, 1, 5)
        );
    }

    @Test
    @DisplayName("날짜 목록 더하기 - 빈 목록")
    void addAllDatesEmpty() {
        // Given
        List<LocalDate> sourceDates = Collections.emptyList();
        List<LocalDate> targetDates = List.of(LocalDate.of(2025, 1, 1));

        // When & Then
        assertThat(PorestTime.addAllDates(sourceDates, targetDates))
                .containsExactly(LocalDate.of(2025, 1, 1));
    }

    @Test
    @DisplayName("날짜 목록 빼기 - 성공")
    void removeAllDates() {
        // Given
        List<LocalDate> sourceDates = List.of(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2),
                LocalDate.of(2025, 1, 3)
        );
        List<LocalDate> targetDates = List.of(
                LocalDate.of(2025, 1, 3),
                LocalDate.of(2025, 1, 4),
                LocalDate.of(2025, 1, 5)
        );

        // When & Then
        assertThat(PorestTime.removeAllDates(sourceDates, targetDates)).containsExactlyInAnyOrder(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2)
        );
    }

    @Test
    @DisplayName("날짜 목록 빼기 - 전체 제거")
    void removeAllDatesComplete() {
        // Given
        List<LocalDate> sourceDates = List.of(LocalDate.of(2025, 1, 1));
        List<LocalDate> targetDates = List.of(LocalDate.of(2025, 1, 1));

        // When & Then
        assertThat(PorestTime.removeAllDates(sourceDates, targetDates)).isEmpty();
    }

    @Test
    @DisplayName("날짜 목록 중 가장 큰 날짜 찾기 - 성공")
    void findMaxDateTime() {
        // Given
        List<LocalDateTime> dateTimes = List.of(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 5, 12, 0, 0),
                LocalDateTime.of(2025, 1, 3, 23, 59, 59)
        );

        // When & Then
        assertThat(PorestTime.findMaxDateTime(dateTimes, ms)).isEqualTo(LocalDateTime.of(2025, 1, 5, 12, 0, 0));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 큰 날짜 찾기 - 실패 (null)")
    void findMaxDateTimeFailTestNull() {
        // Given
        when(ms.getMessage("error.validate.parameter.null", null, null)).thenReturn("Parameter is null");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PorestTime.findMaxDateTime(null, ms));

        assertThat(exception.getMessage()).isEqualTo("Parameter is null");
    }

    @Test
    @DisplayName("날짜 목록 중 가장 큰 날짜 찾기 - 실패 (empty)")
    void findMaxDateTimeFailTestEmpty() {
        // Given
        when(ms.getMessage("error.validate.parameter.null", null, null)).thenReturn("Parameter is empty");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PorestTime.findMaxDateTime(Collections.emptyList(), ms));

        assertThat(exception.getMessage()).isEqualTo("Parameter is empty");
    }

    @Test
    @DisplayName("날짜 목록 중 가장 큰 날짜 찾기 - 단일 원소")
    void findMaxDateTimeSingleElement() {
        // Given
        List<LocalDateTime> dateTimes = List.of(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        // When & Then
        assertThat(PorestTime.findMaxDateTime(dateTimes, ms))
                .isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 작은 날짜 찾기 - 성공")
    void findMinDateTime() {
        // Given
        List<LocalDateTime> dateTimes = List.of(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 5, 12, 0, 0),
                LocalDateTime.of(2025, 1, 3, 23, 59, 59)
        );

        // When & Then
        assertThat(PorestTime.findMinDateTime(dateTimes, ms)).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 작은 날짜 찾기 - 실패 (null)")
    void findMinDateTimeFailTestNull() {
        // Given
        when(ms.getMessage("error.validate.parameter.null", null, null)).thenReturn("Parameter is null");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PorestTime.findMinDateTime(null, ms));

        assertThat(exception.getMessage()).isEqualTo("Parameter is null");
    }

    @Test
    @DisplayName("날짜 목록 중 가장 작은 날짜 찾기 - 실패 (empty)")
    void findMinDateTimeFailTestEmpty() {
        // Given
        when(ms.getMessage("error.validate.parameter.null", null, null)).thenReturn("Parameter is empty");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PorestTime.findMinDateTime(Collections.emptyList(), ms));

        assertThat(exception.getMessage()).isEqualTo("Parameter is empty");
    }

    @Test
    @DisplayName("날짜 목록 중 가장 작은 날짜 찾기 - 단일 원소")
    void findMinDateTimeSingleElement() {
        // Given
        List<LocalDateTime> dateTimes = List.of(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        // When & Then
        assertThat(PorestTime.findMinDateTime(dateTimes, ms))
                .isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }
}