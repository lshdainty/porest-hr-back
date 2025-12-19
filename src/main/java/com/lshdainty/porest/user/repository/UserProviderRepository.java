package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.user.domain.UserProvider;

import java.util.List;
import java.util.Optional;

/**
 * UserProvider Repository Interface
 */
public interface UserProviderRepository {
    /**
     * 신규 유저 프로바이더 저장
     *
     * @param userProvider 저장할 유저 프로바이더
     */
    void save(UserProvider userProvider);

    /**
     * 프로바이더 타입과 프로바이더 ID로 유저 프로바이더 조회
     *
     * @param type 프로바이더 타입
     * @param id 프로바이더 ID
     * @return Optional&lt;UserProvider&gt;
     */
    Optional<UserProvider> findByProviderTypeAndProviderId(String type, String id);

    /**
     * 사용자 ID로 연동된 OAuth 제공자 목록 조회
     *
     * @param userId 사용자 ID
     * @return 연동된 제공자 목록
     */
    List<UserProvider> findByUserId(String userId);
}