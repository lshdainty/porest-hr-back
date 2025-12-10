package com.lshdainty.porest.vacation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

/**
 * 휴가 시간 값을 다국어 문자열로 변환하는 포맷터
 * 예: 2.3750 → "2일 3시간" (한국어) / "2 days 3 hours" (영어)
 */
@Component
@RequiredArgsConstructor
public class VacationTimeFormatter {
    private final MessageSource messageSource;

    /**
     * 휴가 시간 값을 현재 Locale에 맞는 문자열로 변환
     *
     * @param value 휴가 시간 값 (1.0 = 1일, 0.125 = 1시간, 0.0625 = 30분)
     * @return 다국어 문자열 (예: "2일 3시간", "2 days 3 hours")
     */
    public String format(BigDecimal value) {
        return format(value, LocaleContextHolder.getLocale());
    }

    /**
     * 휴가 시간 값을 지정된 Locale에 맞는 문자열로 변환
     *
     * @param value 휴가 시간 값
     * @param locale 언어 설정
     * @return 다국어 문자열
     */
    public String format(BigDecimal value, Locale locale) {
        String dayUnit = messageSource.getMessage("unit.day", null, locale);
        String hourUnit = messageSource.getMessage("unit.hour", null, locale);
        String minuteUnit = messageSource.getMessage("unit.minute", null, locale);
        String defaultValue = "0" + dayUnit;

        if (Objects.isNull(value)) {
            return defaultValue;
        }

        if (value.compareTo(BigDecimal.ZERO) > 0) {
            StringBuilder builder = new StringBuilder();

            // 넘어온 value에서 일 분리
            int days = value.intValue();

            // 넘어온 value에서 시간 분리
            int hours = value.remainder(BigDecimal.valueOf(1.0000))
                    .divide(BigDecimal.valueOf(0.1250), 0, RoundingMode.DOWN).intValue();

            // 넘어온 value에서 분 분리
            int minutes = value.remainder(BigDecimal.valueOf(1.0000))
                    .remainder(BigDecimal.valueOf(0.1250))
                    .divide(BigDecimal.valueOf(0.0625), 0, RoundingMode.DOWN).intValue() * 30;

            if (days > 0) {
                builder.append(days).append(dayUnit);
            }
            if (hours > 0) {
                if (!builder.isEmpty()) builder.append(" ");
                builder.append(hours).append(hourUnit);
            }
            if (minutes > 0) {
                if (!builder.isEmpty()) builder.append(" ");
                builder.append(minutes).append(minuteUnit);
            }

            return builder.toString();
        }

        return defaultValue;
    }
}
