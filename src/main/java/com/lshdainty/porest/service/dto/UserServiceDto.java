package com.lshdainty.porest.service.dto;

import com.lshdainty.porest.type.CompanyType;
import com.lshdainty.porest.type.DepartmentType;
import com.lshdainty.porest.type.RoleType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class UserServiceDto {
    private String id;
    private String pwd;
    private String name;
    private String email;
    private RoleType role;
    private String birth;
    private String workTime;
    private CompanyType company;
    private DepartmentType department;
    private String lunarYN;

    private String profileName;
    private String profileUrl;
    private String profileUUID;
}
