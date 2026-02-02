package com.porest.hr.common.type;

/**
 * 기본 시스템 타입
 * 공통으로 사용되는 기본 시스템 타입을 정의합니다.
 * ETC: 기타 시스템 분류용
 */
public enum DefaultSystemType implements SystemType {
    ETC(999L);

    private static final String MESSAGE_KEY_PREFIX = "type.system.";
    private final Long orderSeq;

    DefaultSystemType(Long orderSeq) {
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
