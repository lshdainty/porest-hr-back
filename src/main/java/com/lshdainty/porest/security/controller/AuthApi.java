package com.lshdainty.porest.security.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.security.controller.dto.AuthApiDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 및 회원가입 API")
public interface AuthApi {

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

    @Operation(
            summary = "비밀번호 인코딩 (개발/테스트용)",
            description = "평문 비밀번호를 BCrypt로 인코딩합니다. 개발 및 테스트 환경에서만 사용하세요."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 인코딩 성공",
                    content = @Content(schema = @Schema(implementation = AuthApiDto.EncodePasswordResp.class))
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
            summary = "초대 토큰 유효성 검증",
            description = "초대 토큰의 유효성을 검증하고 초대된 사용자 정보를 반환합니다. 검증 성공 시 세션에 토큰과 사용자 정보를 저장합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 검증 성공",
                    content = @Content(schema = @Schema(implementation = AuthApiDto.ValidateInvitationResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 토큰"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰에 해당하는 사용자를 찾을 수 없음"
            )
    })
    @GetMapping("/oauth2/signup/validate")
    ApiResponse<AuthApiDto.ValidateInvitationResp> validateInvitationToken(
            @Parameter(description = "초대 토큰", example = "abc123def456", required = true)
            @RequestParam("token") String token,
            HttpSession session
    );

    @Operation(
            summary = "초대받은 사용자 회원가입 완료",
            description = "초대받은 사용자의 추가 정보(생년월일, 음력여부)를 입력받아 회원가입을 완료합니다. 완료 후 세션 정보를 정리합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 완료 성공",
                    content = @Content(schema = @Schema(implementation = AuthApiDto.CompleteInvitationResp.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 토큰 또는 요청 데이터"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰에 해당하는 사용자를 찾을 수 없음"
            )
    })
    @PostMapping("/oauth2/signup/invitation/complete")
    ApiResponse<AuthApiDto.CompleteInvitationResp> completeInvitedUserRegistration(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 완료 정보 (초대 토큰, 생년월일, 음력여부)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthApiDto.CompleteInvitationReq.class))
            )
            @RequestBody AuthApiDto.CompleteInvitationReq data,
            HttpSession session
    );
}
