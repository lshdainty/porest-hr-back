package com.lshdainty.porest.security.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.security.service.SecurityService;
import com.lshdainty.porest.user.controller.dto.UserDto;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final SecurityService securityService;
    private final UserService userService;

    /**
     * 초대 토큰 유효성 검증
     */
    @GetMapping("/oauth2/signup/validate")
    public ApiResponse validateInvitationToken(@RequestParam("token") String token, HttpSession session) {
        // 토큰 빈값이면 토큰 없다고 에러 반환

        UserServiceDto user = securityService.validateInvitationToken(token);

        // ✅ 검증 성공 시 세션에 초대 토큰과 사용자 정보 저장
        session.setAttribute("invitationToken", token);
        session.setAttribute("oauthStep", "signup");
        session.setAttribute("invitedUserId", user.getId()); // 추가 보안

        return ApiResponse.success(UserDto.builder()
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userOriginCompanyType(user.getCompany())
                .userWorkTime(user.getWorkTime())
                .userRoleType(user.getRole())
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build());
    }

    /**
     * 초대받은 사용자의 회원가입 완료
     */
    @PostMapping("/oauth2/signup/invitation/complete")
    public ApiResponse completeInvitedUserRegistration(@RequestBody UserDto data) {
        String userId = userService.completeInvitedUserRegistration(UserServiceDto.builder()
                .invitationToken(data.getInvitationToken())
                .birth(data.getUserBirth())
                .lunarYN(data.getLunarYN())
                .build()
        );

        return ApiResponse.success(UserDto.builder().userId(userId).build());
    }
}
