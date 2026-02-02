package com.porest.hr.user.service;

import com.porest.core.type.YNType;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.service.dto.UserServiceDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 관리 서비스 인터페이스
 */
public interface UserService {

    /**
     * 사용자 생성
     *
     * @param data 사용자 생성 정보
     * @return 생성된 사용자 ID
     */
    String joinUser(UserServiceDto data);

    /**
     * 사용자 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 정보 DTO
     */
    UserServiceDto searchUser(String userId);

    /**
     * 전체 사용자 목록 조회
     *
     * @return 사용자 목록
     */
    List<UserServiceDto> searchUsers();

    /**
     * 사용자 정보 수정
     *
     * @param data 수정할 사용자 정보
     */
    void editUser(UserServiceDto data);

    /**
     * 사용자 삭제 (소프트 삭제)
     *
     * @param userId 삭제할 사용자 ID
     */
    void deleteUser(String userId);

    /**
     * 프로필 이미지를 임시 폴더에 저장
     *
     * @param file 프로필 이미지 파일
     * @return 저장된 파일 정보 (URL, UUID)
     */
    UserServiceDto saveProfileImgInTempFolder(MultipartFile file);

    /**
     * 사용자 존재 여부 확인 및 조회
     *
     * @param userId 사용자 ID
     * @return User 엔티티
     * @throws com.porest.core.exception.EntityNotFoundException 사용자가 존재하지 않는 경우
     */
    User checkUserExist(String userId);

    /**
     * 사용자 조회 (역할 및 권한 정보 포함)
     * 로그인 체크 시 최신 사용자 정보를 조회하기 위한 메서드
     *
     * @param userId 사용자 ID
     * @return User 엔티티 (역할 및 권한 정보 포함)
     * @throws com.porest.core.exception.EntityNotFoundException 사용자가 존재하지 않는 경우
     */
    User findUserById(String userId);

    /**
     * 프로필 URL에서 물리적 파일명을 추출
     *
     * @param profileUrl 프로필 URL
     * @return 물리적 파일명
     */
    String extractPhysicalFileNameFromUrl(String profileUrl);

    /**
     * 원본 파일명과 UUID로 프로필 URL 생성
     *
     * @param originalFilename 원본 파일명
     * @param uuid UUID
     * @return 프로필 URL
     */
    String generateProfileUrl(String originalFilename, String uuid);

    /**
     * 임시 폴더에 저장된 프로필 이미지를 관리용 폴더로 복사
     *
     * @param data 프로필 정보
     * @return 복사된 프로필 정보
     */
    UserServiceDto copyTempProfileToOrigin(UserServiceDto data);

    /**
     * 아이디 중복 체크
     *
     * @param userId 확인할 사용자 ID
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    boolean checkUserIdDuplicate(String userId);

    /**
     * 사용자의 메인 부서 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 메인 부서 존재 여부 (Y/N)
     */
    YNType checkUserHasMainDepartment(String userId);

    /**
     * 대시보드 데이터 수정
     *
     * @param userId 사용자 ID
     * @param dashboard 대시보드 JSON 데이터
     * @return 수정된 사용자 정보
     */
    UserServiceDto updateDashboard(String userId, String dashboard);

    /**
     * 특정 유저의 승인권자 목록 조회
     * 사용자의 메인 부서 기준으로 상위 부서장들을 승인권자로 반환합니다.
     *
     * @param userId 유저 ID
     * @return 승인권자 목록 (상위 부서장들)
     */
    List<UserServiceDto> getUserApprovers(String userId);

    /**
     * Spring Security용: 사용자 ID로 User 조회 (역할 및 권한 포함)
     * 2단계 쿼리로 MultipleBagFetchException 방지
     *
     * @param userId 사용자 ID
     * @return User Optional
     */
    Optional<User> getUserWithRolesById(String userId);

    /**
     * 전체 사용자 엔티티 목록 조회
     * N+1 문제 방지를 위해 DTO 변환 없이 User 엔티티 직접 반환
     *
     * @return 모든 User 엔티티 리스트
     */
    List<User> findAllUsers();
}
