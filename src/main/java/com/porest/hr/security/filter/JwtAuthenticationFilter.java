package com.porest.hr.security.filter;

import com.porest.hr.security.jwt.JwtTokenProvider;
import com.porest.hr.security.principal.JwtClaimsPrincipal;
import com.porest.hr.security.principal.JwtUserPrincipal;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 인증 필터
 * Authorization 헤더의 Bearer 토큰을 검증하고 SecurityContext에 인증 정보를 설정합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            try {
                Authentication authentication;

                if (jwtTokenProvider.isHrToken(token)) {
                    // HR JWT인 경우: Claims에서 직접 정보 추출 (DB 조회 없음)
                    authentication = createAuthenticationFromHrToken(token);
                } else {
                    // SSO JWT인 경우: HR DB 조회 필요 (하위 호환성 유지)
                    authentication = createAuthenticationFromSsoToken(token);
                }

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.warn("JWT 인증 처리 중 오류: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HR JWT에서 인증 객체 생성 (DB 조회 없음)
     * JWT Claims에서 직접 사용자 정보와 권한을 추출합니다.
     *
     * @param token HR JWT 토큰
     * @return Authentication 객체
     */
    private Authentication createAuthenticationFromHrToken(String token) {
        // JWT Claims에서 정보 추출
        String userId = jwtTokenProvider.getUserId(token);
        Long ssoUserNo = jwtTokenProvider.getSsoUserNoFromHrToken(token);
        String name = jwtTokenProvider.getNameFromHrToken(token);
        String email = jwtTokenProvider.getEmailFromHrToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromHrToken(token);
        List<String> permissions = jwtTokenProvider.getPermissionsFromHrToken(token);

        // 권한 목록 생성 (roles + permissions)
        List<String> allAuthorities = new ArrayList<>(roles);
        allAuthorities.addAll(permissions);
        List<SimpleGrantedAuthority> authorities = allAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // JwtClaimsPrincipal 생성 (User는 필요할 때 Lazy Loading)
        JwtClaimsPrincipal principal = new JwtClaimsPrincipal(
                userId, ssoUserNo, name, email, roles, permissions,
                () -> userService.findUserById(userId)  // Lazy Loading Supplier
        );

        log.debug("HR JWT 인증 성공 (DB 조회 없음): userId={}, authorities={}", userId, authorities.size());

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    /**
     * SSO JWT에서 인증 객체 생성 (DB 조회 필요)
     * SSO JWT에는 HR 권한 정보가 없으므로 DB에서 조회합니다.
     *
     * @param token SSO JWT 토큰
     * @return Authentication 객체
     */
    private Authentication createAuthenticationFromSsoToken(String token) {
        // JWT에서 사용자 ID 추출
        String userId = jwtTokenProvider.getUserId(token);

        // HR DB에서 사용자 및 권한 조회
        User user = userService.findUserById(userId);

        // 권한 목록 조회
        List<SimpleGrantedAuthority> authorities = user.getAllAuthorities().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // JwtUserPrincipal 생성 (기존 방식 유지)
        JwtUserPrincipal principal = new JwtUserPrincipal(user);

        log.debug("SSO JWT 인증 성공 (DB 조회): userId={}, authorities={}", userId, authorities.size());

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
