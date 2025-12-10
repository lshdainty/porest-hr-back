package com.lshdainty.porest.service;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.service.policy.description.EnglishRepeatGrantDescriptionGenerator;
import com.lshdainty.porest.vacation.service.policy.description.KoreanRepeatGrantDescriptionGenerator;
import com.lshdainty.porest.vacation.service.policy.description.RepeatGrantDescriptionFactory;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import com.lshdainty.porest.vacation.type.VacationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("반복 부여 설명 팩토리 테스트")
class RepeatGrantDescriptionFactoryTest {

    @Mock
    private KoreanRepeatGrantDescriptionGenerator koreanGenerator;

    @Mock
    private EnglishRepeatGrantDescriptionGenerator englishGenerator;

    private RepeatGrantDescriptionFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RepeatGrantDescriptionFactory(koreanGenerator, englishGenerator);
    }

    private VacationPolicy createRepeatGrantPolicy() {
        return VacationPolicy.createRepeatGrantPolicy(
                "연차",
                "연간 연차 휴가",
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
    }

    @Nested
    @DisplayName("generate - 현재 Locale 사용")
    class GenerateWithCurrentLocale {
        @Test
        @DisplayName("성공 - 한국어 Locale에서 한국어 설명을 반환한다")
        void generateKoreanDescription() {
            // given
            LocaleContextHolder.setLocale(Locale.KOREAN);
            VacationPolicy policy = createRepeatGrantPolicy();
            String expectedDescription = "매년 1월 1일 부여";

            given(koreanGenerator.generate(any(VacationPolicy.class))).willReturn(expectedDescription);

            // when
            String result = factory.generate(policy);

            // then
            assertThat(result).isEqualTo(expectedDescription);

            // cleanup
            LocaleContextHolder.resetLocaleContext();
        }

        @Test
        @DisplayName("성공 - 영어 Locale에서 영어 설명을 반환한다")
        void generateEnglishDescription() {
            // given
            LocaleContextHolder.setLocale(Locale.ENGLISH);
            VacationPolicy policy = createRepeatGrantPolicy();
            String expectedDescription = "Grant annually on January 1st";

            given(englishGenerator.generate(any(VacationPolicy.class))).willReturn(expectedDescription);

            // when
            String result = factory.generate(policy);

            // then
            assertThat(result).isEqualTo(expectedDescription);

            // cleanup
            LocaleContextHolder.resetLocaleContext();
        }
    }

    @Nested
    @DisplayName("generate - 특정 Locale 지정")
    class GenerateWithSpecificLocale {
        @Test
        @DisplayName("성공 - 한국어 Locale을 지정하면 한국어 설명을 반환한다")
        void generateWithKoreanLocale() {
            // given
            VacationPolicy policy = createRepeatGrantPolicy();
            String expectedDescription = "매년 1월 1일 부여";

            given(koreanGenerator.generate(any(VacationPolicy.class))).willReturn(expectedDescription);

            // when
            String result = factory.generate(policy, Locale.KOREAN);

            // then
            assertThat(result).isEqualTo(expectedDescription);
        }

        @Test
        @DisplayName("성공 - 영어 Locale을 지정하면 영어 설명을 반환한다")
        void generateWithEnglishLocale() {
            // given
            VacationPolicy policy = createRepeatGrantPolicy();
            String expectedDescription = "Grant annually on January 1st";

            given(englishGenerator.generate(any(VacationPolicy.class))).willReturn(expectedDescription);

            // when
            String result = factory.generate(policy, Locale.ENGLISH);

            // then
            assertThat(result).isEqualTo(expectedDescription);
        }

        @Test
        @DisplayName("성공 - null Locale이면 한국어 설명을 반환한다")
        void generateWithNullLocale() {
            // given
            VacationPolicy policy = createRepeatGrantPolicy();
            String expectedDescription = "매년 1월 1일 부여";

            given(koreanGenerator.generate(any(VacationPolicy.class))).willReturn(expectedDescription);

            // when
            String result = factory.generate(policy, null);

            // then
            assertThat(result).isEqualTo(expectedDescription);
        }

        @Test
        @DisplayName("성공 - 지원하지 않는 Locale이면 영어 설명을 반환한다")
        void generateWithUnsupportedLocale() {
            // given
            VacationPolicy policy = createRepeatGrantPolicy();
            String expectedDescription = "Grant annually on January 1st";

            given(englishGenerator.generate(any(VacationPolicy.class))).willReturn(expectedDescription);

            // when
            String result = factory.generate(policy, Locale.FRENCH);

            // then
            assertThat(result).isEqualTo(expectedDescription);
        }
    }
}
