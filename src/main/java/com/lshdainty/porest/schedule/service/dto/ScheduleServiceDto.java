package com.lshdainty.porest.schedule.service.dto;

import com.lshdainty.porest.schedule.type.ScheduleType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.Vacation;
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
    private Vacation vacation;

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