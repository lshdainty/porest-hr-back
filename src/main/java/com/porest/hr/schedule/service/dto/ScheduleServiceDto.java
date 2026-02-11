package com.porest.hr.schedule.service.dto;

import com.porest.hr.schedule.type.ScheduleType;
import com.porest.hr.user.domain.User;
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
    private String isDeleted;
    private BigDecimal realUsedTime;

    private User user;

    @Override
    public String toString() {
        return "ScheduleServiceDto{" +
                "id: " + id +
                ", type: " + type +
                ", desc: '" + desc + '\'' +
                ", startDate: " + startDate +
                ", endDate: " + endDate +
                ", isDeleted: '" + isDeleted + '\'' +
                ", realUsedTime: " + realUsedTime +
                '}';
    }
}