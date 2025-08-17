package com.lshdainty.myhr.service.dto;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.domain.VacationTimeType;
import com.lshdainty.myhr.domain.VacationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
public class VacationServiceDto {
    private Long id;
    private String userId;
    private String desc;
    private VacationType type;
    private BigDecimal remainTime;
    private LocalDateTime occurDate;
    private LocalDateTime expiryDate;
    private String delYN;

    private User user;

    private List<Long> historyIds;
    private Long historyId;

    private BigDecimal grantTime;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VacationTimeType timeType;

    @Override
    public String toString() {
        return "VacationServiceDto{" +
                "id: " + id +
                ", desc: '" + desc + '\'' +
                ", type: " + type +
                ", remainTime: " + remainTime +
                ", occurDate: " + occurDate +
                ", expiryDate: " + expiryDate +
                ", historyIds: " + historyIds +
                ", grantTime: " + grantTime +
                ", startDate: " + startDate +
                ", endDate: " + endDate +
                ", timeType: " + timeType +
                ", delYN: '" + delYN + '\'' +
                '}';
    }
}
