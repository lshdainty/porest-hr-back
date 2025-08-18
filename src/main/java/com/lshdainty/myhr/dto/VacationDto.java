package com.lshdainty.myhr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.domain.VacationTimeType;
import com.lshdainty.myhr.domain.VacationType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VacationDto {
    private Long vacationId;
    private VacationType vacationType;
    private String vacationTypeName;
    private BigDecimal remainTime;
    private LocalDateTime occurDate;
    private LocalDateTime expiryDate;
    private String userId;
    private String userName;

    private String vacationDesc;
    private String delYN;
    // 휴가 추가
    private BigDecimal grantTime;

    // 휴가 사용
    private List<Long> vacationHistoryIds;
    private Long vacationHistoryId;
    private BigDecimal usedDateTime;
    private VacationTimeType vacationTimeType;
    private String vacationTimeTypeName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 사용자 월별 휴가 통계
    private int month;
    private String usedDateTimeStr;
}
