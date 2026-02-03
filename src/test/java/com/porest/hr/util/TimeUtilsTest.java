package com.porest.hr.util;

import com.porest.core.exception.InvalidValueException;
import com.porest.core.util.TimeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TimeUtils 테스트")
class TimeUtilsTest {

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 성공")
    void isAfterThanEndDateSuccessTest() {
        // Given & When & Then
        assertThat(TimeUtils.isAfter(
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0))
        ).isTrue();
    }

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 실패 (시작 시간이 종료 시간 이전임)")
    void isAfterThanEndDateFailTest() {
        // Given & When & Then
        assertThat(TimeUtils.isAfter(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 2, 0, 0, 0))
        ).isFalse();
    }

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 실패 (시작 시간과 종료 시간 동일함)")
    void isAfterThanEndDateSuccessTestSameTime() {
        // Given & When & Then
        assertThat(TimeUtils.isAfter(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0))
        ).isFalse();
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 날짜 목록 구하기 - 성공")
    void getBetweenDatesSuccessTest() {
        // Given & When & Then
        assertThat(TimeUtils.getDateRange(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 5, 0, 0, 0)
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
        // Given & When & Then
        assertThrows(InvalidValueException.class, () -> {
            TimeUtils.getDateRange(
                    LocalDateTime.of(2025, 1, 10, 0, 0, 0),
                    LocalDateTime.of(2025, 1, 1, 0, 0, 0)
            );
        });
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 요일에 해당하는 날짜 목록 구하기 - 성공")
    void getBetweenDatesByDayOfWeekSuccessTest() {
        // Given & When & Then
        assertThat(TimeUtils.filterByDayOfWeek(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 10, 0, 0, 0),
                new int[]{6, 7}
        )).containsExactly(
                LocalDate.of(2025, 1, 4),
                LocalDate.of(2025, 1, 5)
        );
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 요일에 해당하는 날짜 목록 구하기 - 실패 (잘못된 요일 입력)")
    void getBetweenDatesByDayOfWeekSuccessTestWrongDay() {
        // Given & When & Then
        assertThrows(InvalidValueException.class, () -> {
            TimeUtils.filterByDayOfWeek(
                    LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2025, 1, 10, 0, 0, 0),
                    new int[]{8}
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
        List<LocalDate> result = TimeUtils.filterByDayOfWeek(start, end, daysOfWeek);

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
        List<LocalDate> result = TimeUtils.filterByDayOfWeek(start, end, daysOfWeek);

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
        List<LocalDate> result = TimeUtils.filterByDayOfWeek(start, end, daysOfWeek);

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
        assertThat(TimeUtils.mergeDates(sourceDates, targetDates)).containsExactlyInAnyOrder(
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
        assertThat(TimeUtils.mergeDates(sourceDates, targetDates))
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
        assertThat(TimeUtils.excludeDates(sourceDates, targetDates)).containsExactlyInAnyOrder(
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
        assertThat(TimeUtils.excludeDates(sourceDates, targetDates)).isEmpty();
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
        assertThat(TimeUtils.latest(dateTimes)).isEqualTo(LocalDateTime.of(2025, 1, 5, 12, 0, 0));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 큰 날짜 찾기 - 실패 (null)")
    void findMaxDateTimeFailTestNull() {
        // When & Then
        assertThrows(InvalidValueException.class,
                () -> TimeUtils.latest(null));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 큰 날짜 찾기 - 실패 (empty)")
    void findMaxDateTimeFailTestEmpty() {
        // When & Then
        assertThrows(InvalidValueException.class,
                () -> TimeUtils.latest(Collections.emptyList()));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 큰 날짜 찾기 - 단일 원소")
    void findMaxDateTimeSingleElement() {
        // Given
        List<LocalDateTime> dateTimes = List.of(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        // When & Then
        assertThat(TimeUtils.latest(dateTimes))
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
        assertThat(TimeUtils.earliest(dateTimes)).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 작은 날짜 찾기 - 실패 (null)")
    void findMinDateTimeFailTestNull() {
        // When & Then
        assertThrows(InvalidValueException.class,
                () -> TimeUtils.earliest(null));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 작은 날짜 찾기 - 실패 (empty)")
    void findMinDateTimeFailTestEmpty() {
        // When & Then
        assertThrows(InvalidValueException.class,
                () -> TimeUtils.earliest(Collections.emptyList()));
    }

    @Test
    @DisplayName("날짜 목록 중 가장 작은 날짜 찾기 - 단일 원소")
    void findMinDateTimeSingleElement() {
        // Given
        List<LocalDateTime> dateTimes = List.of(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        // When & Then
        assertThat(TimeUtils.earliest(dateTimes))
                .isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }
}
