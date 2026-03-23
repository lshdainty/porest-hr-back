package com.porest.hr.security.filter;

import tools.jackson.databind.ObjectMapper;
import com.porest.hr.security.service.IpBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * IP 블랙리스트 차단 필터
 * - Spring Security 필터 체인 앞단에서 실행
 * - 블랙리스트 IP의 모든 요청을 차단
 * - 403 Forbidden 응답 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpBlockFilter extends OncePerRequestFilter {

    private final IpBlacklistService ipBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);

        // IP 블랙리스트 확인
        if (ipBlacklistService.isBlocked(clientIp)) {
            handleBlockedRequest(request, response, clientIp);
            return; // 필터 체인 중단
        }

        // 정상 요청은 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 차단된 요청 처리
     */
    private void handleBlockedRequest(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String clientIp) throws IOException {

        log.warn("🚫 IP BLOCKED - IP: {}, URI: {}, Method: {}, User-Agent: {}",
                clientIp,
                request.getRequestURI(),
                request.getMethod(),
                request.getHeader("User-Agent"));

        // 403 Forbidden 응답
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(
                new BlockedIpResponse(
                        403,
                        "Forbidden",
                        "Access denied. Your IP address has been blocked.",
                        clientIp
                )
        );

        response.getWriter().write(jsonResponse);
    }

    /**
     * 클라이언트 실제 IP 주소 추출
     * - Proxy/Load Balancer 고려
     * - X-Forwarded-For, X-Real-IP 헤더 확인
     */
    private static final java.util.regex.Pattern IP_PATTERN = java.util.regex.Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$" +
            "|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$" +
            "|^::1$"
    );

    private String getClientIp(HttpServletRequest request) {
        // forward-headers-strategy: framework 설정에 의해
        // Spring이 프록시 헤더를 처리하므로 getRemoteAddr()를 우선 사용
        String remoteAddr = request.getRemoteAddr();

        // X-Forwarded-For는 신뢰할 수 있는 프록시 환경에서만 참조
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
            String ip = forwardedFor.contains(",")
                    ? forwardedFor.split(",")[0].trim()
                    : forwardedFor.trim();

            // IP 형식 검증으로 스푸핑 방지
            if (IP_PATTERN.matcher(ip).matches()) {
                return ip;
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
            if (IP_PATTERN.matcher(realIp.trim()).matches()) {
                return realIp.trim();
            }
        }

        return remoteAddr;
    }

    /**
     * 차단 응답 DTO
     */
    private record BlockedIpResponse(
            int status,
            String error,
            String message,
            String blockedIp
    ) {}
}
