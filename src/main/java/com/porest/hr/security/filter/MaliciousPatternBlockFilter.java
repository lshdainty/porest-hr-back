package com.porest.hr.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 악성 패턴 요청 차단 필터
 * - Spring Security 필터 체인 이전에 실행
 * - 버전 관리 시스템, 설정 파일, 백업 파일, 악성 스크립트 등 보안 위협 패턴 차단
 * - 403 Forbidden 응답 반환
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MaliciousPatternBlockFilter extends OncePerRequestFilter {

    /**
     * 차단할 악성 패턴 목록 (정규식)
     */
    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            // 버전 관리 시스템
            Pattern.compile(".*/\\.git(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.svn(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.hg(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.bzr(/.*)?$", Pattern.CASE_INSENSITIVE),

            // 환경 변수 및 설정 파일
            Pattern.compile(".*/\\.env(\\..*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.htaccess$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/web\\.config$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/php\\.ini$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/httpd\\.conf$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/nginx\\.conf$", Pattern.CASE_INSENSITIVE),

            // IDE 및 에디터 설정 파일
            Pattern.compile(".*/\\.idea(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.vscode(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.eclipse(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.settings(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.classpath$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.project$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/\\.DS_Store$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/Thumbs\\.db$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/desktop\\.ini$", Pattern.CASE_INSENSITIVE),

            // 백업 및 임시 파일 (OWASP 권장)
            Pattern.compile(".*\\.bak$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.backup$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.old$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.orig$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.tmp$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.temp$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.swp$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.swo$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.save$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*~$"),

            // 데이터베이스 및 설정 파일
            Pattern.compile(".*\\.sql$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.sqlite$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.db$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/config\\.php$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/config\\.yml$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/config\\.json$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/application\\.yml$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/application\\.properties$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/application-.*\\.yml$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/application-.*\\.properties$", Pattern.CASE_INSENSITIVE),

            // 압축 파일 (소스코드/백업 포함 가능성)
            Pattern.compile(".*\\.tar$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.tar\\.gz$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.tgz$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.zip$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.rar$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.7z$", Pattern.CASE_INSENSITIVE),

            // 공격 벡터 - 악성 스크립트
            Pattern.compile(".*/eval-stdin\\.php$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/shell\\.php$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/c99\\.php$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/r57\\.php$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/adminer\\.php$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/phpMyAdmin(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/phpmyadmin(/.*)?$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.php$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.asp$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.aspx$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.jsp\\.bak$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*\\.java\\.bak$", Pattern.CASE_INSENSITIVE),

            // 로그 파일 (민감 정보 포함 가능)
            Pattern.compile(".*\\.log$", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*/logs(/.*)?$", Pattern.CASE_INSENSITIVE)
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 악성 패턴 확인
        for (Pattern pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(requestUri).matches()) {
                handleBlockedRequest(request, response, requestUri, pattern.pattern());
                return; // 필터 체인 중단
            }
        }

        // 정상 요청은 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 차단된 요청 처리
     */
    private void handleBlockedRequest(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String requestUri,
                                      String matchedPattern) throws IOException {

        String clientIp = getClientIp(request);

        log.warn("Malicious pattern blocked - IP: {}, URI: {}, Method: {}, Pattern: {}, User-Agent: {}",
                clientIp,
                requestUri,
                request.getMethod(),
                matchedPattern,
                request.getHeader("User-Agent"));

        // 403 Forbidden 응답
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access to this resource is not allowed.\",\"path\":\"%s\"}",
                requestUri.replace("\"", "\\\"")
        );

        response.getWriter().write(jsonResponse);
    }

    /**
     * 클라이언트 실제 IP 주소 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
