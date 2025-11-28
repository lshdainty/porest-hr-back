package com.lshdainty.porest.common.config.security;

import com.lshdainty.porest.common.config.properties.AppProperties;
import com.lshdainty.porest.security.handler.*;
import com.lshdainty.porest.security.service.CustomOAuth2UserService;
import com.lshdainty.porest.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService OAuth2Login;
    private final CustomUserDetailsService formLogin;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;
    private final CustomOAuth2AuthenticationFailureHandler customOAuth2AuthenticationFailureHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final RequestLoggingFilter requestLoggingFilter;
    private final AppProperties appProperties;

    // password 암호화를 위한 bean 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        // CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));

        // HTTP Basic 인증 방식 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        // 경로별 인가 작업
        http.authorizeHttpRequests((auth) -> auth
                // 인증 없이 접근 가능한 경로들
                .requestMatchers(
                        "/",
                        "/api/v1/login",                // 로그인
                        "/api/v1/login/check",          // 현재 로그인된 유저정보
                        "/api/v1/logout",               // 로그아웃
                        "/oauth2/**",            // OAuth2 시작 URL
                        "/login/oauth2/**",      // OAuth2 콜백 URL (중요!)
                        "/api/v1/encode-password",      // 비밀번호 인코딩 (개발용)
                        "/css/**",               // css
                        "/images/**",            // images
                        "/js/**",                // js
                        "/swagger-ui/**",        // Swagger UI
                        "/v3/api-docs/**"        // OpenAPI docs
                ).permitAll() // 해당 URL 패턴들은 모든 사용자가 접근 가능

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // Form 로그인 방식 설정
        http.formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/api/v1/login")
                        .usernameParameter("user_id")
                        .passwordParameter("user_pw")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .userDetailsService(formLogin);

        // OAuth2 로그인 방식 설정
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                        .userService(OAuth2Login) // OAuth 2 로그인 성공 이후 사용자 정보를 가져올 때의 설정
                )
                .successHandler(customOAuth2AuthenticationSuccessHandler) // OAuth2 전용 핸들러 적용
                .failureHandler(customOAuth2AuthenticationFailureHandler) // OAuth2 전용 핸들러 적용
        );

        //로그아웃 시 리다이렉트될 URL을 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/v1/logout", "POST"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(customLogoutSuccessHandler)
        );

        // 세션 정책: 세션 사용
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        // 인증되지 않은 사용자의 접근 처리
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthenticationEntryPoint)
        );

        // 요청 로깅 필터 추가 (Security Filter Chain 앞에 추가)
        http.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                appProperties.getFrontend().getBaseUrl()
        ));
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("PATCH");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}