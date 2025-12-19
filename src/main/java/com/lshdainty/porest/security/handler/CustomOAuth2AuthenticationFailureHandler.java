package com.lshdainty.porest.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 인증 실패: {}", exception.getMessage());

        HttpSession session = request.getSession(false);
        String oauthStep = session != null ? (String) session.getAttribute("oauthStep") : null;

        // OAuth 연동 중이었다면
        if ("link".equals(oauthStep)) {
            String errorCode = exception.getMessage();  // "already_linked_self", "already_linked_other" 등
            String redirectUrl = String.format("%s/?oauth_link=error&code=%s",
                    frontendBaseUrl, URLEncoder.encode(errorCode, StandardCharsets.UTF_8));

            // 세션 정리
            if (session != null) {
                session.removeAttribute("oauthStep");
                session.removeAttribute("loginUserId");
            }

            response.sendRedirect(redirectUrl);
        } else {
            // 로그인 실패
            String errorMessage = URLEncoder.encode("소셜 로그인에 실패했습니다.", StandardCharsets.UTF_8);
            String redirectUrl = String.format("%s/login?error=%s", frontendBaseUrl, errorMessage);
            response.sendRedirect(redirectUrl);
        }
    }
}
