package com.lshdainty.porest.vacation.service.dto;

import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.GrantTiming;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import com.lshdainty.porest.vacation.type.VacationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
    private GrantTiming grantTiming;
    private Integer specificMonths;
    private Integer specificDays;
}
