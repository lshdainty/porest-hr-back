package com.lshdainty.myhr.service.dto;

import com.lshdainty.myhr.domain.RoleType;
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
    private String employ;
    private String lunarYN;
}
