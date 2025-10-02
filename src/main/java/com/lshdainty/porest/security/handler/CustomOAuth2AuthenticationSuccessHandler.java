package com.lshdainty.porest.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 인증 성공");

        HttpSession session = request.getSession();
        String invitationToken = (String) session.getAttribute("invitationToken");
        String oauthStep = (String) session.getAttribute("oauthStep");

        // 회원가입 중 OAuth2 연동인 경우
        if (invitationToken != null && "signup".equals(oauthStep)) {
            handleSignupOAuthConnect(request, response, authentication, invitationToken);
        } else {
            // 일반 로그인 성공
            handleNormalLogin(request, response, authentication);
        }
    }

    /**
     * 회원가입 중 OAuth2 연동 처리
     */
    private void handleSignupOAuthConnect(HttpServletRequest request,
                                          HttpServletResponse response,
                                          Authentication authentication,
                                          String invitationToken) throws IOException {

        HttpSession session = request.getSession();

        // OAuth2 제공자 이름 추출
        String provider = getProviderFromAuthentication(authentication);

        log.info("회원가입 OAuth2 연동 성공 - Provider: {}, Token: {}", provider, invitationToken);

        // 세션 정리 (보안)
        session.removeAttribute("invitationToken");
        session.removeAttribute("oauthStep");
        session.removeAttribute("invitedUserId");

        // React 회원가입 페이지로 리다이렉트 (연동 성공 상태)
        String redirectUrl = String.format("%s/signup?token=%s&oauth=%s&status=connected",
                frontendBaseUrl, invitationToken, provider);
        log.info("redirectUrl test log : {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    /**
     * 일반 OAuth2 로그인 성공 처리
     */
    private void handleNormalLogin(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Authentication authentication) throws IOException {

        log.info("일반 OAuth2 로그인 성공");

        // 기존 로그인 성공 처리
        // 메인 페이지나 대시보드로 리다이렉트
        String redirectUrl = frontendBaseUrl + "/dashboard";
        response.sendRedirect(redirectUrl);
    }

    /**
     * Authentication에서 OAuth2 제공자 이름 추출
     */
    private String getProviderFromAuthentication(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            return oauthToken.getAuthorizedClientRegistrationId();
        }
        return "unknown";
    }
}
