package com.lshdainty.porest.user.service.dto;

import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.type.RoleType;
import com.lshdainty.porest.common.type.YNType;
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
    private OriginCompanyType company;
    private YNType lunarYN;

    private String profileName;
    private String profileUrl;
    private String profileUUID;

    // 초대 관련 필드
    private String invitationToken;
}
