package com.porest.hr.security.filter;

import com.porest.hr.common.config.properties.JwtProperties;
import com.porest.hr.security.jwt.JwtTokenProvider;
import com.porest.hr.user.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

/**
 * 슬라이딩 세션 갱신 — 액세스 토큰(1시간)이 "쓰고 있는 중"에 정각으로 끊기지 않도록,
 * 잔여 수명이 임계(10분) 아래로 내려간 유효 토큰은 응답에 새 토큰 쿠키를 실어 준다.
 * desk-back 의 JwtAuthenticationFilter 와 같은 규칙이다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 인증 필터 슬라이딩 갱신 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserService userService;
    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private static final String TOKEN = "hr-jwt-token";
    private static final String RENEWED = "hr-jwt-token-renewed";

    @BeforeEach
    void setUpHrTokenAuth() {
        // 유효한 HR 토큰으로 인증되는 공통 경로 — 각 테스트는 잔여 수명만 바꾼다.
        given(jwtTokenProvider.validateToken(TOKEN)).willReturn(true);
        given(jwtTokenProvider.isHrToken(TOKEN)).willReturn(true);
        given(jwtTokenProvider.getUserId(TOKEN)).willReturn("user1");
        given(jwtTokenProvider.getSsoUserRowIdFromHrToken(TOKEN)).willReturn(7L);
        given(jwtTokenProvider.getNameFromHrToken(TOKEN)).willReturn("사용자");
        given(jwtTokenProvider.getEmailFromHrToken(TOKEN)).willReturn("user1@test.com");
        given(jwtTokenProvider.getRolesFromHrToken(TOKEN)).willReturn(List.of("ROLE_USER"));
        given(jwtTokenProvider.getPermissionsFromHrToken(TOKEN)).willReturn(List.of());
        // 갱신 분기에서만 쓰이는 스텁 — 비갱신 테스트에서는 호출되지 않는다.
        lenient().when(jwtProperties.getHrAccessExpiration()).thenReturn(3_600_000L);
        lenient().when(jwtTokenProvider.createHrAccessToken(
                anyString(), any(), anyString(), anyString(), anyList(), anyList()))
            .thenReturn(RENEWED);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletResponse runFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("hr_access_token", TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Nested
    @DisplayName("잔여 수명이 임계(10분) 미만이면")
    class WhenExpiringSoon {

        @Test
        @DisplayName("성공 - 새 토큰을 hr_access_token 쿠키로 실어 준다")
        void renewsCookie() throws Exception {
            given(jwtTokenProvider.getRemainingExpiration(TOKEN)).willReturn(300_000L); // 5분

            MockHttpServletResponse response = runFilter();

            String setCookie = response.getHeader("Set-Cookie");
            assertThat(setCookie).contains("hr_access_token=" + RENEWED);
            assertThat(setCookie).contains("HttpOnly");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }
    }

    @Nested
    @DisplayName("잔여 수명이 넉넉하면")
    class WhenFresh {

        @Test
        @DisplayName("성공 - 쿠키를 갱신하지 않는다")
        void doesNotRenew() throws Exception {
            given(jwtTokenProvider.getRemainingExpiration(TOKEN)).willReturn(1_800_000L); // 30분

            MockHttpServletResponse response = runFilter();

            assertThat(response.getHeader("Set-Cookie")).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        }
    }

    @Nested
    @DisplayName("이미 만료를 지난 값이 오면")
    class WhenAlreadyExpired {

        @Test
        @DisplayName("성공 - 갱신하지 않는다 (죽은 토큰을 살려 주지 않는다)")
        void doesNotResurrect() throws Exception {
            given(jwtTokenProvider.getRemainingExpiration(TOKEN)).willReturn(0L);

            MockHttpServletResponse response = runFilter();

            assertThat(response.getHeader("Set-Cookie")).isNull();
        }
    }
}
