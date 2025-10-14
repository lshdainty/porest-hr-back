package com.lshdainty.porest.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.security.controller.dto.AuthApiDto;
import com.lshdainty.porest.security.service.CustomUserDetailsService;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 로그인 성공 핸들러
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CustomUserDetailsService.CustomUserDetails userDetails =
                (CustomUserDetailsService.CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        ApiResponse<AuthApiDto.LoginUserInfo> apiResponse = ApiResponse.success(new AuthApiDto.LoginUserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getRole().name(),
                YNType.Y,
                StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                        userService.generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null
        ));
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}