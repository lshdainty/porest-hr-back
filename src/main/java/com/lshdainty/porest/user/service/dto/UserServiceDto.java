package com.lshdainty.porest.user.service.dto;

import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.type.RoleType;
import com.lshdainty.porest.user.type.StatusType;
import com.lshdainty.porest.common.type.YNType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
public class UserServiceDto {
    private String id;
    private String pwd;
    private String name;
    private String email;
    private RoleType role;
    private LocalDate birth;
    private String workTime;
    private LocalDate joinDate; // 입사일
    private OriginCompanyType company;
    private YNType lunarYN;

    private String profileName;
    private String profileUrl;
    private String profileUUID;

    // 초대 관련 필드
    private String invitationToken;
    private LocalDateTime invitationSentAt; // 초대 토큰 생성 시간
    private LocalDateTime invitationExpiresAt; // 초대 토큰 만료 시간
    private StatusType invitationStatus; // 초대 상태
    private LocalDateTime registeredAt; // 회원가입 완료 시간

    // 메인 부서 정보
    private String mainDepartmentNameKR; // 메인 부서의 한글명

    // 승인권자 정보 (부서 정보 포함)
    private Long departmentId; // 부서 ID
    private String departmentName; // 부서명
    private String departmentNameKR; // 부서 한글명
    private Long departmentLevel; // 부서 레벨
}
