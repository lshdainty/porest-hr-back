package com.lshdainty.porest.common.config.security;

import com.lshdainty.porest.security.handler.CustomAuthenticationFailureHandler;
import com.lshdainty.porest.security.handler.CustomAuthenticationSuccessHandler;
import com.lshdainty.porest.security.handler.CustomLogoutSuccessHandler;
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
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

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
        );

        //로그아웃 시 리다이렉트될 URL을 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(customLogoutSuccessHandler)
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
}