package com.lshdainty.porest.security.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.type.RoleType;
import com.lshdainty.porest.user.type.StatusType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

public class AuthApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class LoginUserInfo {
        private String userId;
        private String userName;
        private String userEmail;
        private RoleType userRoleType;
        private String userRoleName;
        private YNType isLogin;
        private String profileUrl;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EncodePasswordReq {
        private String userPwd;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EncodePasswordResp {
        private String originalPw;
        private String encodedPw;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ValidateInvitationResp {
        private String userId;
        private String userName;
        private String userEmail;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private RoleType userRoleType;
        private LocalDateTime invitationSentAt;
        private LocalDateTime invitationExpiresAt;
        private StatusType invitationStatus;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CompleteInvitationReq {
        private String invitationToken;
        private String userBirth;
        private YNType lunarYn;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CompleteInvitationResp {
        private String userId;
    }
}
