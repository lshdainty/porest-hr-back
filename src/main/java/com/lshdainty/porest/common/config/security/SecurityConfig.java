package com.lshdainty.porest.common.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.login.controller.dto.LoginDto;
import com.lshdainty.porest.login.service.CustomOAuth2UserService;
import com.lshdainty.porest.login.service.CustomUserDetailsService;
import com.lshdainty.porest.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService OAuth2Login;
    private final CustomUserDetailsService formLogin;
    private final ObjectMapper objectMapper;

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
                        "/login",           // 로그인
                        "/logout",          // 로그아웃
                        "/oauth2/**",       // OAuth2 로그인
                        "/encode-password", // 비밀번호 인코딩 (개발용)
                        "/css/**",          // css
                        "/images/**",       // images
                        "/js/**"            // js
                ).permitAll() // 해당 URL 패턴들은 모든 사용자가 접근 가능

                // "/api/v1/**" 패턴의 URL은 USER 권한을 가진 사용자만 접근 가능
//                .requestMatchers("/api/v1/**").hasRole(Role.USER.name())

                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // Form 로그인 방식 설정
        http.formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("user_id")
                        .passwordParameter("user_pw")
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                .userDetailsService(formLogin);

        // OAuth2 로그인 방식 설정
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                        .userService(OAuth2Login) // OAuth 2 로그인 성공 이후 사용자 정보를 가져올 때의 설정
                )
        );

        //로그아웃 시 리다이렉트될 URL을 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(logoutSuccessHandler())
        );

        // 세션 정책: 세션 사용
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:*");
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

    // 로그인 성공 핸들러
    private AuthenticationSuccessHandler loginSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            CustomUserDetailsService.CustomUserDetails userDetails =
                    (CustomUserDetailsService.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            LoginDto loginData = LoginDto.builder()
                    .user_id(user.getId())
                    .user_name(user.getName())
                    .user_email(user.getEmail())
                    .user_role(user.getRole().name())
                    .build();

            ApiResponse<LoginDto> apiResponse = ApiResponse.success(loginData);
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        };
    }

    // 로그인 실패 핸들러
    private AuthenticationFailureHandler loginFailureHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Void> apiResponse = ApiResponse.fail(401, "로그인 실패: " + exception.getMessage());
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        };
    }

    // 로그아웃 성공 핸들러
    private LogoutSuccessHandler logoutSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String message = "로그아웃 성공";
            ApiResponse<String> apiResponse = ApiResponse.success(message);
            String jsonResponse = objectMapper.writeValueAsString(apiResponse);
            response.getWriter().write(jsonResponse);
        };
    }
}