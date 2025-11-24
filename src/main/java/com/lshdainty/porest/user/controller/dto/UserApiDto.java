package com.lshdainty.porest.user.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.type.StatusType;
import com.lshdainty.porest.user.type.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class JoinUserReq {
        private String userId;
        private String userPwd;
        private String userName;
        private String userEmail;
        private LocalDate userBirth;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private YNType lunarYn;
        private String profileUrl;
        private String profileUuid;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class JoinUserResp {
        private String userId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchUserResp {
        private String userId;
        private String userName;
        private String userEmail;
        private LocalDate userBirth;
        private String userWorkTime;
        private LocalDate joinDate;
        private RoleType userRoleType;
        private String userRoleName;
        private OriginCompanyType userOriginCompanyType;
        private String userOriginCompanyName;
        private YNType lunarYn;
        private String profileUrl;
        private String invitationToken;
        private LocalDateTime invitationSentAt;
        private LocalDateTime invitationExpiresAt;
        private StatusType invitationStatus;
        private LocalDateTime registeredAt;

        private String mainDepartmentNameKr;
        private String dashboard;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EditUserReq {
        private String userName;
        private String userEmail;
        private LocalDate userBirth;
        private RoleType userRoleType;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private YNType lunarYn;
        private String profileUrl;

        private String profileUuid;
        private String dashboard;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EditUserResp {
        private String userId;
        private String userName;
        private String userEmail;
        private LocalDate userBirth;
        private String userWorkTime;
        private RoleType userRoleType;
        private String userRoleName;
        private OriginCompanyType userOriginCompanyType;
        private String userOriginCompanyName;
        private YNType lunarYn;

        private String profileUrl;
        private String dashboard;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UploadProfileReq {
        private MultipartFile profile;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UploadProfileResp {
        private String profileUrl;
        private String profileUuid;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class InviteUserReq {
        private String userId;
        private String userName;
        private String userEmail;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private LocalDate joinDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class InviteUserResp {
        private String userId;
        private String userName;
        private String userEmail;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private LocalDate joinDate;
        private RoleType userRoleType;
        private LocalDateTime invitationSentAt;
        private LocalDateTime invitationExpiresAt;
        private StatusType invitationStatus;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ResendInvitationResp {
        private String userId;
        private String userName;
        private String userEmail;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private LocalDate joinDate;
        private RoleType userRoleType;
        private LocalDateTime invitationSentAt;
        private LocalDateTime invitationExpiresAt;
        private StatusType invitationStatus;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EditInvitedUserReq {
        private String userName;
        private String userEmail;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private LocalDate joinDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EditInvitedUserResp {
        private String userId;
        private String userName;
        private String userEmail;
        private OriginCompanyType userOriginCompanyType;
        private String userWorkTime;
        private LocalDate joinDate;
        private RoleType userRoleType;
        private LocalDateTime invitationSentAt;
        private LocalDateTime invitationExpiresAt;
        private StatusType invitationStatus;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CheckUserIdDuplicateResp {
        private boolean duplicate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CheckMainDepartmentExistenceResp {
        private YNType hasMainDepartment;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetApproversResp {
        private String userId;
        private String userName;
        private String userEmail;
        private RoleType userRoleType;
        private String userRoleName;
        private Long departmentId;
        private String departmentName;
        private String departmentNameKr;
        private Long departmentLevel;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UpdateDashboardReq {
        private String dashboard;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UpdateDashboardResp {
        private String userId;
        private String dashboard;
    }
}