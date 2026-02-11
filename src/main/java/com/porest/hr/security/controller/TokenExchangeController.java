package com.porest.hr.security.controller;

import com.porest.core.controller.ApiResponse;
import com.porest.hr.common.config.properties.JwtProperties;
import com.porest.hr.security.controller.dto.TokenExchangeDto;
import com.porest.hr.security.service.TokenExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 토큰 교환 API 컨트롤러
 * SSO JWT를 HR JWT로 교환하는 엔드포인트를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Token Exchange", description = "토큰 교환 API")
public class TokenExchangeController {

    private final TokenExchangeService tokenExchangeService;
    private final JwtProperties jwtProperties;

    private static final String ACCESS_TOKEN_COOKIE = "hr_access_token";

    /**
     * SSO 토큰을 HR 토큰으로 교환
     * HR JWT를 HttpOnly Cookie로 설정하고 사용자 정보를 응답합니다.
     *
     * @param request SSO 토큰이 포함된 요청
     * @param response HTTP 응답 (쿠키 설정용)
     * @return 사용자 정보 응답 (토큰은 쿠키로 전달)
     */
    @PostMapping("/exchange")
    @Operation(summary = "토큰 교환", description = "SSO JWT를 HR JWT로 교환합니다. HR 서비스 접근 권한이 있어야 합니다.")
    public ApiResponse<TokenExchangeDto.Response> exchangeToken(
            @Valid @RequestBody TokenExchangeDto.Request request,
            HttpServletResponse response) {
        log.debug("Token exchange request for SSO token");
        TokenExchangeDto.ExchangeResult result = tokenExchangeService.exchange(request.getSsoToken());

        // HR JWT를 HttpOnly Cookie로 설정
        setAccessTokenCookie(response, result.getAccessToken());

        return ApiResponse.success(TokenExchangeDto.Response.of(result.getExpiresIn(), result.getUser()));
    }

    /**
     * 로그아웃 (쿠키 삭제)
     *
     * @param response HTTP 응답 (쿠키 삭제용)
     * @return 성공 응답
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "HR JWT 쿠키를 삭제합니다.")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        return ApiResponse.success(null);
    }

    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtProperties.getHrAccessExpiration() / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
