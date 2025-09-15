package com.lshdainty.porest.service.dto;

import com.lshdainty.porest.type.vacation.GrantMethod;
import com.lshdainty.porest.type.vacation.GrantTiming;
import com.lshdainty.porest.type.vacation.RepeatUnit;
import com.lshdainty.porest.type.vacation.VacationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@Builder
public class VacationPolicyServiceDto {
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
