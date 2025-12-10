package com.lshdainty.porest.user.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum StatusType implements DisplayType {
    PENDING(1L), // 초대 후 아직 회원가입하지 않은 상태
    ACTIVE(2L),  // 회원가입 완료한 상태
    INACTIVE(3L),   // 비활성화된 상태
    EXPIRED(4L); // 초대 링크가 만료된 상태

    private static final String MESSAGE_KEY_PREFIX = "type.user.status.";
    private Long orderSeq;

    StatusType(Long orderSeq) {
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
