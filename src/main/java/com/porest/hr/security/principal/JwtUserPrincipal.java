package com.porest.hr.security.principal;

import com.porest.hr.user.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JWT 인증 시 사용되는 UserPrincipal 구현체
 * JWT 토큰에서 추출한 사용자 ID로 HR DB에서 조회한 User 정보를 보관합니다.
 */
@Getter
@RequiredArgsConstructor
public class JwtUserPrincipal implements UserPrincipal {

    private final User user;

    @Override
    public User getUser() {
        return user;
    }
}
