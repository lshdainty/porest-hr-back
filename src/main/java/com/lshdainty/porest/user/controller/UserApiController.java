package com.lshdainty.porest.user.controller;

import com.lshdainty.porest.security.principal.UserPrincipal;
import com.lshdainty.porest.security.service.CustomUserDetailsService;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.controller.dto.UserDto;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserApiController {
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/api/v1/user")
    public ApiResponse join(@RequestBody UserDto data) {
        String userId = userService.join(UserServiceDto.builder()
                .id(data.getUserId())
                .pwd(data.getUserPwd())
                .name(data.getUserName())
                .email(data.getUserEmail())
                .birth(data.getUserBirth())
                .company(data.getUserOriginCompanyType())
                .workTime(data.getUserWorkTime())
                .lunarYN(data.getLunarYN())
                .profileUrl(data.getProfileUrl())
                .profileUUID(data.getProfileUUID())
                .build()
        );

        return ApiResponse.success(UserDto.builder().userId(userId).build());
    }

    @GetMapping("/api/v1/user/{id}")
    public ApiResponse user(@PathVariable("id") String userId) {
        UserServiceDto user = userService.findUser(userId);

        return ApiResponse.success(UserDto.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userBirth(user.getBirth())
                .userWorkTime(user.getWorkTime())
                .userRoleType(user.getRole())
                .userRoleName(user.getRole().name())
                .userOriginCompanyType(user.getCompany())
                .userCompanyName(user.getCompany().getCompanyName())
                .lunarYN(user.getLunarYN())
                .profileUrl(user.getProfileUrl())
                .invitationToken(user.getInvitationToken())
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .registeredAt(user.getRegisteredAt())
                .build()
        );
    }

    @GetMapping("/api/v1/users")
    public ApiResponse users() {
        List<UserServiceDto> users = userService.findUsers();

        List<UserDto> resps = users.stream()
                .map(u -> UserDto.builder()
                        .userId(u.getId())
                        .userName(u.getName())
                        .userEmail(u.getEmail())
                        .userBirth(u.getBirth())
                        .userWorkTime(u.getWorkTime())
                        .userRoleType(u.getRole())
                        .userRoleName(u.getRole().name())
                        .userOriginCompanyType(u.getCompany())
                        .userCompanyName(u.getCompany().getCompanyName())
                        .lunarYN(u.getLunarYN())
                        .profileUrl(u.getProfileUrl())
                        .invitationToken(u.getInvitationToken())
                        .invitationSentAt(u.getInvitationSentAt())
                        .invitationExpiresAt(u.getInvitationExpiresAt())
                        .invitationStatus(u.getInvitationStatus())
                        .registeredAt(u.getRegisteredAt())
                        .build()
                )
                .collect(Collectors.toList());

        return ApiResponse.success(resps);
    }

    @PutMapping("/api/v1/user/{id}")
    public ApiResponse editUser(@PathVariable("id") String userId, @RequestBody UserDto data) {
        userService.editUser(UserServiceDto.builder()
                .id(userId)
                .name(data.getUserName())
                .email(data.getUserEmail())
                .birth(data.getUserBirth())
                .role(data.getUserRoleType())
                .company(data.getUserOriginCompanyType())
                .workTime(data.getUserWorkTime())
                .lunarYN(data.getLunarYN())
                .profileUrl(data.getProfileUrl())
                .profileUUID(data.getProfileUUID())
                .build()
        );

        UserServiceDto findUser = userService.findUser(userId);

        return ApiResponse.success(UserDto.builder()
                .userId(findUser.getId())
                .userName(findUser.getName())
                .userEmail(findUser.getEmail())
                .userBirth(findUser.getBirth())
                .userWorkTime(findUser.getWorkTime())
                .userRoleType(findUser.getRole())
                .userRoleName(findUser.getRole().name())
                .userOriginCompanyType(findUser.getCompany())
                .userCompanyName(findUser.getCompany().getCompanyName())
                .lunarYN(findUser.getLunarYN())
                .profileUrl(findUser.getProfileUrl())
                .build()
        );
    }

    @DeleteMapping("/api/v1/user/{id}")
    public ApiResponse deleteUser(@PathVariable("id") String userId) {
        userService.deleteUser(userId);
        return ApiResponse.success();
    }

    @PostMapping(value = "/api/v1/user/upload/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse uploadProfile(@ModelAttribute UserDto data) {
        UserServiceDto dto = userService.saveProfileImgInTempFolder(data.getProfile());
        return ApiResponse.success(UserDto.builder()
                .profileUrl(dto.getProfileUrl())
                .profileUUID(dto.getProfileUUID())
                .build());
    }

    @GetMapping("/api/v1/login/user-info")
    public ApiResponse<UserDto> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        // UserPrincipal 인터페이스로 통합 처리
        if (!(principal instanceof UserPrincipal)) {
            throw new IllegalArgumentException("지원하지 않는 인증 타입입니다.");
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        User user = userPrincipal.getUser();

        UserDto result = UserDto.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userRoleType(user.getRole())
                .userRoleName(user.getRole().name())
                .isLogin(YNType.Y)
                .profileUrl(StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                        userService.generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null)
                .build();

        return ApiResponse.success(result);
    }

    /**
     * 비밀번호 인코딩 유틸리티 API (개발/테스트용)
     */
    @PostMapping("/encode-password")
    public ApiResponse<UserDto> encodePassword(@RequestBody UserDto data) {
        log.info("Password encoding request for user: {}", data.getUserId());

        String encodedPassword = passwordEncoder.encode(data.getUserPwd());

        UserDto result = UserDto.builder()
                .originalPW(data.getUserPwd())
                .encodedPW(encodedPassword)
                .build();

        return ApiResponse.success(result);
    }

    /**
     * 관리자가 사용자 초대
     */
    @PostMapping("/api/v1/user/invite")
    public ApiResponse inviteUser(@RequestBody UserDto data) {
        UserServiceDto result = userService.inviteUser(UserServiceDto.builder()
                .id(data.getUserId())
                .name(data.getUserName())
                .email(data.getUserEmail())
                .company(data.getUserOriginCompanyType())
                .workTime(data.getUserWorkTime())
                .build()
        );

        return ApiResponse.success(UserDto.builder()
                .userId(result.getId())
                .userName(result.getName())
                .userEmail(result.getEmail())
                .userOriginCompanyType(result.getCompany())
                .userWorkTime(result.getWorkTime())
                .userRoleType(result.getRole())
                .invitationSentAt(result.getInvitationSentAt())
                .invitationExpiresAt(result.getInvitationExpiresAt())
                .invitationStatus(result.getInvitationStatus())
                .build());
    }

    /**
     * 초대 이메일 재전송
     */
    @PostMapping("/api/v1/user/invitation/resend/{id}")
    public ApiResponse resendInvitation(@PathVariable("id") String userId) {
        UserServiceDto result = userService.resendInvitation(userId);

        return ApiResponse.success(UserDto.builder()
                .userId(result.getId())
                .userName(result.getName())
                .userEmail(result.getEmail())
                .userOriginCompanyType(result.getCompany())
                .userWorkTime(result.getWorkTime())
                .userRoleType(result.getRole())
                .invitationSentAt(result.getInvitationSentAt())
                .invitationExpiresAt(result.getInvitationExpiresAt())
                .invitationStatus(result.getInvitationStatus())
                .build());
    }
}
