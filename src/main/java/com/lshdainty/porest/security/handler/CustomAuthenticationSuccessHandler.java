package com.lshdainty.porest.security.handler;

import tools.jackson.databind.ObjectMapper;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.security.controller.dto.AuthApiDto;
import com.lshdainty.porest.security.service.CustomUserDetailsService;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import com.lshdainty.porest.permission.domain.Role;

/**
 * 로그인 성공 핸들러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("로그인 성공: username={}", authentication.getName());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CustomUserDetailsService.CustomUserDetails userDetails =
                (CustomUserDetailsService.CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

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

        ApiResponse<AuthApiDto.LoginUserInfo> apiResponse = ApiResponse.success(new AuthApiDto.LoginUserInfo(
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
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}