package com.lshdainty.porest.user.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.type.RoleType;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.controller.dto.VacationDto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter @Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private String userId;
    private String userPwd;
    private String userName;
    private String userEmail;
    private String userBirth;
    private String userWorkTime;
    private RoleType userRoleType;
    private OriginCompanyType userOriginCompanyType;
    private YNType lunarYN;
    private YNType delYN;
    private YNType isLogin;

    private List<VacationDto> vacations;

    private String userCompanyName;
    private String userDepartmentName;
    private String userRoleName;

    private MultipartFile profile;
    private String profileUrl;
    private String profileUUID;

    // 비밀번호 인코딩용 필드
    private String originalPW;
    private String encodedPW;

    // 초대 관련 필드
    private String invitationToken;
}
