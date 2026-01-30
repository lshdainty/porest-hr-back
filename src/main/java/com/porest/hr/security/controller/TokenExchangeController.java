package com.porest.hr.security.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.porest.hr.security.controller.dto.TokenExchangeDto;
import com.porest.hr.security.service.TokenExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * SSO 토큰을 HR 토큰으로 교환
     *
     * @param request SSO 토큰이 포함된 요청
     * @return HR JWT 토큰 응답
     */
    @PostMapping("/exchange")
    @Operation(summary = "토큰 교환", description = "SSO JWT를 HR JWT로 교환합니다. HR 서비스 접근 권한이 있어야 합니다.")
    public ApiResponse<TokenExchangeDto.Response> exchangeToken(
            @Valid @RequestBody TokenExchangeDto.Request request) {
        log.debug("Token exchange request for SSO token");
        TokenExchangeDto.Response response = tokenExchangeService.exchange(request.getSsoToken());
        return ApiResponse.success(response);
    }
}
