package com.lshdainty.porest.permission.type;

import lombok.Getter;

/**
 * Action Type Enum<br>
 * 리소스에 대해 수행할 수 있는 작업 타입
 */
@Getter
public enum ActionType {
    READ("조회"),
    EDIT("수정"),
    WRITE("작성"),
    REQUEST("신청"),
    APPROVE("승인"),
    GRANT("부여"),
    MANAGE("관리");

    private final String description;

    ActionType(String description) {
        this.description = description;
    }
}
