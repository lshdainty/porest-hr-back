package com.lshdainty.porest.work.service.dto;

import com.lshdainty.porest.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@Builder
public class WorkHistoryServiceDto {
    private Long id;
    private LocalDate date;
    private User user;
    private String userId;
    private String groupCode;
    private String partCode;
    private String classCode;
    private BigDecimal hours;
    private String content;

    // 조회용 필드
    private String userName;
    private String groupName;
    private String partName;
    private String className;

    // WorkCode 전체 정보 조회용 필드
    private WorkCodeServiceDto groupInfo;
    private WorkCodeServiceDto partInfo;
    private WorkCodeServiceDto classInfo;
}
