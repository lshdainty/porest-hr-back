package com.porest.hr.common.config.database;

import com.porest.core.security.AuditorPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA Auditing을 위한 AuditorAware 구현체
 * Spring Security의 SecurityContext에서 현재 로그인한 사용자의 ID를 가져옵니다.
 */
@Component("auditorAware")
public class LoginUserAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        // AuditorPrincipal 인터페이스를 구현한 객체에서 User ID 가져오기
        if (authentication.getPrincipal() instanceof AuditorPrincipal auditorPrincipal) {
            return Optional.ofNullable(auditorPrincipal.getUserId());
        }

        return Optional.empty();
    }
}
