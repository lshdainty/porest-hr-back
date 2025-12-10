package com.lshdainty.porest.security.handler;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.security.controller.dto.AuthApiDto;
import com.lshdainty.porest.security.principal.CustomOAuth2User;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import com.lshdainty.porest.permission.domain.Role;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

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

        // CustomOAuth2User에서 User 정보 추출
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = customOAuth2User.getUser();

        // 역할 상세 정보 생성
        List<AuthApiDto.RoleInfo> roleInfos = user.getRoles().stream()
                .map(role -> new AuthApiDto.RoleInfo(
                        role.getCode(),
                        role.getName(),
                        role.getPermissions().stream()
                                .map(permission -> new AuthApiDto.PermissionInfo(
                                        permission.getCode(),
                                        permission.getName()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        // 모든 권한 코드 목록 (중복 제거)
        List<String> allPermissions = user.getAllAuthorities();

        // 세션에 사용자 정보 저장
        HttpSession session = request.getSession();

        session.setAttribute("user", new AuthApiDto.LoginUserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleInfos,  // 역할 상세 정보
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList()),  // 역할 이름 목록 (기존 호환)
                user.getRoles().isEmpty() ? null : user.getRoles().get(0).getName(),  // 첫 번째 역할 (기존 호환)
                allPermissions,  // 모든 권한 코드
                YNType.Y,
                StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                        userService.generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null
        ));
        log.info("세션에 사용자 정보 저장 완료 - userId: {}, userName: {}", user.getId(), user.getName());

        // 로그인 페이지로 리다이렉트 (프론트에서 세션 정보 조회)
        String redirectUrl = frontendBaseUrl + "/login?status=success";
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
