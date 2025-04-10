//package com.lshdainty.myhr.lib.jwt;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lshdainty.myhr.dto.LoginDto;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.util.StreamUtils;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Collection;
//import java.util.Iterator;
//
//@Slf4j
//public class LoginFilter extends UsernamePasswordAuthenticationFilter {
//    private final AuthenticationManager authenticationManager;
//    private final JwtUtil jwtUtil;
//
//    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
//        this.authenticationManager = authenticationManager;
//        this.jwtUtil = jwtUtil;
//        this.setFilterProcessesUrl("/jwt/login");
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse resp) throws AuthenticationException {
//        // 클라이언트 요청에서 (userName, pw) 추출
//        LoginDto loginDto = null;
//        try {
//            String requestBody = StreamUtils.copyToString(req.getInputStream(), StandardCharsets.UTF_8);
//            loginDto = new ObjectMapper().readValue(requestBody, LoginDto.class);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        Long userNo = loginDto.getId();
//        String password = loginDto.getPw();
//
//        log.info("Attempting to authenticate user {}", userNo);
//        log.info("Attempting to authenticate password {}", password);
//
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userNo, password, null);  //  추후 (null) role 권한 추가되면 추가할 것
//
//        return authenticationManager.authenticate(token);
//    }
//
//    // 로그인 성공 시 실행하는 메소드
//    @Override
//    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse resp, FilterChain chain, Authentication authResult) throws IOException, ServletException {
//        log.info("Successfully authenticated user {}", authResult.getPrincipal());
//
//        CustomUserDetails principal = (CustomUserDetails) authResult.getPrincipal();
//        Long userNo = principal.getUserNo();
//
//        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
//        Iterator<? extends GrantedAuthority> authoritiesIterator = authorities.iterator();
//        GrantedAuthority authority = authoritiesIterator.next();
//
//        String role = authority.getAuthority();
//
//        String token = jwtUtil.generateToken(userNo, role, 60*60*1000L);
//
//        resp.addHeader("Authorization", "Bearer " + token);
//    }
//
//    // 로그인 실패 시 실행하는 메소드
//    @Override
//    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse resp, AuthenticationException failed) throws IOException, ServletException {
//        log.info("Unsuccessful authentication");
//        resp.setStatus(401);
//    }
//}
