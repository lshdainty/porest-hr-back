package com.porest.hr.vacation.type;

import com.porest.core.type.DisplayType;

public enum ApprovalStatus implements DisplayType {
    PENDING(1L),
    APPROVED(2L),
    REJECTED(3L);

    private static final String MESSAGE_KEY_PREFIX = "type.approval.status.";
    private Long orderSeq;

    ApprovalStatus(Long orderSeq) {
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
