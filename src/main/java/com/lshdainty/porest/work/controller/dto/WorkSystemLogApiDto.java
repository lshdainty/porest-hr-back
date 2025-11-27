package com.lshdainty.porest.work.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.work.type.SystemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class WorkSystemLogApiDto {

    /**
     * 시스템 체크 토글 요청 DTO
     */
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ToggleSystemCheckReq {
        private SystemType systemCode;
    }

    /**
     * 시스템 체크 토글 응답 DTO
     */
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ToggleSystemCheckResp {
        private boolean checked;  // true: 생성됨(체크됨), false: 삭제됨(체크 해제됨)
        private String message;
    }

    /**
     * 시스템 체크 상태 조회 응답 DTO
     */
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CheckSystemStatusResp {
        private SystemType systemCode;
        private boolean checked;  // true: 오늘 체크됨, false: 오늘 체크 안됨
    }
}
