package com.porest.hr.security.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 교환 관련 DTO
 */
public class TokenExchangeDto {

    /**
     * 토큰 교환 요청 DTO
     */
    @Getter
    public static class Request {
        @NotBlank(message = "SSO 토큰은 필수입니다")
        private String ssoToken;
    }

    /**
     * 토큰 교환 응답 DTO
     * accessToken은 HttpOnly Cookie로 전달하므로 body에 포함하지 않음
     */
    @Getter
    @Builder
    public static class Response {
        private long expiresIn;
        private UserInfo user;

        public static Response of(long expiresIn, UserInfo user) {
            return Response.builder()
                    .expiresIn(expiresIn)
                    .user(user)
                    .build();
        }
    }

    /**
     * 토큰 교환 서비스 결과 DTO
     * accessToken을 포함하여 Controller에서 쿠키 설정에 사용
     */
    @Getter
    @Builder
    public static class ExchangeResult {
        private String accessToken;
        private long expiresIn;
        private UserInfo user;
    }

    /**
     * 사용자 정보 DTO
     */
    @Getter
    @Builder
    public static class UserInfo {
        private Long userNo;
        private String userId;
        private String userName;
        private String userEmail;
        private java.util.List<String> roles;
        private java.util.List<String> permissions;
    }
}
