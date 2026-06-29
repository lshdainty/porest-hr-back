package com.porest.hr.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정 Properties
 * SSO에서 발급한 JWT를 검증하고, HR JWT를 발급하기 위한 설정
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 서명 검증을 위한 비밀키
     * (RS256 전환 후 SSO 검증엔 미사용 — HR 자체 토큰 hrSecret 폴백용으로만 유지)
     */
    private String secret;

    /**
     * SSO 토큰(RS256) 검증용 JWKS 엔드포인트 URI
     * 예: https://sso.../.well-known/jwks.json
     */
    private String ssoJwksUri;

    /**
     * HR JWT 발급을 위한 비밀키
     * SSO와 다른 별도의 키 사용
     */
    private String hrSecret;

    /**
     * HR Access Token 만료 시간 (밀리초)
     * 기본값: 1시간 (3600000ms)
     */
    private long hrAccessExpiration = 3600000;
}
