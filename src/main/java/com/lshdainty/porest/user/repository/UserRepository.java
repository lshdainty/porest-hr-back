package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    // 신규 사용자 저장
    void save(User user);
    // userId로 단일 유저 검색
    Optional<User> findById(String userId);
    // userId로 유저 검색 (역할과 권한 정보 포함)
    Optional<User> findByIdWithRolesAndPermissions(String userId);
    // 전체 유저 목록 조회
    List<User> findUsers();
    // 초대 토큰으로 유저 검색
    Optional<User> findByInvitationToken(String token);
}
