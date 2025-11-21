package com.lshdainty.porest.work.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.work.type.CodeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class WorkHistoryApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreateWorkHistoryReq {
        private LocalDate workDate;
        private String workUserId;
        private String workGroupCode;
        private String workPartCode;
        private String workClassCode;
        private BigDecimal workHour;
        private String workContent;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreateWorkHistoryResp {
        private Long workHistorySeq;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BulkCreateWorkHistoryReq {
        private List<CreateWorkHistoryReq> workHistories;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BulkCreateWorkHistoryResp {
        private List<Long> workHistorySeqs;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UpdateWorkHistoryReq {
        private LocalDate workDate;
        private String workUserId;
        private String workGroupCode;
        private String workPartCode;
        private String workClassCode;
        private BigDecimal workHour;
        private String workContent;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WorkHistoryResp {
        private Long workHistorySeq;
        private LocalDate workDate;
        private String workUserId;
        private String workUserName;
        private WorkCodeResp workGroup;
        private WorkCodeResp workPart;
        private WorkCodeResp workClass;
        private BigDecimal workHour;
        private String workContent;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WorkCodeResp {
        private Long workCodeSeq;
        private String workCode;
        private String workCodeName;
        private CodeType codeType;
        private Integer orderSeq;
        private Long parentWorkCodeSeq;
    }
}
