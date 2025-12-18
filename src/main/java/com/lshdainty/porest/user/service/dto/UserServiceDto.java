package com.lshdainty.porest.user.service.dto;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.CompanyType;
import com.lshdainty.porest.user.type.StatusType;
import com.lshdainty.porest.common.type.YNType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@Builder
public class UserServiceDto {
    private String id;
    private String pwd;
    private String name;
    private String email;
    private List<String> roleNames;
    private LocalDate birth;
    private String workTime;
    private LocalDate joinDate; // 입사일
    private CompanyType company;
    private YNType lunarYN;
    private CountryCode countryCode; // 국가 코드

    private String profileName;
    private String profileUrl;
    private String profileUUID;
    private String dashboard;

    // 초대 관련 필드
    private String invitationToken;
    private LocalDateTime invitationSentAt; // 초대 토큰 생성 시간
    private LocalDateTime invitationExpiresAt; // 초대 토큰 만료 시간
    private StatusType invitationStatus; // 초대 상태
    private LocalDateTime registeredAt; // 회원가입 완료 시간
    private YNType passwordChangeRequired; // 비밀번호 변경 필요 여부

    // 회원가입 완료 관련 필드 (세션 기반)
    private String newUserId; // 새로운 ID
    private String newPassword; // 새 비밀번호
    private String newPasswordConfirm; // 새 비밀번호 확인

    // 메인 부서 정보
    private String mainDepartmentNameKR; // 메인 부서의 한글명

    // 승인권자 정보 (부서 정보 포함)
    private Long departmentId; // 부서 ID
    private String departmentName; // 부서명
    private String departmentNameKR; // 부서 한글명
    private Long departmentLevel; // 부서 레벨

    // 역할 및 권한 상세 정보
    private List<RoleDetailDto> roles; // 역할 상세 정보 (역할 코드, 이름, 권한 목록)
    private List<String> allPermissions; // 모든 권한 코드 목록

    /**
     * 역할 상세 정보 DTO
     */
    @Getter
    @Builder
    public static class RoleDetailDto {
        private String roleCode;
        private String roleName;
        private List<PermissionDetailDto> permissions;
    }

    /**
     * 권한 상세 정보 DTO
     */
    @Getter
    @Builder
    public static class PermissionDetailDto {
        private String permissionCode;
        private String permissionName;
    }
}
