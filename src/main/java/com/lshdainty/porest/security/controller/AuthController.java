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

import java.util.List;
import java.util.stream.Collectors;
import com.lshdainty.porest.permission.domain.Role;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthApi {
    private final SecurityService securityService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
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

        // 최신 사용자 정보 재조회 (프로필 이미지 변경 등 세션 외 데이터 반영)
        User user = userService.findUserById(userPrincipal.getUser().getId());

        // 역할 상세 정보 생성
        List<AuthApiDto.RoleInfo> roleInfos = user.getRoles().stream()
                .map(role -> new AuthApiDto.RoleInfo(
                        role.getCode(),
                        role.getName(),
                        role.getPermissions().stream()
                                .map(permission -> new AuthApiDto.PermissionInfo(
                                        permission.getCode(),
                                        permission.getName()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        // 모든 권한 코드 목록 (중복 제거)
        List<String> allPermissions = user.getAllAuthorities();

        AuthApiDto.LoginUserInfo result = new AuthApiDto.LoginUserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleInfos,  // 역할 상세 정보
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList()),  // 역할 이름 목록 (기존 호환)
                user.getRoles().isEmpty() ? null : user.getRoles().get(0).getName(),  // 첫 번째 역할 (기존 호환)
                allPermissions,  // 모든 권한 코드
                YNType.Y,
                StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                        userService.generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null
        );

        log.info("user info : {}, {}, {}, roles: {}, permissions: {}, {}",
                result.getUserId(), result.getUserName(), result.getUserEmail(),
                result.getUserRoles(), result.getPermissions(), result.getProfileUrl());

        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse<AuthApiDto.EncodePasswordResp> encodePassword(AuthApiDto.EncodePasswordReq data) {
        log.info("Password encoding request");

        String encodedPassword = passwordEncoder.encode(data.getUserPwd());

        return ApiResponse.success(new AuthApiDto.EncodePasswordResp(
                data.getUserPwd(),
                encodedPassword
        ));
    }

    @Override
    public ApiResponse<AuthApiDto.ValidateInvitationResp> validateInvitationToken(String token, HttpSession session) {
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
                user.getJoinDate(),
                user.getRoleNames(),
                user.getInvitationSentAt(),
                user.getInvitationExpiresAt(),
                user.getInvitationStatus()
        ));
    }

    @Override
    public ApiResponse<AuthApiDto.CompleteInvitationResp> completeInvitedUserRegistration(AuthApiDto.CompleteInvitationReq data, HttpSession session) {
        String userId = userService.completeInvitedUserRegistration(UserServiceDto.builder()
                .invitationToken(data.getInvitationToken())
                .birth(data.getUserBirth())
                .lunarYN(data.getLunarYn())
                .build()
        );

        // 회원가입 완료 후 세션 정리
        if (userId != null) {
            session.removeAttribute("invitationToken");
            session.removeAttribute("oauthStep");
            session.removeAttribute("invitedUserId");
        }

        return ApiResponse.success(new AuthApiDto.CompleteInvitationResp(userId));
    }
}
