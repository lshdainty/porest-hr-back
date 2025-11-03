package com.lshdainty.porest.vacation.service.dto;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.type.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class VacationPolicyServiceDto {
    private Long userVacationPolicyId;
    private Long id;
    private String name;
    private String desc;
    private VacationType vacationType;
    private GrantMethod grantMethod;
    private BigDecimal grantTime;
    private RepeatUnit repeatUnit;
    private Integer repeatInterval;
    private Integer specificMonths;
    private Integer specificDays;
    private LocalDateTime firstGrantDate;
    private YNType isRecurring;
    private Integer maxGrantCount;
    private EffectiveType effectiveType;
    private ExpirationType expirationType;
    private Integer approvalRequiredCount;
}
