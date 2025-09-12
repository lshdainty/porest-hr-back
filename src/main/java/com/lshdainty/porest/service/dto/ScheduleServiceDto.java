package com.lshdainty.porest.service.dto;

import com.lshdainty.porest.type.ScheduleType;
import com.lshdainty.porest.domain.User;
import com.lshdainty.porest.domain.Vacation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class ScheduleServiceDto {
    private Long id;
    private String userId;
    private ScheduleType type;
    private String desc;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String delYN;
    private BigDecimal realUsedTime;

    private User user;
    private Vacation vacation;

    @Override
    public String toString() {
        return "ScheduleServiceDto{" +
                "id: " + id +
                ", type: " + type +
                ", desc: '" + desc + '\'' +
                ", startDate: " + startDate +
                ", endDate: " + endDate +
                ", delYN: '" + delYN + '\'' +
                ", realUsedTime: " + realUsedTime +
                '}';
    }
}