package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum GrantStatus implements DisplayType {
    // 아래 4개의 경우 사용자가 부여받은 휴가의 상태 타입
    /**
     * 활성<br>
     * 사용자가 휴가를 신청한 뒤 모든 승인권자가 승인을 한 뒤의 상태<br>
     * 관리자 부여 혹은 반복 부여 시에는 db에 insert할때의 상태<br>
     * 휴가가 활성 상태여야 사용자는 해당 휴가를 사용할 수 있음
     */
    ACTIVE(1L),
    /**
     * 소진<br>
     * 사용자가 부여받은 GrantTime을 모두 사용한 상태<br>
     * 사용자가 휴가를 사용시 GrantTime이 0이 되면 db에 해당 값으로 update
     */
    EXHAUSTED(2L),
    /**
     * 만료<br>
     * 부가 부여 테이블에서 유효기간이 지난 휴가의 상태<br>
     * 사용자가 해당 휴가를 모두 사용하던 안하던 유효기간이 지나면<br>
     * 해당 상태로 update함 (GrantTime은 0으로 변경하지 않음)
     */
    EXPIRED(3L),
    /**
     * 회수<br>
     * 관리자가 휴가를 잘 못 부여해서 회수를 할 때 해당 상태로 update<br>
     * 사용자에게 부여한 휴가 정책이 사라지는 경우도 강제로 해당 상태로 update<br>
     */
    REVOKED(4L),

    // 아래의 경우 휴가를 부여받기 전 승인 진행 중일 때의 부여 상태 타입
    /**
     * 대기<br>
     * 사용자가 본인에게 부여된 휴가 정책을 통해 휴가를 신청하면 해당 값으로 insert<br>
     * 승인권자가 한명도 승인, 거부를 하지 않았다면 해당 상태로 남아있음
     */
    PENDING(5L),
    /**
     * 진행<br>
     * 승인권자의 수가 2명 이상이고 1명의 승인권자가 승인을 한 경우 해당 값으로 update<br>
     * 만약 승인권자가 1명일 경우 해당 상태 없이 바로 활성 혹은 거부로 값이 update
     */
    PROGRESS(6L),
    /**
     * 반려<br>
     * 승인권자가 모두 거부를 한 경우 해당 값으로 update<br>
     * 해당 값일때는 휴가를 사용할 수 없음
     */
    REJECTED(7L),
    /**
     * 취소<br>
     * 신청자가 신청한 휴가를 취소하면 해당 값으로 update<br>
     * 해당 값은 한 명도 승인을 안한 경우에만 가능
     */
    CANCELED(8L);

    private static final String MESSAGE_KEY_PREFIX = "type.grant.status.";
    private Long orderSeq;

    GrantStatus(Long orderSeq) {
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
