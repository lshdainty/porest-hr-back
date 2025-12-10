package com.lshdainty.porest.vacation.service.policy.description;

import com.lshdainty.porest.vacation.domain.VacationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 반복 부여 정책 설명 생성 팩토리
 * 현재 Locale에 따라 적절한 언어의 설명 생성기를 선택하여 설명 문자열을 생성
 */
@Component
@RequiredArgsConstructor
public class RepeatGrantDescriptionFactory {
    private final KoreanRepeatGrantDescriptionGenerator koreanGenerator;
    private final EnglishRepeatGrantDescriptionGenerator englishGenerator;

    /**
     * 현재 Locale에 맞는 반복 부여 정책 설명 생성
     *
     * @param policy 휴가 정책
     * @return 현재 Locale에 맞는 설명 문자열
     */
    public String generate(VacationPolicy policy) {
        Locale locale = LocaleContextHolder.getLocale();
        return getGenerator(locale).generate(policy);
    }

    /**
     * 특정 Locale에 맞는 반복 부여 정책 설명 생성
     *
     * @param policy 휴가 정책
     * @param locale 언어 설정
     * @return 해당 Locale에 맞는 설명 문자열
     */
    public String generate(VacationPolicy policy, Locale locale) {
        return getGenerator(locale).generate(policy);
    }

    private RepeatGrantDescriptionGenerator getGenerator(Locale locale) {
        if (locale == null) {
            return koreanGenerator;
        }

        String language = locale.getLanguage();
        switch (language) {
            case "ko":
                return koreanGenerator;
            case "en":
                return englishGenerator;
            default:
                // 기본값은 영어
                return englishGenerator;
        }
    }
}
