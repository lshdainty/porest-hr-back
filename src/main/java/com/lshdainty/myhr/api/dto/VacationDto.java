package com.lshdainty.myhr.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.type.VacationTimeType;
import com.lshdainty.myhr.type.VacationType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VacationDto {
    // 휴가 아이디
    private Long vacationId;
    // 휴가 타입
    private VacationType vacationType;
    // 휴가 타입명
    private String vacationTypeName;
    // 휴가 잔여 시간
    private BigDecimal remainTime;
    // 휴가 발생 시간
    private LocalDateTime occurDate;
    // 휴가 소멸 시간
    private LocalDateTime expiryDate;
    // 사용자 아이디
    private String userId;
    // 사용자명
    private String userName;

    // 휴가 사유
    private String vacationDesc;
    // 휴가 이력 삭제 여부
    private String delYN;
    // 휴가 추가
    private BigDecimal grantTime;

    // 휴가 사용
    // 휴가에 속한 히스토리 아이디 리스트
    private List<Long> vacationHistoryIds;
    // 휴가에 속한 히스토리 아이디
    private Long vacationHistoryId;
    // 휴가 시간 타입
    private VacationTimeType vacationTimeType;
    // 휴가 시간 타입명
    private String vacationTimeTypeName;
    // 휴가 사용 시작 시간
    private LocalDateTime startDate;
    // 휴가 사용 종료 시간
    private LocalDateTime endDate;

    // 사용자 월별 휴가 통계
    // 월별 통계 월
    private Integer month;
    // 휴가 사용 시간
    private BigDecimal usedTime;
    // 휴가 사용 시간 문자열 (1일 1시간)
    private String usedTimeStr;

    // 사용 가능 휴가
    // 휴가 잔여 시간 문자열 (1일 1시간)
    private String remainTimeStr;

    // 휴가 통계
    // 휴가 사용 예정 시간
    private BigDecimal expectUsedTime;
    // 휴가 사용 예정 시간 문자열 (1일 1시간)
    private String expectUsedTimeStr;
    // 전달 휴가 잔여 시간
    private BigDecimal prevRemainTime;
    // 전달 휴가 잔여 시간 문자열 (1일 1시간)
    private String prevRemainTimeStr;
    // 전달 휴가 사용 시간
    private BigDecimal prevUsedTime;
    // 전달 휴가 사용 시간 문자열 (1일 1시간)
    private String prevUsedTimeStr;
    // 전달 휴가 사용 예정 시간
    private BigDecimal prevExpectUsedTime;
    // 전달 휴가 사용 예정 시간 문자열 (1일 1시간)
    private String prevExpectUsedTimeStr;
    // 전달 - 현재 잔여 차이
    private BigDecimal remainTimeGap;
    // 전달 - 현재 잔여 차이 문자열 (1일 1시간)
    private String remainTimeGapStr;
    // 전달 - 현재 사용 차이
    private BigDecimal usedTimeGap;
    // 전달 - 현재 사용 차이 문자열 (1일 1시간)
    private String usedTimeGapStr;
}
