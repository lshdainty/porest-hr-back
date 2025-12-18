package com.lshdainty.porest.user.controller;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.common.type.CompanyType;
import com.lshdainty.porest.user.controller.dto.UserApiDto;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserApiController implements UserApi {
    private final UserService userService;
    private final MessageSource messageSource;

    @Override
    public ApiResponse joinUser(UserApiDto.JoinUserReq data) {
        String userId = userService.joinUser(UserServiceDto.builder()
                .id(data.getUserId())
                .pwd(data.getUserPwd())
                .name(data.getUserName())
                .email(data.getUserEmail())
                .birth(data.getUserBirth())
                .company(data.getUserCompanyType())
                .workTime(data.getUserWorkTime())
                .lunarYN(data.getLunarYn())
                .profileUrl(data.getProfileUrl())
                .profileUUID(data.getProfileUuid())
                .countryCode(data.getCountryCode())
                .build()
        );

        return ApiResponse.success(new UserApiDto.JoinUserResp(userId));
    }

    @Override
    @PreAuthorize("hasAuthority('USER:READ')")
    public ApiResponse searchUser(String userId) {
        UserServiceDto user = userService.searchUser(userId);

        // 역할 상세 정보 변환 (null 체크)
        List<UserApiDto.RoleDetailResp> roleDetails = user.getRoles() != null
                ? user.getRoles().stream()
                    .map(role -> new UserApiDto.RoleDetailResp(
                            role.getRoleCode(),
                            role.getRoleName(),
                            role.getPermissions() != null
                                ? role.getPermissions().stream()
                                    .map(perm -> new UserApiDto.PermissionDetailResp(
                                            perm.getPermissionCode(),
                                            perm.getPermissionName()
                                    ))
                                    .collect(Collectors.toList())
                                : List.of()
                    ))
                    .collect(Collectors.toList())
                : List.of();

        // roleNames null 체크
        List<String> roleNames = user.getRoleNames() != null ? user.getRoleNames() : List.of();
        List<String> allPermissions = user.getAllPermissions() != null ? user.getAllPermissions() : List.of();

        return ApiResponse.success(new UserApiDto.SearchUserResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirth(),
                user.getWorkTime(),
                user.getJoinDate(),
                roleDetails,
                roleNames,
                roleNames.isEmpty() ? null : roleNames.get(0),
                allPermissions,
                user.getCompany(),
                getTranslatedName(user.getCompany()),
                user.getLunarYN(),
                user.getProfileUrl(),
                user.getInvitationToken(),
                user.getInvitationSentAt(),
                user.getInvitationExpiresAt(),
                user.getInvitationStatus(),
                user.getRegisteredAt(),
                user.getMainDepartmentNameKR(),
                user.getDashboard(),
                user.getCountryCode()
        ));
    }

    @Override
    public ApiResponse checkUserIdDuplicate(String userId) {
        boolean isDuplicate = userService.checkUserIdDuplicate(userId);
        return ApiResponse.success(new UserApiDto.CheckUserIdDuplicateResp(isDuplicate));
    }

    @Override
    @PreAuthorize("hasAuthority('USER:READ')")
    public ApiResponse searchUsers() {
        List<UserServiceDto> users = userService.searchUsers();

        List<UserApiDto.SearchUserResp> resps = users.stream()
                .map(u -> {
                    // 역할 상세 정보 변환 (null 체크)
                    List<UserApiDto.RoleDetailResp> roleDetails = u.getRoles() != null
                            ? u.getRoles().stream()
                                .map(role -> new UserApiDto.RoleDetailResp(
                                        role.getRoleCode(),
                                        role.getRoleName(),
                                        role.getPermissions() != null
                                            ? role.getPermissions().stream()
                                                .map(perm -> new UserApiDto.PermissionDetailResp(
                                                        perm.getPermissionCode(),
                                                        perm.getPermissionName()
                                                ))
                                                .collect(Collectors.toList())
                                            : List.of()
                                ))
                                .collect(Collectors.toList())
                            : List.of();

                    // roleNames null 체크
                    List<String> roleNames = u.getRoleNames() != null ? u.getRoleNames() : List.of();
                    List<String> allPermissions = u.getAllPermissions() != null ? u.getAllPermissions() : List.of();

                    return new UserApiDto.SearchUserResp(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getBirth(),
                            u.getWorkTime(),
                            u.getJoinDate(),
                            roleDetails,
                            roleNames,
                            roleNames.isEmpty() ? null : roleNames.get(0),
                            allPermissions,
                            u.getCompany(),
                            getTranslatedName(u.getCompany()),
                            u.getLunarYN(),
                            u.getProfileUrl(),
                            u.getInvitationToken(),
                            u.getInvitationSentAt(),
                            u.getInvitationExpiresAt(),
                            u.getInvitationStatus(),
                            u.getRegisteredAt(),
                            u.getMainDepartmentNameKR(),
                            u.getDashboard(),
                            u.getCountryCode()
                    );
                })
                .collect(Collectors.toList());

        return ApiResponse.success(resps);
    }

    @Override
    @PreAuthorize("hasAuthority('USER:EDIT')")
    public ApiResponse editUser(String userId, UserApiDto.EditUserReq data) {
        userService.editUser(UserServiceDto.builder()
                .id(userId)
                .name(data.getUserName())
                .email(data.getUserEmail())
                .birth(data.getUserBirth())
                .roleNames(data.getUserRoles())
                .company(data.getUserCompanyType())
                .workTime(data.getUserWorkTime())
                .lunarYN(data.getLunarYn())
                .profileUrl(data.getProfileUrl())
                .profileUUID(data.getProfileUuid())
                .dashboard(data.getDashboard())
                .countryCode(data.getCountryCode())
                .build()
        );

        UserServiceDto findUser = userService.searchUser(userId);

        // 역할 상세 정보 변환 (null 체크)
        List<UserApiDto.RoleDetailResp> roleDetails = findUser.getRoles() != null
                ? findUser.getRoles().stream()
                    .map(role -> new UserApiDto.RoleDetailResp(
                            role.getRoleCode(),
                            role.getRoleName(),
                            role.getPermissions() != null
                                ? role.getPermissions().stream()
                                    .map(perm -> new UserApiDto.PermissionDetailResp(
                                            perm.getPermissionCode(),
                                            perm.getPermissionName()
                                    ))
                                    .collect(Collectors.toList())
                                : List.of()
                    ))
                    .collect(Collectors.toList())
                : List.of();

        // roleNames null 체크
        List<String> roleNames = findUser.getRoleNames() != null ? findUser.getRoleNames() : List.of();
        List<String> allPermissions = findUser.getAllPermissions() != null ? findUser.getAllPermissions() : List.of();

        return ApiResponse.success(new UserApiDto.EditUserResp(
                findUser.getId(),
                findUser.getName(),
                findUser.getEmail(),
                findUser.getBirth(),
                findUser.getWorkTime(),
                roleDetails,
                roleNames,
                roleNames.isEmpty() ? null : roleNames.get(0),
                allPermissions,
                findUser.getCompany(),
                getTranslatedName(findUser.getCompany()),
                findUser.getLunarYN(),
                findUser.getProfileUrl(),
                findUser.getDashboard(),
                findUser.getCountryCode()
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('USER:MANAGE')")
    public ApiResponse deleteUser(String userId) {
        userService.deleteUser(userId);
        return ApiResponse.success();
    }

    @Override
    public ApiResponse uploadProfile(UserApiDto.UploadProfileReq data) {
        UserServiceDto dto = userService.saveProfileImgInTempFolder(data.getProfile());
        return ApiResponse.success(new UserApiDto.UploadProfileResp(
                dto.getProfileUrl(),
                dto.getProfileUUID()
        ));
    }

    /**
     * 관리자가 사용자 초대
     */
    @Override
    @PreAuthorize("hasAuthority('USER:MANAGE')")
    public ApiResponse inviteUser(UserApiDto.InviteUserReq data) {
        UserServiceDto result = userService.inviteUser(UserServiceDto.builder()
                .id(data.getUserId())
                .name(data.getUserName())
                .email(data.getUserEmail())
                .company(data.getUserCompanyType())
                .workTime(data.getUserWorkTime())
                .joinDate(data.getJoinDate())
                .countryCode(data.getCountryCode())
                .build()
        );

        return ApiResponse.success(new UserApiDto.InviteUserResp(
                result.getId(),
                result.getName(),
                result.getEmail(),
                result.getCompany(),
                result.getWorkTime(),
                result.getJoinDate(),
                result.getRoleNames(),
                result.getInvitationSentAt(),
                result.getInvitationExpiresAt(),
                result.getInvitationStatus(),
                result.getCountryCode()
        ));
    }

    /**
     * 초대된 사용자 정보 수정
     */
    @Override
    @PreAuthorize("hasAuthority('USER:MANAGE')")
    public ApiResponse editInvitedUser(String userId, UserApiDto.EditInvitedUserReq data) {
        UserServiceDto result = userService.editInvitedUser(userId, UserServiceDto.builder()
                .name(data.getUserName())
                .email(data.getUserEmail())
                .company(data.getUserCompanyType())
                .workTime(data.getUserWorkTime())
                .joinDate(data.getJoinDate())
                .countryCode(data.getCountryCode())
                .build()
        );

        return ApiResponse.success(new UserApiDto.EditInvitedUserResp(
                result.getId(),
                result.getName(),
                result.getEmail(),
                result.getCompany(),
                result.getWorkTime(),
                result.getJoinDate(),
                result.getRoleNames(),
                result.getInvitationSentAt(),
                result.getInvitationExpiresAt(),
                result.getInvitationStatus(),
                result.getCountryCode()
        ));
    }

    /**
     * 초대 이메일 재전송
     */
    @Override
    @PreAuthorize("hasAuthority('USER:MANAGE')")
    public ApiResponse resendInvitation(String userId) {
        UserServiceDto result = userService.resendInvitation(userId);

        return ApiResponse.success(new UserApiDto.ResendInvitationResp(
                result.getId(),
                result.getName(),
                result.getEmail(),
                result.getCompany(),
                result.getWorkTime(),
                result.getJoinDate(),
                result.getRoleNames(),
                result.getInvitationSentAt(),
                result.getInvitationExpiresAt(),
                result.getInvitationStatus(),
                result.getCountryCode()
        ));
    }

    /**
     * 사용자의 메인 부서 존재 여부 확인
     */
    @Override
    public ApiResponse checkUserMainDepartmentExistence(String userId) {
        YNType hasMainDepartment = userService.checkUserHasMainDepartment(userId);
        return ApiResponse.success(new UserApiDto.CheckMainDepartmentExistenceResp(hasMainDepartment));
    }

    /**
     * 유저 대시보드 수정
     * PATCH /api/v1/users/{userId}/dashboard
     */
    @Override
    @PreAuthorize("hasAuthority('USER:EDIT')")
    public ApiResponse updateDashboard(String userId, UserApiDto.UpdateDashboardReq data) {
        UserServiceDto result = userService.updateDashboard(userId, data.getDashboard());

        return ApiResponse.success(new UserApiDto.UpdateDashboardResp(
                result.getId(),
                result.getDashboard()
        ));
    }

    /**
     * 특정 유저의 승인권자 목록 조회
     * GET /api/v1/users/{userId}/approvers
     */
    @Override
    public ApiResponse getUserApprovers(String userId) {
        List<UserServiceDto> approvers = userService.getUserApprovers(userId);

        List<UserApiDto.ApproverDetailResp> approverDetails = approvers.stream()
                .map(approver -> {
                    // 역할 상세 정보 변환 (null 체크)
                    List<UserApiDto.RoleDetailResp> roleDetails = approver.getRoles() != null
                            ? approver.getRoles().stream()
                                .map(role -> new UserApiDto.RoleDetailResp(
                                        role.getRoleCode(),
                                        role.getRoleName(),
                                        role.getPermissions() != null
                                            ? role.getPermissions().stream()
                                                .map(perm -> new UserApiDto.PermissionDetailResp(
                                                        perm.getPermissionCode(),
                                                        perm.getPermissionName()
                                                ))
                                                .collect(Collectors.toList())
                                            : List.of()
                                ))
                                .collect(Collectors.toList())
                            : List.of();

                    // roleNames null 체크
                    List<String> roleNames = approver.getRoleNames() != null ? approver.getRoleNames() : List.of();
                    List<String> allPermissions = approver.getAllPermissions() != null ? approver.getAllPermissions() : List.of();

                    return new UserApiDto.ApproverDetailResp(
                            approver.getId(),
                            approver.getName(),
                            approver.getEmail(),
                            roleDetails,
                            roleNames,
                            roleNames.isEmpty() ? null : roleNames.get(0),
                            allPermissions,
                            approver.getDepartmentId(),
                            approver.getDepartmentName(),
                            approver.getDepartmentNameKR(),
                            approver.getDepartmentLevel()
                    );
                })
                .collect(Collectors.toList());

        int maxAvailableCount = approverDetails.size();
        boolean isAutoApproval = maxAvailableCount == 0;

        return ApiResponse.success(new UserApiDto.GetApproversResp(
                approverDetails,
                maxAvailableCount,
                isAutoApproval
        ));
    }

    /**
     * 관리자가 사용자 비밀번호 초기화
     * PATCH /api/v1/users/{userId}/password
     */
    @Override
    @PreAuthorize("hasAuthority('USER:MANAGE')")
    public ApiResponse resetPassword(String userId, UserApiDto.ResetPasswordReq data) {
        userService.resetPassword(userId, data.getNewPassword());
        return ApiResponse.success();
    }

    /**
     * 비밀번호 초기화 요청 (비로그인)
     * POST /api/v1/users/password/reset-request
     */
    @Override
    public ApiResponse requestPasswordReset(UserApiDto.RequestPasswordResetReq data) {
        userService.requestPasswordReset(data.getUserId(), data.getEmail());
        return ApiResponse.success();
    }

    /**
     * 본인 비밀번호 변경
     * PATCH /api/v1/users/me/password
     */
    @Override
    public ApiResponse changePassword(UserApiDto.ChangePasswordReq data) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        userService.changePassword(userId, data.getCurrentPassword(), data.getNewPassword(), data.getNewPasswordConfirm());
        return ApiResponse.success();
    }

    /**
     * 초대 확인 (회원가입 1단계)
     * POST /api/v1/users/registration/validate
     */
    @Override
    public ApiResponse<UserApiDto.ValidateRegistrationResp> validateRegistration(UserApiDto.ValidateRegistrationReq data, HttpSession session) {
        UserServiceDto dto = UserServiceDto.builder()
                .id(data.getUserId())
                .name(data.getUserName())
                .email(data.getUserEmail())
                .invitationToken(data.getInvitationCode())
                .build();

        boolean valid = userService.validateRegistration(dto);

        if (valid) {
            // 세션에 초대된 사용자 ID 저장
            session.setAttribute("invitedUserId", data.getUserId());
            session.setAttribute("registrationStep", "validated");
        }

        return ApiResponse.success(new UserApiDto.ValidateRegistrationResp(valid, "초대 확인이 완료되었습니다."));
    }

    /**
     * 회원가입 완료 (회원가입 2단계)
     * POST /api/v1/users/registration/complete
     */
    @Override
    public ApiResponse<UserApiDto.CompleteRegistrationResp> completeRegistration(UserApiDto.CompleteRegistrationReq data, HttpSession session) {
        // 세션에서 초대된 사용자 ID 확인
        String invitedUserId = (String) session.getAttribute("invitedUserId");
        String step = (String) session.getAttribute("registrationStep");

        if (invitedUserId == null || !"validated".equals(step)) {
            throw new com.lshdainty.porest.common.exception.UnauthorizedException(
                    com.lshdainty.porest.common.exception.ErrorCode.UNAUTHORIZED
            );
        }

        UserServiceDto dto = UserServiceDto.builder()
                .newUserId(data.getNewUserId())
                .email(data.getNewUserEmail())
                .newPassword(data.getPassword())
                .newPasswordConfirm(data.getPasswordConfirm())
                .birth(data.getUserBirth())
                .lunarYN(data.getLunarYn())
                .build();

        String newUserId = userService.completeRegistration(dto, invitedUserId);

        // 세션 정리
        session.removeAttribute("invitedUserId");
        session.removeAttribute("registrationStep");

        return ApiResponse.success(new UserApiDto.CompleteRegistrationResp(newUserId));
    }

    private String getTranslatedName(CompanyType type) {
        if (type == null) return null;
        return messageSource.getMessage(type.getMessageKey(), null, LocaleContextHolder.getLocale());
    }
}
