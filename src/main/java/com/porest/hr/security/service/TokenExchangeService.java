package com.porest.hr.security.service;

import com.porest.core.exception.ErrorCode;
import com.porest.core.exception.ForbiddenException;
import com.porest.core.exception.UnauthorizedException;
import com.porest.hr.common.exception.HrErrorCode;
import com.porest.hr.permission.domain.Role;
import com.porest.hr.security.controller.dto.TokenExchangeDto;
import com.porest.hr.security.jwt.JwtTokenProvider;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 토큰 교환 서비스
 * SSO JWT를 검증하고 HR JWT를 발급합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenExchangeService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    private static final String HR_SERVICE_CODE = "hr";

    /**
     * SSO 토큰을 HR 토큰으로 교환
     *
     * @param ssoToken SSO에서 발급한 JWT 토큰
     * @return HR JWT 토큰 응답
     */
    public TokenExchangeDto.ExchangeResult exchange(String ssoToken) {
        log.debug("Token exchange request received");

        // 1. SSO JWT 검증
        if (!jwtTokenProvider.validateSsoToken(ssoToken)) {
            log.warn("Invalid SSO token");
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED, "Invalid SSO token");
        }

        // 2. SSO JWT에서 서비스 접근 목록 추출
        List<String> services = jwtTokenProvider.getServicesFromSsoToken(ssoToken);
        log.debug("Services from SSO token: {}", services);

        // 3. HR 서비스 접근 권한 확인
        if (!services.contains(HR_SERVICE_CODE)) {
            log.warn("User does not have access to HR service");
            throw new ForbiddenException(HrErrorCode.PERMISSION_DENIED, "HR service access denied");
        }

        // 4. 사용자 정보 추출
        String userId = jwtTokenProvider.getUserIdFromSsoToken(ssoToken);

        // 5. HR DB에서 사용자 조회
        User user = userService.findUserById(userId);

        // 6. 역할/권한 조회
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
        List<String> permissions = user.getAllAuthorities();

        // 7. HR JWT 발급
        String hrToken = jwtTokenProvider.createHrAccessToken(user, roles, permissions);
        long expiresIn = jwtTokenProvider.getHrAccessExpirationSeconds();

        // 8. 응답 생성
        TokenExchangeDto.UserInfo userInfo = TokenExchangeDto.UserInfo.builder()
                .userNo(user.getSsoUserRowId())
                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .build();

        log.info("Token exchanged successfully for user: {}", userId);

        return TokenExchangeDto.ExchangeResult.builder()
                .accessToken(hrToken)
                .expiresIn(expiresIn)
                .user(userInfo)
                .build();
    }
}
