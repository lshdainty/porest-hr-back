package com.lshdainty.porest.service;

import com.lshdainty.porest.vacation.service.VacationTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("휴가 시간 포맷터 테스트")
class VacationTimeFormatterTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private VacationTimeFormatter vacationTimeFormatter;

    private void setupKoreanMessages() {
        given(messageSource.getMessage(eq("unit.day"), any(), any(Locale.class))).willReturn("일");
        given(messageSource.getMessage(eq("unit.hour"), any(), any(Locale.class))).willReturn("시간");
        given(messageSource.getMessage(eq("unit.minute"), any(), any(Locale.class))).willReturn("분");
    }

    private void setupEnglishMessages() {
        given(messageSource.getMessage(eq("unit.day"), any(), any(Locale.class))).willReturn(" days");
        given(messageSource.getMessage(eq("unit.hour"), any(), any(Locale.class))).willReturn(" hours");
        given(messageSource.getMessage(eq("unit.minute"), any(), any(Locale.class))).willReturn(" minutes");
    }

    @Nested
    @DisplayName("format - 한국어")
    class FormatKorean {
        @BeforeEach
        void setUp() {
            setupKoreanMessages();
        }

        @Test
        @DisplayName("성공 - null이면 기본값 0일을 반환한다")
        void formatNullReturnsDefault() {
            // when
            String result = vacationTimeFormatter.format(null, Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("0일");
        }

        @Test
        @DisplayName("성공 - 0이면 기본값 0일을 반환한다")
        void formatZeroReturnsDefault() {
            // when
            String result = vacationTimeFormatter.format(BigDecimal.ZERO, Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("0일");
        }

        @Test
        @DisplayName("성공 - 음수이면 기본값 0일을 반환한다")
        void formatNegativeReturnsDefault() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("-1.0000"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("0일");
        }

        @Test
        @DisplayName("성공 - 1일을 포맷한다")
        void formatOneDay() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("1.0000"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("1일");
        }

        @Test
        @DisplayName("성공 - 2일을 포맷한다")
        void formatTwoDays() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("2.0000"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("2일");
        }

        @Test
        @DisplayName("성공 - 1시간을 포맷한다 (0.125 = 1시간)")
        void formatOneHour() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("0.1250"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("1시간");
        }

        @Test
        @DisplayName("성공 - 4시간을 포맷한다 (0.5 = 4시간)")
        void formatFourHours() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("0.5000"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("4시간");
        }

        @Test
        @DisplayName("성공 - 30분을 포맷한다 (0.0625 = 30분)")
        void formatThirtyMinutes() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("0.0625"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("30분");
        }

        @Test
        @DisplayName("성공 - 1일 4시간을 포맷한다")
        void formatOneDayFourHours() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("1.5000"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("1일 4시간");
        }

        @Test
        @DisplayName("성공 - 2일 3시간을 포맷한다")
        void formatTwoDaysThreeHours() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("2.3750"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("2일 3시간");
        }

        @Test
        @DisplayName("성공 - 1시간 30분을 포맷한다")
        void formatOneHourThirtyMinutes() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("0.1875"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("1시간 30분");
        }

        @Test
        @DisplayName("성공 - 1일 2시간 30분을 포맷한다")
        void formatOneDayTwoHoursThirtyMinutes() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("1.3125"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("1일 2시간 30분");
        }

        @Test
        @DisplayName("성공 - 15일을 포맷한다")
        void formatFifteenDays() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("15.0000"), Locale.KOREAN);

            // then
            assertThat(result).isEqualTo("15일");
        }
    }

    @Nested
    @DisplayName("format - 영어")
    class FormatEnglish {
        @BeforeEach
        void setUp() {
            setupEnglishMessages();
        }

        @Test
        @DisplayName("성공 - null이면 기본값 0 days를 반환한다")
        void formatNullReturnsDefault() {
            // when
            String result = vacationTimeFormatter.format(null, Locale.ENGLISH);

            // then
            assertThat(result).isEqualTo("0 days");
        }

        @Test
        @DisplayName("성공 - 1일을 포맷한다")
        void formatOneDay() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("1.0000"), Locale.ENGLISH);

            // then
            assertThat(result).isEqualTo("1 days");
        }

        @Test
        @DisplayName("성공 - 2일 3시간을 포맷한다")
        void formatTwoDaysThreeHours() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("2.3750"), Locale.ENGLISH);

            // then
            assertThat(result).isEqualTo("2 days 3 hours");
        }
    }

    @Nested
    @DisplayName("format - 현재 Locale 사용")
    class FormatCurrentLocale {
        @BeforeEach
        void setUp() {
            setupKoreanMessages();
        }

        @Test
        @DisplayName("성공 - 현재 Locale에 맞게 포맷한다")
        void formatWithCurrentLocale() {
            // when
            String result = vacationTimeFormatter.format(new BigDecimal("1.5000"));

            // then
            assertThat(result).isEqualTo("1일 4시간");
        }
    }
}
