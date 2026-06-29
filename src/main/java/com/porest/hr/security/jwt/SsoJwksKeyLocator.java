package com.porest.hr.security.jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import com.porest.hr.common.config.properties.JwtProperties;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.LocatorAdapter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.Key;
import java.util.List;

/**
 * SSO 토큰(RS256) 서명 키를 SSO JWKS 엔드포인트에서 kid 로 조회하는 jjwt KeyLocator.
 *
 * <p>SSO 가 private key 로 서명하고 {@code /.well-known/jwks.json} 으로 public key(JWK Set)를
 * 노출하므로, HR 은 토큰 헤더의 kid 에 해당하는 RSA public key 를 받아 검증만 한다(위조 불가).
 * JWKSource 는 원격 fetch 결과를 캐시한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SsoJwksKeyLocator extends LocatorAdapter<Key> {

    private final JwtProperties jwtProperties;
    private JWKSource<SecurityContext> jwkSource;

    @PostConstruct
    public void init() {
        try {
            // create(URL) 은 캐시 포함 기본 설정. 시작 시 fetch 하지 않고 첫 조회 때 가져온다.
            this.jwkSource = JWKSourceBuilder
                    .create(new URL(jwtProperties.getSsoJwksUri()))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid SSO JWKS URI: " + jwtProperties.getSsoJwksUri(), e);
        }
    }

    @Override
    protected Key locate(JwsHeader header) {
        String kid = header.getKeyId();
        try {
            JWKMatcher.Builder matcher = new JWKMatcher.Builder().keyType(KeyType.RSA);
            if (kid != null) {
                matcher.keyID(kid);
            }
            List<JWK> jwks = jwkSource.get(new JWKSelector(matcher.build()), null);
            if (jwks.isEmpty()) {
                throw new JwtException("No matching SSO JWK (kid=" + kid + ")");
            }
            return jwks.get(0).toRSAKey().toRSAPublicKey();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Failed to resolve SSO signing key from JWKS", e);
        }
    }
}
