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
     */
    @Getter
    @Builder
    public static class Response {
        private String accessToken;
        private String tokenType;
        private long expiresIn;
        private UserInfo user;

        public static Response of(String accessToken, long expiresIn, UserInfo user) {
            return Response.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(expiresIn)
                    .user(user)
                    .build();
        }
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
