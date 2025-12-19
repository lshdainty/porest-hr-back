package com.lshdainty.porest.security.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.UnauthorizedException;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.security.controller.dto.AuthApiDto;
import com.lshdainty.porest.security.principal.UserPrincipal;
import com.lshdainty.porest.security.service.IpBlacklistService;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.user.domain.UserProvider;
import com.lshdainty.porest.user.repository.UserProviderRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증/보안 API")
public class AuthController implements AuthApi {
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final IpBlacklistService ipBlacklistService;
    private final UserProviderRepository userProviderRepository;

    /**
     * CSRF 토큰 발급
     * 이 메서드가 호출되면 Spring Security가 자동으로 CSRF 토큰을 생성하고
     * XSRF-TOKEN 쿠키에 담아 응답합니다.
     */
    @Override
    public void getCsrfToken(HttpServletRequest request) {
        // Spring Security가 자동으로 CSRF 토큰을 생성하고 쿠키에 담아줍니다.
        log.debug("CSRF token requested from: {}", request.getRemoteAddr());
    }

    /**
     * 비밀번호 인코딩 (개발/테스트 환경 전용)
     * Production 환경에서는 404 에러가 발생합니다.
     */
    @Override
    @Profile({"local", "dev"})  // local, dev 프로파일에서만 활성화
    public ApiResponse<AuthApiDto.EncodePasswordResp> encodePassword(AuthApiDto.EncodePasswordReq data) {
        log.warn("⚠️ Development tool accessed - Password encoding request");

        String encodedPassword = passwordEncoder.encode(data.getUserPwd());

        return ApiResponse.success(new AuthApiDto.EncodePasswordResp(
                data.getUserPwd(),
                encodedPassword
        ));
    }

    @Override
    public ApiResponse<AuthApiDto.LoginUserInfo> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        // UserPrincipal 인터페이스로 통합 처리
        if (!(principal instanceof UserPrincipal)) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
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
                        userService.generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null,
                user.getPasswordChangeRequired(),  // 비밀번호 변경 필요 여부
                user.getInvitationStatus()  // 초대 상태
        );

        log.info("user info : {}, {}, {}, roles: {}, permissions: {}, {}",
                result.getUserId(), result.getUserName(), result.getUserEmail(),
                result.getUserRoles(), result.getPermissions(), result.getProfileUrl());

        return ApiResponse.success(result);
    }

    // ========== OAuth 연동 관리 ==========

    @Override
    public ApiResponse<AuthApiDto.OAuthLinkStartResp> startOAuthLink(String provider, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        String loginUserId = userPrincipal.getUser().getId();

        // 세션에 OAuth 연동 정보 저장
        session.setAttribute("oauthStep", "link");
        session.setAttribute("loginUserId", loginUserId);

        log.info("OAuth 연동 시작: userId={}, provider={}", loginUserId, provider);

        // OAuth 인증 URL 반환
        String authUrl = "/oauth2/authorization/" + provider.toLowerCase();
        return ApiResponse.success(new AuthApiDto.OAuthLinkStartResp(authUrl));
    }

    @Override
    public ApiResponse<List<AuthApiDto.LinkedProviderResp>> getLinkedProviders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        String loginUserId = userPrincipal.getUser().getId();

        List<UserProvider> providers = userProviderRepository.findByUserId(loginUserId);

        List<AuthApiDto.LinkedProviderResp> result = providers.stream()
                .map(p -> new AuthApiDto.LinkedProviderResp(
                        p.getSeq(),
                        p.getType(),
                        p.getCreateDate()
                ))
                .collect(Collectors.toList());

        log.info("연동된 OAuth 제공자 조회: userId={}, count={}", loginUserId, result.size());
        return ApiResponse.success(result);
    }

    // ========== IP 블랙리스트 관리 (개발 환경 전용) ==========

    @Override
    @Profile({"local", "dev"})
    public ApiResponse<Set<String>> getRuntimeBlacklist() {
        Set<String> blacklist = ipBlacklistService.getRuntimeBlacklist();
        log.info("Runtime blacklist retrieved: {} IPs", blacklist.size());
        return ApiResponse.success(blacklist);
    }

    @Override
    @Profile({"local", "dev"})
    public ApiResponse<String> addToBlacklist(String ip) {
        ipBlacklistService.addToBlacklist(ip);
        log.warn("⚠️ IP manually added to blacklist: {}", ip);
        return ApiResponse.success("IP " + ip + " has been added to blacklist");
    }

    @Override
    @Profile({"local", "dev"})
    public ApiResponse<String> removeFromBlacklist(String ip) {
        ipBlacklistService.removeFromBlacklist(ip);
        log.info("IP removed from blacklist: {}", ip);
        return ApiResponse.success("IP " + ip + " has been removed from blacklist");
    }

    @Override
    @Profile({"local", "dev"})
    public ApiResponse<Boolean> checkIpStatus(String ip) {
        boolean isBlocked = ipBlacklistService.isBlocked(ip);
        return ApiResponse.success(isBlocked);
    }
}
