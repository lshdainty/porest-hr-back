//package com.lshdainty.myhr.lib.jwt;
//
//import com.lshdainty.myhr.domain.User;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@AllArgsConstructor
//@Slf4j
//public class JwtFilter extends OncePerRequestFilter {
//    private final JwtUtil jwtUtil;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain) throws ServletException, IOException {
//        // request header check
//        String authHeader = req.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(req, resp);
//            return;
//        }
//
//        String token = authHeader.split(" ")[1];
//
//        // 소멸 시간 검증
//        if (jwtUtil.isExpired(token)) {
//            filterChain.doFilter(req, resp);
//            return;
//        }
//
//        // user 정보 획득
//        Long userNo = jwtUtil.getUserName(token);
//
//        User user = User.createUser(userNo);
//
//        // userDetails에 회원 정보 객체 담기
//        CustomUserDetails customUserDetails = new CustomUserDetails(user);
//
//        // 스프링 시큐리티 인증 토큰 생성
//        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
//
//        // 세션에 사용자 등록
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(req, resp);
//    }
//}
