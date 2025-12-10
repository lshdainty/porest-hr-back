package com.lshdainty.porest.vacation.service.policy.description;

import com.lshdainty.porest.vacation.domain.VacationPolicy;

/**
 * 반복 부여 정책 설명 생성 인터페이스
 * 각 언어별로 구현체를 제공하여 다국어 지원
 */
public interface RepeatGrantDescriptionGenerator {
    /**
     * 반복 부여 정책을 해당 언어로 설명하는 문자열 생성
     *
     * @param policy 휴가 정책
     * @return 해당 언어로 된 설명 문자열
     */
    String generate(VacationPolicy policy);
}
