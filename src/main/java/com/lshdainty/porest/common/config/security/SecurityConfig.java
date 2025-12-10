package com.lshdainty.porest.common.config.security;

import com.lshdainty.porest.common.config.properties.AppProperties;
import com.lshdainty.porest.security.filter.CsrfCookieFilter;
import com.lshdainty.porest.security.filter.IpBlockFilter;
import com.lshdainty.porest.security.handler.*;
import com.lshdainty.porest.security.service.CustomOAuth2UserService;
import com.lshdainty.porest.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
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
    private final AppProperties appProperties;
    private final IpBlockFilter ipBlockFilter;

    // password 암호화를 위한 bean 등록
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        // CSRF 활성화 및 Double Submit Cookie 패턴 설정
        // HttpOnly를 false로 설정하여 React(JavaScript)가 쿠키에서 토큰을 읽을 수 있도록 함
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        // Spring Security 6.1+ 권장: XorCsrfTokenRequestAttributeHandler 사용
        // 헤더와 파라미터 양쪽 모두 지원 (폼 로그인 + SPA 호환)
        XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
        delegate.setCsrfRequestAttributeName("_csrf");

        // SpaCsrfTokenRequestHandler: 첫 요청 시 CSRF 토큰을 자동으로 로드
        CsrfTokenRequestHandler requestHandler = delegate::handle;

        http.csrf(csrf -> csrf
                .csrfTokenRepository(tokenRepository)
                .csrfTokenRequestHandler(requestHandler)
        );

        // CORS 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));

        // HTTP Basic 인증 방식 활성화 (Prometheus 등 모니터링 도구용)
        // Form 로그인과 함께 사용되며, /actuator/** 경로에서 Basic Auth 지원
        http.httpBasic(basic -> basic.realmName("Porest Monitoring"));

        // 경로별 인가 작업
        http.authorizeHttpRequests((auth) -> auth
                // ========== 보안 위협 패턴 원천 차단 (AntPathRequestMatcher 사용) ==========
                // 숨김 파일/폴더 - 버전 관리 시스템
                .requestMatchers(
                        new AntPathRequestMatcher("/**/.git"),
                        new AntPathRequestMatcher("/**/.git/**"),
                        new AntPathRequestMatcher("/**/.svn"),
                        new AntPathRequestMatcher("/**/.svn/**"),
                        new AntPathRequestMatcher("/**/.hg"),
                        new AntPathRequestMatcher("/**/.hg/**"),
                        new AntPathRequestMatcher("/**/.bzr"),
                        new AntPathRequestMatcher("/**/.bzr/**")
                ).denyAll()

                // 숨김 파일/폴더 - 환경 변수 및 설정
                .requestMatchers(
                        new AntPathRequestMatcher("/**/.env"),
                        new AntPathRequestMatcher("/**/.env.*"),
                        new AntPathRequestMatcher("/**/.htaccess"),
                        new AntPathRequestMatcher("/**/web.config"),
                        new AntPathRequestMatcher("/**/php.ini"),
                        new AntPathRequestMatcher("/**/httpd.conf"),
                        new AntPathRequestMatcher("/**/nginx.conf")
                ).denyAll()

                // IDE 및 에디터 설정 파일
                .requestMatchers(
                        new AntPathRequestMatcher("/**/.idea"),
                        new AntPathRequestMatcher("/**/.idea/**"),
                        new AntPathRequestMatcher("/**/.vscode"),
                        new AntPathRequestMatcher("/**/.vscode/**"),
                        new AntPathRequestMatcher("/**/.eclipse"),
                        new AntPathRequestMatcher("/**/.eclipse/**"),
                        new AntPathRequestMatcher("/**/.settings"),
                        new AntPathRequestMatcher("/**/.settings/**"),
                        new AntPathRequestMatcher("/**/.classpath"),
                        new AntPathRequestMatcher("/**/.project"),
                        new AntPathRequestMatcher("/**/.DS_Store"),
                        new AntPathRequestMatcher("/**/Thumbs.db"),
                        new AntPathRequestMatcher("/**/desktop.ini")
                ).denyAll()

                // 백업 및 임시 파일 (OWASP 권장)
                .requestMatchers(
                        new AntPathRequestMatcher("/**/*.bak"),
                        new AntPathRequestMatcher("/**/*.backup"),
                        new AntPathRequestMatcher("/**/*.old"),
                        new AntPathRequestMatcher("/**/*.orig"),
                        new AntPathRequestMatcher("/**/*.tmp"),
                        new AntPathRequestMatcher("/**/*.temp"),
                        new AntPathRequestMatcher("/**/*.swp"),
                        new AntPathRequestMatcher("/**/*.swo"),
                        new AntPathRequestMatcher("/**/*.save"),
                        new AntPathRequestMatcher("/**/*~")
                ).denyAll()

                // 데이터베이스 및 설정 파일
                .requestMatchers(
                        new AntPathRequestMatcher("/**/*.sql"),
                        new AntPathRequestMatcher("/**/*.sqlite"),
                        new AntPathRequestMatcher("/**/*.db"),
                        new AntPathRequestMatcher("/**/config.php"),
                        new AntPathRequestMatcher("/**/config.yml"),
                        new AntPathRequestMatcher("/**/config.json"),
                        new AntPathRequestMatcher("/**/application.yml"),
                        new AntPathRequestMatcher("/**/application.properties"),
                        new AntPathRequestMatcher("/**/application-*.yml"),
                        new AntPathRequestMatcher("/**/application-*.properties")
                ).denyAll()

                // 압축 파일 (소스코드/백업 포함 가능성)
                .requestMatchers(
                        new AntPathRequestMatcher("/**/*.tar"),
                        new AntPathRequestMatcher("/**/*.tar.gz"),
                        new AntPathRequestMatcher("/**/*.tgz"),
                        new AntPathRequestMatcher("/**/*.zip"),
                        new AntPathRequestMatcher("/**/*.rar"),
                        new AntPathRequestMatcher("/**/*.7z")
                ).denyAll()

                // 공격 벡터 - 악성 스크립트
                .requestMatchers(
                        new AntPathRequestMatcher("/**/eval-stdin.php"),
                        new AntPathRequestMatcher("/**/shell.php"),
                        new AntPathRequestMatcher("/**/c99.php"),
                        new AntPathRequestMatcher("/**/r57.php"),
                        new AntPathRequestMatcher("/**/adminer.php"),
                        new AntPathRequestMatcher("/**/phpMyAdmin"),
                        new AntPathRequestMatcher("/**/phpMyAdmin/**"),
                        new AntPathRequestMatcher("/**/phpmyadmin"),
                        new AntPathRequestMatcher("/**/phpmyadmin/**"),
                        new AntPathRequestMatcher("/**/*.php"),
                        new AntPathRequestMatcher("/**/*.asp"),
                        new AntPathRequestMatcher("/**/*.aspx"),
                        new AntPathRequestMatcher("/**/*.jsp.bak"),
                        new AntPathRequestMatcher("/**/*.java.bak")
                ).denyAll()

                // 로그 파일 (민감 정보 포함 가능)
                .requestMatchers(
                        new AntPathRequestMatcher("/**/*.log"),
                        new AntPathRequestMatcher("/**/logs"),
                        new AntPathRequestMatcher("/**/logs/**")
                ).denyAll()

                // ========== 정상 접근 허용 경로 ==========
                // 인증 없이 접근 가능한 경로들
                .requestMatchers(
                        "/",
                        "/api/v1/csrf-token",   // CSRF 토큰 발급 (React 앱 시작 시 필요)
                        "/api/v1/login",        // 로그인
                        "/api/v1/login/check",  // 현재 로그인된 유저정보
                        "/api/v1/logout",       // 로그아웃
                        "/oauth2/**",           // OAuth2 시작 URL
                        "/login/oauth2/**",     // OAuth2 콜백 URL (중요!)
                        "/css/**",              // css
                        "/images/**",           // images
                        "/js/**",               // js
                        "/actuator/health"      // Health check (liveness/readiness probe용)
                ).permitAll() // 해당 URL 패턴들은 모든 사용자가 접근 가능

                // Prometheus 메트릭스는 인증 필요 (모니터링 시스템 계정으로 접근)
                .requestMatchers("/actuator/prometheus", "/actuator/metrics/**")
                .authenticated()

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

        // IP 블랙리스트 필터 추가
        // UsernamePasswordAuthenticationFilter 앞에서 실행하여 인증 전에 IP 차단
        http.addFilterBefore(ipBlockFilter, UsernamePasswordAuthenticationFilter.class);

        // CSRF 토큰을 즉시 로드하는 필터 추가
        // CsrfFilter 이후에 실행하여 토큰을 즉시 쿠키에 저장
        http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

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