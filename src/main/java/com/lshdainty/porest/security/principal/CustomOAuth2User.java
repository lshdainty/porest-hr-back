package com.lshdainty.porest.security.principal;

import com.lshdainty.porest.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * OAuth2 로그인 시 사용되는 Principal 구현체
 * OAuth2User와 UserPrincipal을 모두 구현하여 OAuth2 인증 정보와 User 엔티티를 함께 제공합니다.
 */
@Getter
public class CustomOAuth2User implements OAuth2User, UserPrincipal {
    private final User user;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nameAttributeKey;

    public CustomOAuth2User(User user, Map<String, Object> attributes,
                           Collection<? extends GrantedAuthority> authorities,
                           String nameAttributeKey) {
        this.user = user;
        this.attributes = attributes;
        this.authorities = authorities;
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return attributes.get(nameAttributeKey).toString();
    }

    // UserPrincipal 인터페이스 구현
    @Override
    public User getUser() {
        return user;
    }
}