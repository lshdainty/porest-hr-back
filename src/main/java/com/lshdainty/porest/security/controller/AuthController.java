package com.lshdainty.porest.security.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.common.exception.UnauthorizedException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.security.controller.dto.AuthApiDto;
import com.lshdainty.porest.security.principal.UserPrincipal;
import com.lshdainty.porest.security.service.SecurityService;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final SecurityService securityService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("login/check")
    public ApiResponse<AuthApiDto.LoginUserInfo> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        // UserPrincipal 인터페이스로 통합 처리
        if (!(principal instanceof UserPrincipal)) {
            throw new UnauthorizedException("지원하지 않는 인증 타입입니다.");
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        User user = userPrincipal.getUser();

        /*
        * TODO : 프로파일 이미지를 추후에 변경하면 세션에 적용안됨
        *  수정 필요
        * */

        AuthApiDto.LoginUserInfo result = new AuthApiDto.LoginUserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getRole().name(),
                YNType.Y,
                StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                        userService.generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null
        );

        log.info("user info : {}, {}, {}, {}, {}, {}", result.getUserId(), result.getUserName(), result.getUserEmail(), result.getUserRoleType(), result.getUserRoleName(), result.getProfileUrl());

        return ApiResponse.success(result);
    }

    /**
     * 비밀번호 인코딩 유틸리티 API (개발/테스트용)
     */
    @PostMapping("/encode-password")
    public ApiResponse<AuthApiDto.EncodePasswordResp> encodePassword(@RequestBody AuthApiDto.EncodePasswordReq data) {
        log.info("Password encoding request");

        String encodedPassword = passwordEncoder.encode(data.getUserPwd());

        return ApiResponse.success(new AuthApiDto.EncodePasswordResp(
                data.getUserPwd(),
                encodedPassword
        ));
    }

    /**
     * 초대 토큰 유효성 검증
     */
    @GetMapping("/oauth2/signup/validate")
    public ApiResponse<AuthApiDto.ValidateInvitationResp> validateInvitationToken(@RequestParam("token") String token, HttpSession session) {
        // 토큰 빈값이면 토큰 없다고 에러 반환

        UserServiceDto user = securityService.validateInvitationToken(token);

        // ✅ 검증 성공 시 세션에 초대 토큰과 사용자 정보 저장
        session.setAttribute("invitationToken", token);
        session.setAttribute("oauthStep", "signup");
        session.setAttribute("invitedUserId", user.getId()); // 추가 보안

        return ApiResponse.success(new AuthApiDto.ValidateInvitationResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCompany(),
                user.getWorkTime(),
                user.getRole(),
                user.getInvitationSentAt(),
                user.getInvitationExpiresAt(),
                user.getInvitationStatus()
        ));
    }

    /**
     * 초대받은 사용자의 회원가입 완료
     */
    @PostMapping("/oauth2/signup/invitation/complete")
    public ApiResponse<AuthApiDto.CompleteInvitationResp> completeInvitedUserRegistration(@RequestBody AuthApiDto.CompleteInvitationReq data) {
        String userId = userService.completeInvitedUserRegistration(UserServiceDto.builder()
                .invitationToken(data.getInvitationToken())
                .birth(data.getUserBirth())
                .lunarYN(data.getLunarYn())
                .build()
        );

        return ApiResponse.success(new AuthApiDto.CompleteInvitationResp(userId));
    }
}
