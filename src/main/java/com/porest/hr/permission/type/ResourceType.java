package com.porest.hr.permission.type;

import lombok.Getter;

/**
 * Resource Type Enum<br>
 * 권한이 적용되는 리소스 타입
 */
@Getter
public enum ResourceType {
    USER("사용자"),
    VACATION("휴가"),
    WORK("업무"),
    SCHEDULE("일정"),
    COMPANY("회사/부서"),
    HOLIDAY("공휴일"),
    DUES("회비"),
    REGULATION("사규"),
    NOTICE("공지사항"),
    ROLE("권한");

    private final String description;

    ResourceType(String description) {
        this.description = description;
    }
}
