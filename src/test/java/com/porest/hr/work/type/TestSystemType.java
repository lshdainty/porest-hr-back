package com.porest.hr.work.type;

import com.porest.hr.common.type.SystemType;

/**
 * 테스트 전용 SystemType 구현체
 * 실제 OriginSystemType(porest-back-skc)을 대체하여 테스트에서 사용
 */
public enum TestSystemType implements SystemType {
    ERP(1L),
    MES(2L),
    WMS(3L),
    CRM(4L);

    private static final String MESSAGE_KEY_PREFIX = "type.system.type.";
    private final Long orderSeq;

    TestSystemType(Long orderSeq) {
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
