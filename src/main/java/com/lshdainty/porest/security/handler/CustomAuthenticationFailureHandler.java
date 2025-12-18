package com.lshdainty.porest.security.handler;

import tools.jackson.databind.ObjectMapper;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 실패 핸들러
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        // 보안: 상세 에러는 서버 로그에만 기록, 프론트에는 일반 메시지만 반환
        log.warn("로그인 실패: username={}, reason={}", username, exception.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 보안: 예외 메시지를 직접 노출하지 않음 (SQL 쿼리 등 민감한 정보 포함 가능)
        ApiResponse<Void> apiResponse = ApiResponse.error(
                ErrorCode.UNAUTHORIZED.getCode(),
                "아이디 또는 비밀번호가 올바르지 않습니다."
        );
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }
}