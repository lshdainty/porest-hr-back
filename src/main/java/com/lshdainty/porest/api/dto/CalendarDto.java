package com.lshdainty.porest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalendarDto {
    private String userId;
    private String userName;
    private String calendarName;    // vacation, schedule type name
    private String calendarType;    // vacation, schedule type
    private String calendarDesc;    // vacation, schedule desc
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String domainType;      // vacation or schedule
    private List<Long> historyIds;  // vacation history ids>
    private Long scheduleId;
}
