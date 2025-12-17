package com.lshdainty.porest.common.type;

/**
 * 기본 시스템 타입
 * 회사별 모듈이 없을 때 사용되는 기본 구현체
 */
public enum DefaultSystemType implements SystemType {
    ERP(1L),
    ETC(99L);

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
