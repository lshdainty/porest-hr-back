package com.lshdainty.myhr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.domain.VacationTimeType;
import com.lshdainty.myhr.domain.VacationType;
import com.lshdainty.myhr.service.dto.VacationServiceDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VacationDto {
    private Long vacationId;
    private VacationType vacationType;
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

    public VacationDto(Long vacationId) {
        this.vacationId = vacationId;
    }

    public VacationDto(Vacation vacation) {
        vacationId = vacation.getId();
        vacationType = vacation.getType();
        grantTime = vacation.getRemainTime();
        occurDate = vacation.getOccurDate();
        expiryDate = vacation.getExpiryDate();
    }

    public VacationDto(VacationServiceDto vacation) {
        vacationId = vacation.getId();
        vacationDesc = vacation.getDesc();
        vacationType = vacation.getType();
        grantTime = vacation.getGrantTime();
        remainTime = vacation.getRemainTime();
        occurDate = vacation.getOccurDate();
        expiryDate = vacation.getExpiryDate();
    }
}
