package com.lshdainty.porest.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 보안 관련 설정
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * IP 블랙리스트 설정
     */
    private IpBlacklist ipBlacklist = new IpBlacklist();

    @Getter
    @Setter
    public static class IpBlacklist {
        /**
         * IP 블랙리스트 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 차단할 IP 목록
         * 예: ["192.168.1.100", "10.0.0.5"]
         * CIDR 표기법도 지원: ["192.168.1.0/24"]
         */
        private List<String> blockedIps = new ArrayList<>();

        /**
         * 차단 시 로그 레벨 (WARN 또는 ERROR)
         */
        private String logLevel = "WARN";
    }
}
