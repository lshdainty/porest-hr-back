package com.porest.hr.common.type;

/**
 * 기본 회사 타입
 * 공통으로 사용되는 기본 회사 타입을 정의합니다.
 * SYSTEM: 시스템 사용자 구분용 (조회 시 제외 조건 등)
 */
public enum DefaultCompanyType implements CompanyType {
    NONE(0L),
    SYSTEM(999L);

    private static final String MESSAGE_KEY_PREFIX = "type.company.";
    private final Long orderSeq;

    DefaultCompanyType(Long orderSeq) {
        this.orderSeq = orderSeq;
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY_PREFIX + this.name().toLowerCase();
    }

    @Override
    public Long getOrderSeq() {
        return orderSeq;
    }
}
