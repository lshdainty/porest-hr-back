package com.lshdainty.porest.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CSRF 토큰을 즉시 로드하여 쿠키에 저장하는 필터
 * Spring Security의 Deferred Token 패턴을 해제하고, 모든 요청에서 즉시 토큰을 생성합니다.
 */
@Slf4j
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // CsrfToken을 즉시 로드하여 쿠키에 저장
        // Spring Security가 request attribute에 CsrfToken을 넣어둡니다.
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // getToken()을 호출하면 토큰이 즉시 생성되고 쿠키에 저장됩니다.
            csrfToken.getToken();
            log.debug("CSRF 토큰 생성 완료: uri={}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
