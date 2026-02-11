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
     * SSO와 동일한 키를 사용해야 함
     */
    private String secret;

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
