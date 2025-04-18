package com.lshdainty.myhr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.domain.Schedule;
import com.lshdainty.myhr.domain.ScheduleType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleDto {
    private Long scheduleId;
    private Long userNo;
    private String userName;
    private Long vacationId;
    private ScheduleType scheduleType;
    private String scheduleTypeName;
    private String scheduleDesc;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public ScheduleDto(Long id) {
        this.scheduleId = id;
    }

    public ScheduleDto(Schedule schedule) {
        this.scheduleId = schedule.getId();
        this.userNo = schedule.getUser().getId();
        this.userName = schedule.getUser().getName();
        if (schedule.getType().isVacationType()) {
            this.vacationId = schedule.getVacation().getId();
        }
        this.scheduleType = schedule.getType();
        this.scheduleTypeName = schedule.getType().getTypeName();
        this.scheduleDesc = schedule.getDesc();
        this.startDate = schedule.getStartDate();
        this.endDate = schedule.getEndDate();
    }
}
