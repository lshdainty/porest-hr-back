package com.porest.hr.security.jwt;

import com.porest.hr.common.config.properties.JwtProperties;
import com.porest.hr.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰 Provider
 * SSO에서 발급한 JWT 토큰을 검증하고, HR JWT를 발급합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey ssoKey;
    private SecretKey hrKey;

    private static final String HR_ISSUER = "porest-hr";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String TOKEN_TYPE_HR_ACCESS = "hr_access";

    @PostConstruct
    public void init() {
        this.ssoKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        // HR secret이 설정되어 있으면 별도 키 사용, 없으면 SSO 키와 동일하게 사용
        String hrSecret = jwtProperties.getHrSecret();
        if (hrSecret != null && !hrSecret.isBlank()) {
            this.hrKey = Keys.hmacShaKeyFor(hrSecret.getBytes(StandardCharsets.UTF_8));
        } else {
            this.hrKey = this.ssoKey;
        }
    }

    // ==================== SSO JWT 검증 ====================

    /**
     * SSO JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateSsoToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(ssoKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("SSO JWT token expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid SSO JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("SSO JWT token is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * SSO JWT 토큰에서 사용자 번호 추출
     *
     * @param token JWT 토큰
     * @return 사용자 번호
     */
    public Long getUserNoFromSsoToken(String token) {
        Claims claims = getSsoClaims(token);
        return claims.get("userNo", Long.class);
    }

    /**
     * SSO JWT 토큰에서 사용자 ID 추출
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserIdFromSsoToken(String token) {
        Claims claims = getSsoClaims(token);
        return claims.getSubject();
    }

    /**
     * SSO JWT 토큰에서 서비스 목록 추출
     *
     * @param token JWT 토큰
     * @return 서비스 코드 목록
     */
    @SuppressWarnings("unchecked")
    public List<String> getServicesFromSsoToken(String token) {
        Claims claims = getSsoClaims(token);
        List<String> services = claims.get("services", List.class);
        return services != null ? services : Collections.emptyList();
    }

    /**
     * SSO JWT 토큰에서 Claims 추출
     *
     * @param token JWT 토큰
     * @return Claims
     */
    private Claims getSsoClaims(String token) {
        return Jwts.parser()
                .verifyWith(ssoKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== HR JWT 생성 ====================

    /**
     * HR Access Token 생성
     *
     * @param user HR 사용자
     * @param roles 역할 목록
     * @param permissions 권한 목록
     * @return HR Access Token
     */
    public String createHrAccessToken(User user, List<String> roles, List<String> permissions) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getHrAccessExpiration());

        return Jwts.builder()
                .subject(user.getId())
                .issuer(HR_ISSUER)
                .issuedAt(now)
                .expiration(expiration)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_HR_ACCESS)
                .claim("ssoUserNo", user.getSsoUserNo())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .signWith(hrKey)
                .compact();
    }

    /**
     * HR Access Token 만료 시간 반환 (초 단위)
     *
     * @return 만료 시간 (초)
     */
    public long getHrAccessExpirationSeconds() {
        return jwtProperties.getHrAccessExpiration() / 1000;
    }

    // ==================== HR JWT 검증 ====================

    /**
     * HR JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateHrToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(hrKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // HR JWT인지 확인 (issuer 체크)
            String issuer = claims.getIssuer();
            return HR_ISSUER.equals(issuer);
        } catch (ExpiredJwtException e) {
            log.warn("HR JWT token expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Invalid HR JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("HR JWT token is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰 유효성 검증 (HR 또는 SSO)
     * HR JWT를 우선 검증하고, 실패하면 SSO JWT 검증
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        // HR JWT 먼저 검증
        if (validateHrToken(token)) {
            return true;
        }
        // SSO JWT 검증 (기존 호환성)
        return validateSsoToken(token);
    }

    /**
     * JWT 토큰에서 사용자 ID 추출 (HR 또는 SSO)
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public String getUserId(String token) {
        // HR JWT인지 확인
        if (isHrToken(token)) {
            return getHrClaims(token).getSubject();
        }
        // SSO JWT
        return getSsoClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 사용자 이름 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public String getUserName(String token) {
        Claims claims = isHrToken(token) ? getHrClaims(token) : getSsoClaims(token);
        return claims.get("name", String.class);
    }

    /**
     * JWT 토큰에서 이메일 추출
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmail(String token) {
        Claims claims = isHrToken(token) ? getHrClaims(token) : getSsoClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * HR JWT인지 확인
     *
     * @param token JWT 토큰
     * @return HR JWT 여부
     */
    public boolean isHrToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(hrKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return HR_ISSUER.equals(claims.getIssuer());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * HR JWT 토큰에서 Claims 추출
     *
     * @param token JWT 토큰
     * @return Claims
     */
    private Claims getHrClaims(String token) {
        return Jwts.parser()
                .verifyWith(hrKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== HR JWT Claims 추출 메서드 ====================

    /**
     * HR JWT 토큰에서 SSO 사용자 번호 추출
     *
     * @param token JWT 토큰
     * @return SSO 사용자 번호
     */
    public Long getSsoUserNoFromHrToken(String token) {
        Claims claims = getHrClaims(token);
        return claims.get("ssoUserNo", Long.class);
    }

    /**
     * HR JWT 토큰에서 사용자 이름 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public String getNameFromHrToken(String token) {
        Claims claims = getHrClaims(token);
        return claims.get("name", String.class);
    }

    /**
     * HR JWT 토큰에서 이메일 추출
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmailFromHrToken(String token) {
        Claims claims = getHrClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * HR JWT 토큰에서 역할 목록 추출
     *
     * @param token JWT 토큰
     * @return 역할 코드 목록
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromHrToken(String token) {
        Claims claims = getHrClaims(token);
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : Collections.emptyList();
    }

    /**
     * HR JWT 토큰에서 권한 목록 추출
     *
     * @param token JWT 토큰
     * @return 권한 코드 목록
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromHrToken(String token) {
        Claims claims = getHrClaims(token);
        List<String> permissions = claims.get("permissions", List.class);
        return permissions != null ? permissions : Collections.emptyList();
    }
}
