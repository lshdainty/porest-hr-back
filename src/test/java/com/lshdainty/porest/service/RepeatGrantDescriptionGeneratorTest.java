package com.lshdainty.porest.service;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.service.policy.description.EnglishRepeatGrantDescriptionGenerator;
import com.lshdainty.porest.vacation.service.policy.description.KoreanRepeatGrantDescriptionGenerator;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import com.lshdainty.porest.vacation.type.VacationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DisplayName("반복 부여 설명 생성기 테스트")
class RepeatGrantDescriptionGeneratorTest {

    @Nested
    @DisplayName("KoreanRepeatGrantDescriptionGenerator")
    class KoreanGeneratorTest {

        private final KoreanRepeatGrantDescriptionGenerator generator = new KoreanRepeatGrantDescriptionGenerator();

        @Test
        @DisplayName("null 정책이면 null을 반환한다")
        void generateNullPolicy() {
            // when
            String result = generator.generate(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("REPEAT_GRANT가 아닌 정책이면 null을 반환한다")
        void generateNonRepeatGrantPolicy() {
            // given
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "수동 부여",
                    "수동 부여 정책",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    YNType.N,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("매년 1월 1일 부여 - 기본 연간 부여")
        void generateYearlyJanuary1st() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "연차",
                    "연간 연차",
                    VacationType.ANNUAL,
                    new BigDecimal("15.0000"),
                    YNType.N,
                    RepeatUnit.YEARLY,
                    1,
                    1,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("매년");
            assertThat(result).contains("1월");
            assertThat(result).contains("1일");
            assertThat(result).contains("부여");
        }

        @Test
        @DisplayName("2년 간격으로 매년 부여")
        void generateEveryTwoYears() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "장기근속",
                    "장기근속 휴가",
                    VacationType.ANNUAL,
                    new BigDecimal("3.0000"),
                    YNType.N,
                    RepeatUnit.YEARLY,
                    2,
                    1,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("2년");
            assertThat(result).contains("간격");
        }

        @Test
        @DisplayName("매월 15일 부여")
        void generateMonthly15th() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "월간",
                    "월간 휴가",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    15,
                    LocalDateTime.of(2025, 1, 15, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("매월");
            assertThat(result).contains("15일");
            assertThat(result).contains("부여");
        }

        @Test
        @DisplayName("분기마다 1일 부여")
        void generateQuarterly() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "분기",
                    "분기 휴가",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.QUARTERLY,
                    1,
                    null,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("분기마다");
            assertThat(result).contains("1일");
            assertThat(result).contains("부여");
        }

        @Test
        @DisplayName("반기마다 부여")
        void generateHalfYearly() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "반기",
                    "반기 휴가",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.HALF,
                    1,
                    null,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("반기마다");
            assertThat(result).contains("부여");
        }

        @Test
        @DisplayName("매일 부여")
        void generateDaily() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "일간",
                    "일간 휴가",
                    VacationType.ANNUAL,
                    new BigDecimal("0.1250"),
                    YNType.N,
                    RepeatUnit.DAILY,
                    1,
                    null,
                    null,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("매일");
            assertThat(result).contains("부여");
        }

        @Test
        @DisplayName("1회성 부여 - 7년 후 1회")
        void generateOneTimeGrant() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "장기근속",
                    "7년차 장기근속",
                    VacationType.ANNUAL,
                    new BigDecimal("3.0000"),
                    YNType.N,
                    RepeatUnit.YEARLY,
                    7,
                    1,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.N,
                    1,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("1회 부여");
        }
    }

    @Nested
    @DisplayName("EnglishRepeatGrantDescriptionGenerator")
    class EnglishGeneratorTest {

        private final EnglishRepeatGrantDescriptionGenerator generator = new EnglishRepeatGrantDescriptionGenerator();

        @Test
        @DisplayName("null 정책이면 null을 반환한다")
        void generateNullPolicy() {
            // when
            String result = generator.generate(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("REPEAT_GRANT가 아닌 정책이면 null을 반환한다")
        void generateNonRepeatGrantPolicy() {
            // given
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "Manual Grant",
                    "Manual grant policy",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    YNType.N,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Grant annually on January 1st")
        void generateYearlyJanuary1st() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Annual",
                    "Annual leave",
                    VacationType.ANNUAL,
                    new BigDecimal("15.0000"),
                    YNType.N,
                    RepeatUnit.YEARLY,
                    1,
                    1,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("Grant");
            assertThat(result).contains("annually");
            assertThat(result).contains("January");
            assertThat(result).contains("1st");
        }

        @Test
        @DisplayName("Grant every 2 years")
        void generateEveryTwoYears() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Long service",
                    "Long service leave",
                    VacationType.ANNUAL,
                    new BigDecimal("3.0000"),
                    YNType.N,
                    RepeatUnit.YEARLY,
                    2,
                    1,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("every 2 years");
        }

        @Test
        @DisplayName("Grant monthly on the 15th")
        void generateMonthly15th() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Monthly",
                    "Monthly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    15,
                    LocalDateTime.of(2025, 1, 15, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("monthly");
            assertThat(result).contains("15th");
        }

        @Test
        @DisplayName("Grant quarterly on the 1st")
        void generateQuarterly() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Quarterly",
                    "Quarterly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.QUARTERLY,
                    1,
                    null,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("quarterly");
            assertThat(result).contains("1st");
        }

        @Test
        @DisplayName("Grant semi-annually")
        void generateHalfYearly() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Half",
                    "Half yearly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.HALF,
                    1,
                    null,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("semi-annually");
        }

        @Test
        @DisplayName("Grant daily")
        void generateDaily() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Daily",
                    "Daily leave",
                    VacationType.ANNUAL,
                    new BigDecimal("0.1250"),
                    YNType.N,
                    RepeatUnit.DAILY,
                    1,
                    null,
                    null,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("daily");
        }

        @Test
        @DisplayName("1 time(s) only")
        void generateOneTimeGrant() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Long service",
                    "7 year long service",
                    VacationType.ANNUAL,
                    new BigDecimal("3.0000"),
                    YNType.N,
                    RepeatUnit.YEARLY,
                    7,
                    1,
                    1,
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    YNType.N,
                    1,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("1 time(s) only");
        }

        @Test
        @DisplayName("서수 테스트 - 2nd")
        void generateOrdinal2nd() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Monthly",
                    "Monthly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    2,
                    LocalDateTime.of(2025, 1, 2, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("2nd");
        }

        @Test
        @DisplayName("서수 테스트 - 3rd")
        void generateOrdinal3rd() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Monthly",
                    "Monthly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    3,
                    LocalDateTime.of(2025, 1, 3, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("3rd");
        }

        @Test
        @DisplayName("서수 테스트 - 11th (예외 케이스)")
        void generateOrdinal11th() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Monthly",
                    "Monthly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    11,
                    LocalDateTime.of(2025, 1, 11, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("11th");
        }

        @Test
        @DisplayName("서수 테스트 - 12th (예외 케이스)")
        void generateOrdinal12th() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Monthly",
                    "Monthly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    12,
                    LocalDateTime.of(2025, 1, 12, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("12th");
        }

        @Test
        @DisplayName("서수 테스트 - 13th (예외 케이스)")
        void generateOrdinal13th() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Monthly",
                    "Monthly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    13,
                    LocalDateTime.of(2025, 1, 13, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("13th");
        }

        @Test
        @DisplayName("서수 테스트 - 21st")
        void generateOrdinal21st() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Monthly",
                    "Monthly leave",
                    VacationType.ANNUAL,
                    new BigDecimal("1.0000"),
                    YNType.N,
                    RepeatUnit.MONTHLY,
                    1,
                    null,
                    21,
                    LocalDateTime.of(2025, 1, 21, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("21st");
        }

        @Test
        @DisplayName("매년 특정 일만 지정 (월 없이)")
        void generateYearlyWithOnlyDay() {
            // given
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "Annual",
                    "Annual leave",
                    VacationType.ANNUAL,
                    new BigDecimal("15.0000"),
                    YNType.N,
                    RepeatUnit.YEARLY,
                    1,
                    null,
                    15,
                    LocalDateTime.of(2025, 1, 15, 0, 0),
                    YNType.Y,
                    null,
                    EffectiveType.START_OF_YEAR,
                    ExpirationType.END_OF_YEAR
            );

            // when
            String result = generator.generate(policy);

            // then
            assertThat(result).contains("on the 15th");
        }
    }
}
