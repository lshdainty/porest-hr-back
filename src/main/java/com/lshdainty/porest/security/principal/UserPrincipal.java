package com.lshdainty.porest.security.principal;

import com.lshdainty.porest.user.domain.User;

/**
 * Form 로그인과 OAuth2 로그인의 공통 인터페이스
 * 두 가지 인증 방식 모두에서 User 객체에 접근할 수 있도록 합니다.
 */
public interface UserPrincipal {
    /**
     * 인증된 사용자 정보를 반환합니다.
     * @return User 엔티티
     */
    User getUser();
}