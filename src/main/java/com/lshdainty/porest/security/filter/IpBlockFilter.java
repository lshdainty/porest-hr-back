package com.lshdainty.porest.security.filter;

import tools.jackson.databind.ObjectMapper;
import com.lshdainty.porest.security.service.IpBlacklistService;
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
    private String getClientIp(HttpServletRequest request) {
        // Proxy를 통한 경우 실제 IP 추출
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR",
                "X-Real-IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP를 포함할 수 있음 (쉼표로 구분)
                // 첫 번째 IP가 실제 클라이언트 IP
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // 헤더에 없으면 기본 remote address 사용
        return request.getRemoteAddr();
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
