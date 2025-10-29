package com.lshdainty.porest.vacation.service.dto;

import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.GrantTiming;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import com.lshdainty.porest.vacation.type.VacationType;
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
    private GrantTiming grantTiming;
    private Integer specificMonths;
    private Integer specificDays;
    private LocalDateTime firstGrantDate;  // 첫 부여 시점 (반복 부여 방식에서 필수)
}
