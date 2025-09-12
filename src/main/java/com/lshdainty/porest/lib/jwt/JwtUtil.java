//package com.lshdainty.myhr.lib.jwt;
//
//import io.jsonwebtoken.Jwts;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
//@Slf4j
//@Component
//public class JwtUtil {
//    private SecretKey secretKey;
//
//    public JwtUtil(@Value("${jwt.secret}") String secret) {
//        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
//    }
//
//    public Long getUserName(String token) {
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userNo", Long.class);
//    }
//
//    public String getRole(String token) {
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
//    }
//
//    public Boolean isExpired(String token) {
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
//    }
//
//    public String generateToken(Long userNo, String role, Long expiredMs) {
//        return Jwts.builder()
//                .claim("userNo", userNo)
//                .claim("role", role)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis()+ expiredMs))
//                .signWith(secretKey)
//                .compact();
//    }
//}
