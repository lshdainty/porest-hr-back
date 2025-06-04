package com.lshdainty.myhr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationTimeType;
import com.lshdainty.myhr.domain.VacationType;
import com.lshdainty.myhr.service.dto.VacationServiceDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Long userNo;
    private String userName;

    private String vacationDesc;
    private String delYN;
    // 휴가 추가
    private BigDecimal grantTime;

    // 휴가 사용
    private BigDecimal usedDateTime;
    private VacationTimeType vacationTimeType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
