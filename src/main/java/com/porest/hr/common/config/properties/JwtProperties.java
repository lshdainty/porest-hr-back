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
     * SSO 토큰(RS256) 검증용 JWKS 엔드포인트 URI
     * 예: https://sso.../.well-known/jwks.json
     */
    private String ssoJwksUri;

    /**
     * HR 자체 토큰(HMAC) 서명·검증 키
     * HMAC-SHA256 요구사항에 따라 256bit(32자) 이상이어야 한다
     */
    private String hrSecret;

    /**
     * HR Access Token 만료 시간 (밀리초)
     * 기본값: 1시간 (3600000ms)
     */
    private long hrAccessExpiration = 3600000;
}
