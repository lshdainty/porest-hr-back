package com.lshdainty.porest.login.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.login.service.CustomUserDetailsService;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.login.service.dto.LoginServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginApiController {
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 사용자 정보 조회 API (로그인 상태 확인)
     * Spring Security에서 관리하는 인증 정보를 기반으로 사용자 정보를 반환합니다.
     */
    @GetMapping("/api/v1/user-info")
    public ResponseEntity<ApiResponse> getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
                Map<String, Object> result = new HashMap<>();
                result.put("isLoggedIn", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(ApiResponse.fail("인증이 필요합니다."));
            }

            CustomUserDetailsService.CustomUserDetails userDetails = (CustomUserDetailsService.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            Map<String, Object> result = new HashMap<>();
            result.put("isLoggedIn", true);
            result.put("userId", user.getId());
            result.put("userRole", user.getRole().name());
            result.put("message", "로그인 상태입니다.");

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            log.error("Failed to get user info", e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.fail("사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 인코딩 유틸리티 API (개발/테스트용)
     */
    @PostMapping("/encode-password")
    public ResponseEntity<ApiResponse> encodePassword(@RequestBody LoginServiceDto loginServiceDto) {
        log.info("Password encoding request for user: {}", loginServiceDto.getId());

        String encodedPassword = passwordEncoder.encode(loginServiceDto.getPw());

        Map<String, String> result = new HashMap<>();
        result.put("originalPassword", loginServiceDto.getPw());
        result.put("encodedPassword", encodedPassword);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

}