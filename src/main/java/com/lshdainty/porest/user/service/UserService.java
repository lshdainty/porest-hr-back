package com.lshdainty.porest.user.service;

import com.lshdainty.porest.common.message.MessageKey;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.common.util.MessageResolver;
import com.lshdainty.porest.common.util.PorestFile;
import com.lshdainty.porest.department.repository.DepartmentRepository;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.RoleRepository;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepository;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import com.lshdainty.porest.user.type.StatusType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    private final MessageResolver messageResolver;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final DepartmentRepository departmentRepository;
    private final EntityManager em;

    @Value("${file.root-path}")
    private String fileRootPath;

    @Value("${file.web-url-prefix}")
    private String webUrlPrefix;

    @Value("${file.temp.path.profile}")
    private String tempPath;

    @Value("${file.origin.path.profile}")
    private String originPath;

    @Transactional
    public String joinUser(UserServiceDto data) {
        log.debug("사용자 생성 시작: id={}, name={}, email={}", data.getId(), data.getName(), data.getEmail());

        UserServiceDto profileDto = UserServiceDto.builder().build();
        if (StringUtils.hasText(data.getProfileUUID()) && StringUtils.hasText(data.getProfileUrl())) {
            log.debug("프로필 이미지 복사 진행: uuid={}", data.getProfileUUID());
            profileDto = copyTempProfileToOrigin(data);
        }

        User user = User.createUser(
                data.getId(),
                data.getPwd(),
                data.getName(),
                data.getEmail(),
                data.getBirth(),
                data.getCompany(),
                data.getWorkTime(),
                data.getLunarYN(),
                profileDto.getProfileName(),
                profileDto.getProfileUUID()
        );

        userRepository.save(user);
        log.info("사용자 생성 완료: id={}", user.getId());
        return user.getId();
    }

    public UserServiceDto searchUser(String userId) {
        log.debug("사용자 조회: userId={}", userId);
        // 역할 및 권한 정보를 포함하여 조회
        User user = findUserById(userId);

        // 메인 부서의 한글명 조회
        String mainDepartmentNameKR = user.getUserDepartments().stream()
                .filter(ud -> YNType.isY(ud.getMainYN()) && YNType.isN(ud.getIsDeleted()))
                .findFirst()
                .map(ud -> ud.getDepartment().getNameKR())
                .orElse(null);

        // 역할 상세 정보 생성
        List<UserServiceDto.RoleDetailDto> roleDetails = user.getRoles().stream()
                .map(role -> UserServiceDto.RoleDetailDto.builder()
                        .roleCode(role.getCode())
                        .roleName(role.getName())
                        .permissions(role.getPermissions().stream()
                                .map(permission -> UserServiceDto.PermissionDetailDto.builder()
                                        .permissionCode(permission.getCode())
                                        .permissionName(permission.getName())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return UserServiceDto.builder()
                .id(user.getId())
                .pwd(user.getPwd())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roleDetails)
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .allPermissions(user.getAllAuthorities())
                .birth(user.getBirth())
                .workTime(user.getWorkTime())
                .joinDate(user.getJoinDate())
                .company(user.getCompany())
                .lunarYN(user.getLunarYN())
                .profileName(user.getProfileName())
                .profileUrl(StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                        generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null)
                // profileUUID는 프론트엔드에 노출하지 않음
                .invitationToken(user.getInvitationToken())
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .registeredAt(user.getRegisteredAt())
                .mainDepartmentNameKR(mainDepartmentNameKR)
                .dashboard(user.getDashboard())
                .build();
    }


    public List<UserServiceDto> searchUsers() {
        log.debug("전체 사용자 목록 조회 시작");
        // 역할 및 권한 정보를 포함하여 조회
        List<User> users = userRepository.findUsersWithRolesAndPermissions();
        log.debug("전체 사용자 목록 조회 완료: count={}", users.size());

        return users.stream()
                .map(user -> {
                    // 메인 부서의 한글명 조회
                    String mainDepartmentNameKR = user.getUserDepartments().stream()
                            .filter(ud -> YNType.isY(ud.getMainYN()) && YNType.isN(ud.getIsDeleted()))
                            .findFirst()
                            .map(ud -> ud.getDepartment().getNameKR())
                            .orElse(null);

                    // 역할 상세 정보 생성
                    List<UserServiceDto.RoleDetailDto> roleDetails = user.getRoles().stream()
                            .map(role -> UserServiceDto.RoleDetailDto.builder()
                                    .roleCode(role.getCode())
                                    .roleName(role.getName())
                                    .permissions(role.getPermissions().stream()
                                            .map(permission -> UserServiceDto.PermissionDetailDto.builder()
                                                    .permissionCode(permission.getCode())
                                                    .permissionName(permission.getName())
                                                    .build())
                                            .collect(Collectors.toList()))
                                    .build())
                            .collect(Collectors.toList());

                    return UserServiceDto.builder()
                            .id(user.getId())
                            .pwd(user.getPwd())
                            .name(user.getName())
                            .email(user.getEmail())
                            .roles(roleDetails)
                            .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                            .allPermissions(user.getAllAuthorities())
                            .birth(user.getBirth())
                            .workTime(user.getWorkTime())
                            .joinDate(user.getJoinDate())
                            .company(user.getCompany())
                            .lunarYN(user.getLunarYN())
                            .profileName(user.getProfileName())
                            .profileUrl(StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                                    generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null)
                            // profileUUID는 프론트엔드에 노출하지 않음
                            .invitationToken(user.getInvitationToken())
                            .invitationSentAt(user.getInvitationSentAt())
                            .invitationExpiresAt(user.getInvitationExpiresAt())
                            .invitationStatus(user.getInvitationStatus())
                            .registeredAt(user.getRegisteredAt())
                            .mainDepartmentNameKR(mainDepartmentNameKR)
                            .dashboard(user.getDashboard())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void editUser(UserServiceDto data) {
        log.debug("사용자 수정 시작: id={}", data.getId());
        User user = checkUserExist(data.getId());

        UserServiceDto profileDto = UserServiceDto.builder().build();
        if (StringUtils.hasText(data.getProfileUUID()) && !data.getProfileUUID().equals(user.getProfileUUID())) {
            log.debug("프로필 이미지 변경: uuid={}", data.getProfileUUID());
            profileDto = copyTempProfileToOrigin(data);
        }

        List<Role> roles = null;
        if (data.getRoleNames() != null) {
            // 역할 코드로 조회 (프론트에서 역할 코드를 전송)
            roles = data.getRoleNames().stream()
                    .map(code -> roleRepository.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Role not found: " + code)))
                    .collect(Collectors.toList());
        }

        user.updateUser(data.getName(), data.getEmail(), roles, data.getBirth(),
                data.getCompany(),
                data.getWorkTime(),
                data.getLunarYN(),
                profileDto.getProfileName(),
                profileDto.getProfileUUID(),
                data.getDashboard()
        );
        log.info("사용자 수정 완료: id={}", data.getId());
    }

    @Transactional
    public void deleteUser(String userId) {
        log.debug("사용자 삭제 시작: userId={}", userId);
        User user = checkUserExist(userId);
        user.deleteUser();
        log.info("사용자 삭제 완료: userId={}", userId);
    }

    public UserServiceDto saveProfileImgInTempFolder(MultipartFile file) {
        log.debug("프로필 이미지 임시 저장 시작: filename={}", file.getOriginalFilename());
        // 원본 파일명 추출
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String uuid = UUID.randomUUID().toString();

        // PorestFile의 static 메소드를 사용하여 물리적 파일명 생성
        String physicalFilename = PorestFile.generatePhysicalFilename(originalFilename, uuid);

        // 임시 폴더에 물리적 파일명으로 저장
        PorestFile.save(file, tempPath, physicalFilename, messageResolver);

        // 저장된 파일의 절대 경로 생성 (물리적 파일명 기준)
        String absolutePath = Paths.get(tempPath, physicalFilename).toString();

        log.debug("프로필 이미지 임시 저장 완료: uuid={}, path={}", uuid, absolutePath);
        return UserServiceDto.builder()
                .profileUrl(absolutePath.replace(fileRootPath, webUrlPrefix))
                .profileUUID(uuid)
                .build();
    }

    public User checkUserExist(String userId) {
        Optional<User> findUser = userRepository.findById(userId);
        if ((findUser.isEmpty()) || YNType.isY(findUser.get().getIsDeleted())) {
            log.warn("사용자 조회 실패 - 존재하지 않거나 삭제된 사용자: userId={}", userId);
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.NOT_FOUND_USER));
        }
        return findUser.get();
    }

    /**
     * 사용자 조회 (역할 및 권한 정보 포함)
     * 로그인 체크 시 최신 사용자 정보를 조회하기 위한 메서드
     *
     * @param userId 사용자 ID
     * @return User 엔티티 (역할 및 권한 정보 포함)
     */
    public User findUserById(String userId) {
        Optional<User> findUser = userRepository.findByIdWithRolesAndPermissions(userId);
        if ((findUser.isEmpty()) || YNType.isY(findUser.get().getIsDeleted())) {
            log.warn("사용자 조회 실패 - 존재하지 않거나 삭제된 사용자: userId={}", userId);
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.NOT_FOUND_USER));
        }
        return findUser.get();
    }

    /**
     * 프로필 URL에서 물리적 파일명을 추출하는 헬퍼 메소드
     */
    public String extractPhysicalFileNameFromUrl(String profileUrl) {
        if (!StringUtils.hasText(profileUrl)) {
            return null;
        }

        String relativePath = profileUrl.replace(webUrlPrefix, "");
        Path path = Paths.get(relativePath);
        return path.getFileName().toString();
    }

    /**
     * 원본 파일명과 UUID로 프로필 URL 생성
     */
    public String generateProfileUrl(String originalFilename, String uuid) {
        // PorestFile의 static 메소드를 사용하여 물리적 파일명 생성
        String physicalFilename = PorestFile.generatePhysicalFilename(originalFilename, uuid);
        if (physicalFilename == null) {
            return null;
        }

        String absolutePath = Paths.get(originPath, physicalFilename).toString();
        return absolutePath.replace(fileRootPath, webUrlPrefix);
    }

    /**
     * 임시 폴더에 저장된 프로필 이미지를 관리용 폴더로 복사하는 헬퍼 메소드
     */
    public UserServiceDto copyTempProfileToOrigin(UserServiceDto data) {
        String profileName = null;
        String profileUUID = null;

        String physicalFileName = extractPhysicalFileNameFromUrl(data.getProfileUrl());
        if (physicalFileName != null) {
            String tempFilePath = Paths.get(tempPath, physicalFileName).toString();
            String originFilePath = Paths.get(originPath, physicalFileName).toString();

            if (PorestFile.copy(tempFilePath, originFilePath, messageResolver)) {
                // PorestFile의 static 메소드를 사용하여 원본 파일명 추출
                profileName = PorestFile.extractOriginalFilename(physicalFileName, null);
                profileUUID = data.getProfileUUID();
            }
        }

        return UserServiceDto.builder()
                .profileName(profileName) // 추출된 원본 파일명
                .profileUUID(profileUUID)
                .build();
    }

    /**
     * 관리자가 사용자를 초대하여 생성
     */
    @Transactional
    public UserServiceDto inviteUser(UserServiceDto data) {
        log.debug("사용자 초대 시작: id={}, name={}, email={}", data.getId(), data.getName(), data.getEmail());
        // 아이디 중복 체크
        if (checkUserIdDuplicate(data.getId())) {
            log.warn("사용자 초대 실패 - 중복 아이디: id={}", data.getId());
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VALIDATE_DUPLICATE_USER_ID));
        }

        User user = User.createInvitedUser(
                data.getId(),
                data.getName(),
                data.getEmail(),
                data.getCompany(),
                data.getWorkTime(),
                data.getJoinDate()
        );

        userRepository.save(user);

        // 초대 이메일 발송
        emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());
        log.info("사용자 초대 완료: id={}, email={}", user.getId(), user.getEmail());

        return UserServiceDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .workTime(user.getWorkTime())
                .joinDate(user.getJoinDate())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }

    /**
     * 초대 이메일 재전송
     */
    @Transactional
    public UserServiceDto resendInvitation(String userId) {
        log.debug("초대 이메일 재전송 시작: userId={}", userId);
        User user = checkUserExist(userId);
        user.renewInvitationToken();

        // 초대 이메일 재발송
        emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());
        log.info("초대 이메일 재전송 완료: userId={}, email={}", userId, user.getEmail());

        return UserServiceDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .workTime(user.getWorkTime())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }

    /**
     * 초대된 사용자 정보 수정
     */
    @Transactional
    public UserServiceDto editInvitedUser(String userId, UserServiceDto data) {
        log.debug("초대된 사용자 정보 수정 시작: userId={}", userId);
        User user = checkUserExist(userId);

        // PENDING 상태인지 확인
        if (user.getInvitationStatus() != StatusType.PENDING) {
            log.warn("초대된 사용자 수정 실패 - PENDING 상태가 아님: userId={}, status={}", userId, user.getInvitationStatus());
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VALIDATE_NOT_PENDING_USER));
        }

        // 이메일 변경 여부 확인
        String oldEmail = user.getEmail();
        boolean emailChanged = data.getEmail() != null && !data.getEmail().equals(oldEmail);

        user.updateInvitedUser(
                data.getName(),
                data.getEmail(),
                data.getCompany(),
                data.getWorkTime(),
                data.getJoinDate()
        );

        // 이메일이 변경된 경우 초대 이메일 재전송
        if (emailChanged) {
            log.debug("이메일 변경으로 초대 이메일 재전송: userId={}, oldEmail={}, newEmail={}", userId, oldEmail, data.getEmail());
            emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());
        }
        log.info("초대된 사용자 정보 수정 완료: userId={}", userId);

        return UserServiceDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .workTime(user.getWorkTime())
                .joinDate(user.getJoinDate())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }

    /**
     * 사용자가 초대를 수락하고 회원가입 완료
     */
    @Transactional
    public String completeInvitedUserRegistration(UserServiceDto data) {
        log.debug("초대 수락 및 회원가입 시작: token={}", data.getInvitationToken());
        Optional<User> findUser = userRepository.findByInvitationToken(data.getInvitationToken());
        if (findUser.isEmpty()) {
            log.warn("회원가입 실패 - 초대 토큰 없음: token={}", data.getInvitationToken());
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.NOT_FOUND_INVITATION));
        }

        User user = findUser.get();
        if (!user.isInvitationValid()) {
            log.warn("회원가입 실패 - 초대 만료: userId={}", user.getId());
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VALIDATE_EXPIRED_INVITATION));
        }

        user.completeRegistration(
                data.getBirth(),
                data.getLunarYN()
        );
        log.info("회원가입 완료: userId={}", user.getId());

        return user.getId();
    }

    /**
     * 아이디 중복 체크
     */
    public boolean checkUserIdDuplicate(String userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        // userId가 PK이므로 삭제 여부와 관계없이 존재하면 중복으로 판단
        return existingUser.isPresent();
    }

    /**
     * 사용자의 메인 부서 존재 여부 확인
     */
    public YNType checkUserHasMainDepartment(String userId) {
        // 유저 존재 여부 확인
        checkUserExist(userId);

        // 메인 부서 존재 여부 확인
        boolean hasMainDepartment = departmentRepository.hasMainDepartment(userId);

        return hasMainDepartment ? YNType.Y : YNType.N;
    }

    /**
     * 대시보드 데이터 수정
     */
    @Transactional
    public UserServiceDto updateDashboard(String userId, String dashboard) {
        log.debug("대시보드 수정 시작: userId={}", userId);
        User user = checkUserExist(userId);
        user.updateDashboard(dashboard);
        log.info("대시보드 수정 완료: userId={}", userId);

        return UserServiceDto.builder()
                .id(user.getId())
                .dashboard(user.getDashboard())
                .build();
    }

    /**
     * 특정 유저의 승인권자 목록 조회
     *
     * 사용자의 메인 부서 기준으로 상위 부서장들을 승인권자로 반환합니다.
     *
     * @param userId 유저 ID
     * @return 승인권자 목록 (상위 부서장들)
     */
    public List<UserServiceDto> getUserApprovers(String userId) {
        log.debug("승인권자 목록 조회 시작: userId={}", userId);
        // 유저 존재 확인
        checkUserExist(userId);

        // 상위 부서장 목록 조회
        List<com.lshdainty.porest.department.domain.Department> approverDepartments =
                departmentRepository.findApproversByUserId(userId);
        log.debug("승인권자 목록 조회 완료: userId={}, count={}", userId, approverDepartments.size());

        // Department 정보를 UserServiceDto로 변환
        return approverDepartments.stream()
                .map(dept -> {
                    // headUserId로 User 조회 (역할 및 권한 정보 포함)
                    User approver = userRepository.findByIdWithRolesAndPermissions(dept.getHeadUserId())
                            .orElse(null);

                    if (approver == null || YNType.isY(approver.getIsDeleted())) {
                        return null;
                    }

                    // 역할 상세 정보 생성
                    List<UserServiceDto.RoleDetailDto> roleDetails = approver.getRoles().stream()
                            .map(role -> UserServiceDto.RoleDetailDto.builder()
                                    .roleCode(role.getCode())
                                    .roleName(role.getName())
                                    .permissions(role.getPermissions().stream()
                                            .map(permission -> UserServiceDto.PermissionDetailDto.builder()
                                                    .permissionCode(permission.getCode())
                                                    .permissionName(permission.getName())
                                                    .build())
                                            .collect(Collectors.toList()))
                                    .build())
                            .collect(Collectors.toList());

                    return UserServiceDto.builder()
                            .id(approver.getId())
                            .name(approver.getName())
                            .email(approver.getEmail())
                            .roles(roleDetails)
                            .roleNames(approver.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                            .allPermissions(approver.getAllAuthorities())
                            .departmentId(dept.getId())
                            .departmentName(dept.getName())
                            .departmentNameKR(dept.getNameKR())
                            .departmentLevel(dept.getLevel())
                            .build();
                })
                .filter(dto -> dto != null) // null 제거
                .collect(Collectors.toList());
    }

    /**
     * Spring Security용: 사용자 ID로 User 조회 (역할 및 권한 포함)
     * 2단계 쿼리로 MultipleBagFetchException 방지
     */
    public Optional<User> getUserWithRolesById(String userId) {
        // 1단계: User + userRoles + role 조회
        List<User> result = em.createQuery(
                "select distinct u from User u " +
                "left join fetch u.userRoles ur " +
                "left join fetch ur.role r " +
                "where u.id = :userId and u.isDeleted = :isDeleted",
                User.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        User user = result.get(0);

        // 2단계: rolePermissions + permission 초기화
        if (!user.getUserRoles().isEmpty()) {
            em.createQuery(
                "select distinct r from Role r " +
                "left join fetch r.rolePermissions rp " +
                "left join fetch rp.permission p " +
                "where r in :roles",
                Role.class)
                .setParameter("roles", user.getRoles())
                .getResultList();
        }

        return Optional.of(user);
    }

    /**
     * Spring Security용: 초대 토큰으로 User 조회 (역할 및 권한 포함)
     * 2단계 쿼리로 MultipleBagFetchException 방지
     */
    public Optional<User> getUserWithRolesByInvitationToken(String token) {
        // 1단계: User + userRoles + role 조회
        List<User> result = em.createQuery(
                "select distinct u from User u " +
                "left join fetch u.userRoles ur " +
                "left join fetch ur.role r " +
                "where u.invitationToken = :token and u.isDeleted = :isDeleted",
                User.class)
                .setParameter("token", token)
                .setParameter("isDeleted", YNType.N)
                .getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        User user = result.get(0);

        // 2단계: rolePermissions + permission 초기화
        if (!user.getUserRoles().isEmpty()) {
            em.createQuery(
                "select distinct r from Role r " +
                "left join fetch r.rolePermissions rp " +
                "left join fetch rp.permission p " +
                "where r in :roles",
                Role.class)
                .setParameter("roles", user.getRoles())
                .getResultList();
        }

        return Optional.of(user);
    }
}