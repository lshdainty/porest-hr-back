package com.lshdainty.porest.security.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.security.controller.dto.AuthApiDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Auth", description = "인증 및 보안 API")
public interface AuthApi {

    @Operation(
            summary = "CSRF 토큰 발급",
            description = "앱 시작 시 CSRF 토큰을 쿠키로 발급받습니다. " +
                    "이후 모든 변경 요청(POST/PUT/DELETE)에는 X-XSRF-TOKEN 헤더로 토큰을 전송해야 합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CSRF 토큰이 XSRF-TOKEN 쿠키에 설정됨"
            )
    })
    @GetMapping("/api/v1/csrf-token")
    void getCsrfToken(HttpServletRequest request);

    @Operation(
            summary = "비밀번호 인코딩 (개발/테스트용)",
            description = "⚠️ 개발 및 테스트 환경에서만 사용 가능합니다. Production 환경에서는 비활성화됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 인코딩 성공",
                    content = @Content(schema = @Schema(implementation = AuthApiDto.EncodePasswordResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Production 환경에서는 사용 불가"
            )
    })
    @PostMapping("/api/v1/encode-password")
    ApiResponse<AuthApiDto.EncodePasswordResp> encodePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "인코딩할 비밀번호 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthApiDto.EncodePasswordReq.class))
            )
            @RequestBody AuthApiDto.EncodePasswordReq data
    );

    @Operation(
            summary = "로그인 사용자 정보 조회",
            description = "현재 로그인한 사용자의 상세 정보를 조회합니다. 사용자 ID, 이름, 이메일, 역할, 권한, 프로필 이미지 URL 등을 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = AuthApiDto.LoginUserInfo.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 (로그인되지 않음)"
            )
    })
    @GetMapping("/api/v1/login/check")
    ApiResponse<AuthApiDto.LoginUserInfo> getUserInfo();

    // ========== OAuth 연동 관리 ==========

    @Operation(
            summary = "OAuth 연동 시작",
            description = "로그인된 사용자의 소셜 계정 연동을 시작합니다. 세션에 연동 정보를 저장하고 OAuth 인증 URL을 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "OAuth 연동 시작 성공",
                    content = @Content(schema = @Schema(implementation = AuthApiDto.OAuthLinkStartResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 (로그인되지 않음)"
            )
    })
    @PostMapping("/api/v1/oauth/link/start")
    ApiResponse<AuthApiDto.OAuthLinkStartResp> startOAuthLink(
            @Parameter(description = "OAuth 제공자 (google, kakao, naver)", example = "google", required = true)
            @RequestParam String provider,
            HttpSession session
    );

    @Operation(
            summary = "연동된 OAuth 제공자 목록 조회",
            description = "현재 로그인된 사용자에게 연동된 OAuth 제공자 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AuthApiDto.LinkedProviderResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 (로그인되지 않음)"
            )
    })
    @GetMapping("/api/v1/oauth/providers")
    ApiResponse<List<AuthApiDto.LinkedProviderResp>> getLinkedProviders();

    // ========== IP 블랙리스트 관리 (개발 환경 전용) ==========

    @Operation(
            summary = "런타임 블랙리스트 조회",
            description = "현재 런타임에 추가된 IP 블랙리스트를 조회합니다. (설정 파일 기반 블랙리스트는 제외)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "블랙리스트 조회 성공",
                    content = @Content(schema = @Schema(implementation = Set.class))
            )
    })
    @GetMapping("/api/v1/security/ip-blacklist")
    ApiResponse<Set<String>> getRuntimeBlacklist();

    @Operation(
            summary = "IP 블랙리스트에 추가",
            description = "런타임에 특정 IP를 블랙리스트에 추가합니다. 즉시 차단이 적용됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "블랙리스트 추가 성공"
            )
    })
    @PostMapping("/api/v1/security/ip-blacklist")
    ApiResponse<String> addToBlacklist(
            @Parameter(description = "차단할 IP 주소", example = "192.168.1.100", required = true)
            @RequestParam String ip
    );

    @Operation(
            summary = "IP 블랙리스트에서 제거",
            description = "런타임 블랙리스트에서 특정 IP를 제거합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "블랙리스트 제거 성공"
            )
    })
    @DeleteMapping("/api/v1/security/ip-blacklist")
    ApiResponse<String> removeFromBlacklist(
            @Parameter(description = "차단 해제할 IP 주소", example = "192.168.1.100", required = true)
            @RequestParam String ip
    );

    @Operation(
            summary = "IP 차단 상태 확인",
            description = "특정 IP가 현재 블랙리스트에 있는지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "차단 상태 확인 성공",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    @GetMapping("/api/v1/security/ip-blacklist/check")
    ApiResponse<Boolean> checkIpStatus(
            @Parameter(description = "확인할 IP 주소", example = "192.168.1.100", required = true)
            @RequestParam String ip
    );
}
