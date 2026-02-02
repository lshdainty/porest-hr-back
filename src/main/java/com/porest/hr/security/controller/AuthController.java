package com.porest.hr.security.controller;

import com.porest.core.controller.ApiResponse;
import com.porest.core.exception.ErrorCode;
import com.porest.core.exception.UnauthorizedException;
import com.porest.core.type.YNType;
import com.porest.hr.security.controller.dto.AuthApiDto;
import com.porest.hr.security.principal.UserPrincipal;
import com.porest.hr.security.service.IpBlacklistService;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.porest.hr.permission.domain.Role;
import org.springframework.web.bind.annotation.*;

/**
 * 인증/보안 API 컨트롤러
 * JWT 토큰으로 인증된 사용자 정보 조회 및 IP 블랙리스트 관리를 담당합니다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "인증/보안 API")
public class AuthController implements AuthApi {
    private final UserService userService;
    private final IpBlacklistService ipBlacklistService;

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
        // getUserId()는 JWT Claims에서 직접 반환하므로 DB 조회 없음
        User user = userService.findUserById(userPrincipal.getUserId());

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
                YNType.N,  // 비밀번호 변경 필요 여부 (SSO에서 관리)
                null  // 초대 상태 (SSO에서 관리)
        );

        log.info("user info : {}, {}, {}, roles: {}, permissions: {}, {}",
                result.getUserId(), result.getUserName(), result.getUserEmail(),
                result.getUserRoles(), result.getPermissions(), result.getProfileUrl());

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
