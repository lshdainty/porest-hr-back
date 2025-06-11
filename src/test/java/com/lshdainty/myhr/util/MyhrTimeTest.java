package com.lshdainty.myhr.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("myHR Time Util 테스트")
class MyhrTimeTest {
    @Mock
    private MessageSource ms;

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 성공")
    void isAfterThanEndDateSuccessTest() {
        // Given & When & Then
        assertThat(MyhrTime.isAfterThanEndDate(
                LocalDateTime.of(2025, 1, 2, 0, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0))
        ).isTrue();
    }

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 실패 (시작 시간이 종료 시간 이전임)")
    void isAfterThanEndDateFailTest() {
        // Given & When & Then
        assertThat(MyhrTime.isAfterThanEndDate(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 2, 0, 0, 0))
        ).isFalse();
    }

    @Test
    @DisplayName("시작 시간이 종료 시간 이후인지 확인 - 실패 (시작 시간과 종료 시간 동일함)")
    void isAfterThanEndDateSuccessTestSameTime() {
        // Given & When & Then
        assertThat(MyhrTime.isAfterThanEndDate(
                LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0))
        ).isFalse();
    }

    @Test
    @DisplayName("시작 시간과 종료 시간 사이의 날짜 목록 구하기 - 성공")
    void getBetweenDatesSuccessTest() {
        // Given & When & Then
        assertThat(MyhrTime.getBetweenDates(
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
            MyhrTime.getBetweenDates(
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
        assertThat(MyhrTime.getBetweenDatesByDayOfWeek(
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
            MyhrTime.getBetweenDatesByDayOfWeek(
                    LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2025, 1, 10, 0, 0, 0),
                    new int[]{8},
                    ms
            );
        });
    }

    @Test
    void addAllDates() {
    }

    @Test
    void removeAllDates() {
    }

    @Test
    void findMaxDateTime() {
    }

    @Test
    void findMinDateTime() {
    }
}