package com.porest.hr.client.sso.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * SSO OAuth2 token 교환 요청 (표준 token request, snake_case).
 * client 백엔드(BFF)가 인가코드 + PKCE code_verifier 로 SSO {@code /oauth2/token} 호출.
 */
@Getter
@Builder
public class SsoTokenRequest {

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("code")
    private String code;

    @JsonProperty("code_verifier")
    private String codeVerifier;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("redirect_uri")
    private String redirectUri;
}
