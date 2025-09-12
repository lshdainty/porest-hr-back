//package com.lshdainty.myhr.Config;
//
//import com.lshdainty.myhr.lib.jwt.JwtFilter;
//import com.lshdainty.myhr.lib.jwt.JwtUtil;
//import com.lshdainty.myhr.lib.jwt.LoginFilter;
//import lombok.AllArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@AllArgsConstructor
//public class SecurityConfig {
//    private final AuthenticationConfiguration authConfig;
//    private final JwtUtil jwtUtil;
//
//    // LoginFilter에 필요한 인증 메니저 주입
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
//
//    // password 암호화를 위한 bean 등록
//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        // csrf disable
//        // JWT는 session 상태를 stateless 상태로 관리함
//        http.csrf(AbstractHttpConfigurer::disable);
//
//        // Form 로그인 방식 disable
//        // api 서비스만 제공하므로 form방식 제거
//        http.formLogin(AbstractHttpConfigurer::disable);
//
//        // http basic 인증 방식 disable
//        http.httpBasic(AbstractHttpConfigurer::disable);
//
//        // 경로별 인가 작업
//        http.authorizeHttpRequests((auth) -> auth
//                .requestMatchers("/", "/login", "/join", "/jwt/login", "/jwt/convert/pw").permitAll() // 모든 권한 사용자 가능
//                .anyRequest().authenticated()   // 권한에 따른 접근
//        );
//
//        http.addFilterBefore(new JwtFilter(jwtUtil), LoginFilter.class);
//
//        // LoginFilter 추가 작업
//        /*
//        * addFilterAt은 특정 위치에 커스텀 filter를 추가하는 작업
//        * 자체적으로 생성한 JWT의 LoginFilter를
//        * 기존 FormLogin 방식이 사용하는 UsernamePasswordAuthenticationFilter 위치에 추가하는 작업
//        * */
//        http.addFilterAt(new LoginFilter(authenticationManager(authConfig), jwtUtil), UsernamePasswordAuthenticationFilter.class);
//
//        // http 세션 설정 (중요)
//        http.sessionManagement((session) -> session
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//
//        return http.build();
//    }
//}
