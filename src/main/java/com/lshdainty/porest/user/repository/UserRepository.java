package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 */
public interface UserRepository {
    /**
     * 신규 사용자 저장
     *
     * @param user 저장할 사용자
     */
    void save(User user);

    /**
     * SSO User No로 단일 유저 검색
     *
     * @param ssoUserNo SSO에서 발급한 사용자 순번
     * @return Optional&lt;User&gt;
     */
    Optional<User> findBySsoUserNo(Long ssoUserNo);

    /**
     * userId로 단일 유저 검색
     *
     * @param userId 유저 ID
     * @return Optional&lt;User&gt;
     */
    Optional<User> findById(String userId);

    /**
     * userId로 유저 검색 (역할과 권한 정보 포함)
     *
     * @param userId 유저 ID
     * @return Optional&lt;User&gt;
     */
    Optional<User> findByIdWithRolesAndPermissions(String userId);

    /**
     * 전체 유저 목록 조회
     *
     * @return List&lt;User&gt;
     */
    List<User> findUsers();

    /**
     * 전체 유저 목록 조회 (역할과 권한 정보 포함)
     *
     * @return List&lt;User&gt;
     */
    List<User> findUsersWithRolesAndPermissions();

    /**
     * 삭제된 유저 중 modifyDate가 특정 기간 내인 유저 조회
     *
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return List&lt;User&gt;
     */
    List<User> findDeletedUsersByModifyDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
