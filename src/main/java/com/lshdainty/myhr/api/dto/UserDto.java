package com.lshdainty.myhr.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.type.CompanyType;
import com.lshdainty.myhr.type.DepartmentType;
import com.lshdainty.myhr.type.RoleType;
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
    private CompanyType userCompanyType;
    private DepartmentType userDepartmentType;
    private String lunarYN;
    private String delYN;

    private List<VacationDto> vacations;

    private String userCompanyName;
    private String userDepartmentName;
    private String userRoleName;

    private MultipartFile profile;
}
