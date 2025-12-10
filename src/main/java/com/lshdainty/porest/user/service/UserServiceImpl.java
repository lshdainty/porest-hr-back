package com.lshdainty.porest.user.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
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
public class UserServiceImpl implements UserService {
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

    @Override
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
                profileDto.getProfileUUID(),
                data.getCountryCode()
        );

        userRepository.save(user);
        log.info("사용자 생성 완료: id={}", user.getId());
        return user.getId();
    }

    @Override
    public UserServiceDto searchUser(String userId) {
        log.debug("사용자 조회: userId={}", userId);
        User user = findUserById(userId);

        String mainDepartmentNameKR = user.getUserDepartments().stream()
                .filter(ud -> YNType.isY(ud.getMainYN()) && YNType.isN(ud.getIsDeleted()))
                .findFirst()
                .map(ud -> ud.getDepartment().getNameKR())
                .orElse(null);

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
                .countryCode(user.getCountryCode())
                .profileName(user.getProfileName())
                .profileUrl(StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                        generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null)
                .invitationToken(user.getInvitationToken())
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .registeredAt(user.getRegisteredAt())
                .mainDepartmentNameKR(mainDepartmentNameKR)
                .dashboard(user.getDashboard())
                .build();
    }

    @Override
    public List<UserServiceDto> searchUsers() {
        log.debug("전체 사용자 목록 조회 시작");
        List<User> users = userRepository.findUsersWithRolesAndPermissions();
        log.debug("전체 사용자 목록 조회 완료: count={}", users.size());

        return users.stream()
                .map(user -> {
                    String mainDepartmentNameKR = user.getUserDepartments().stream()
                            .filter(ud -> YNType.isY(ud.getMainYN()) && YNType.isN(ud.getIsDeleted()))
                            .findFirst()
                            .map(ud -> ud.getDepartment().getNameKR())
                            .orElse(null);

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
                            .countryCode(user.getCountryCode())
                            .profileName(user.getProfileName())
                            .profileUrl(StringUtils.hasText(user.getProfileName()) && StringUtils.hasText(user.getProfileUUID()) ?
                                    generateProfileUrl(user.getProfileName(), user.getProfileUUID()) : null)
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

    @Override
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
                data.getDashboard(),
                data.getCountryCode()
        );
        log.info("사용자 수정 완료: id={}", data.getId());
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.debug("사용자 삭제 시작: userId={}", userId);
        User user = checkUserExist(userId);
        user.deleteUser();
        log.info("사용자 삭제 완료: userId={}", userId);
    }

    @Override
    public UserServiceDto saveProfileImgInTempFolder(MultipartFile file) {
        log.debug("프로필 이미지 임시 저장 시작: filename={}", file.getOriginalFilename());
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String uuid = UUID.randomUUID().toString();

        String physicalFilename = PorestFile.generatePhysicalFilename(originalFilename, uuid);

        PorestFile.save(file, tempPath, physicalFilename, messageResolver);

        String absolutePath = Paths.get(tempPath, physicalFilename).toString();

        log.debug("프로필 이미지 임시 저장 완료: uuid={}, path={}", uuid, absolutePath);
        return UserServiceDto.builder()
                .profileUrl(absolutePath.replace(fileRootPath, webUrlPrefix))
                .profileUUID(uuid)
                .build();
    }

    @Override
    public User checkUserExist(String userId) {
        Optional<User> findUser = userRepository.findById(userId);
        if ((findUser.isEmpty()) || YNType.isY(findUser.get().getIsDeleted())) {
            log.warn("사용자 조회 실패 - 존재하지 않거나 삭제된 사용자: userId={}", userId);
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        return findUser.get();
    }

    @Override
    public User findUserById(String userId) {
        Optional<User> findUser = userRepository.findByIdWithRolesAndPermissions(userId);
        if ((findUser.isEmpty()) || YNType.isY(findUser.get().getIsDeleted())) {
            log.warn("사용자 조회 실패 - 존재하지 않거나 삭제된 사용자: userId={}", userId);
            throw new EntityNotFoundException(ErrorCode.USER_NOT_FOUND);
        }
        return findUser.get();
    }

    @Override
    public String extractPhysicalFileNameFromUrl(String profileUrl) {
        if (!StringUtils.hasText(profileUrl)) {
            return null;
        }

        String relativePath = profileUrl.replace(webUrlPrefix, "");
        Path path = Paths.get(relativePath);
        return path.getFileName().toString();
    }

    @Override
    public String generateProfileUrl(String originalFilename, String uuid) {
        String physicalFilename = PorestFile.generatePhysicalFilename(originalFilename, uuid);
        if (physicalFilename == null) {
            return null;
        }

        String absolutePath = Paths.get(originPath, physicalFilename).toString();
        return absolutePath.replace(fileRootPath, webUrlPrefix);
    }

    @Override
    public UserServiceDto copyTempProfileToOrigin(UserServiceDto data) {
        String profileName = null;
        String profileUUID = null;

        String physicalFileName = extractPhysicalFileNameFromUrl(data.getProfileUrl());
        if (physicalFileName != null) {
            String tempFilePath = Paths.get(tempPath, physicalFileName).toString();
            String originFilePath = Paths.get(originPath, physicalFileName).toString();

            if (PorestFile.copy(tempFilePath, originFilePath, messageResolver)) {
                profileName = PorestFile.extractOriginalFilename(physicalFileName, null);
                profileUUID = data.getProfileUUID();
            }
        }

        return UserServiceDto.builder()
                .profileName(profileName)
                .profileUUID(profileUUID)
                .build();
    }

    @Override
    @Transactional
    public UserServiceDto inviteUser(UserServiceDto data) {
        log.debug("사용자 초대 시작: id={}, name={}, email={}", data.getId(), data.getName(), data.getEmail());
        if (checkUserIdDuplicate(data.getId())) {
            log.warn("사용자 초대 실패 - 중복 아이디: id={}", data.getId());
            throw new DuplicateException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.createInvitedUser(
                data.getId(),
                data.getName(),
                data.getEmail(),
                data.getCompany(),
                data.getWorkTime(),
                data.getJoinDate(),
                data.getCountryCode()
        );

        userRepository.save(user);

        emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());
        log.info("사용자 초대 완료: id={}, email={}", user.getId(), user.getEmail());

        return UserServiceDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .workTime(user.getWorkTime())
                .joinDate(user.getJoinDate())
                .countryCode(user.getCountryCode())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }

    @Override
    @Transactional
    public UserServiceDto resendInvitation(String userId) {
        log.debug("초대 이메일 재전송 시작: userId={}", userId);
        User user = checkUserExist(userId);
        user.renewInvitationToken();

        emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());
        log.info("초대 이메일 재전송 완료: userId={}, email={}", userId, user.getEmail());

        return UserServiceDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .company(user.getCompany())
                .workTime(user.getWorkTime())
                .joinDate(user.getJoinDate())
                .countryCode(user.getCountryCode())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }

    @Override
    @Transactional
    public UserServiceDto editInvitedUser(String userId, UserServiceDto data) {
        log.debug("초대된 사용자 정보 수정 시작: userId={}", userId);
        User user = checkUserExist(userId);

        if (user.getInvitationStatus() != StatusType.PENDING) {
            log.warn("초대된 사용자 수정 실패 - PENDING 상태가 아님: userId={}, status={}", userId, user.getInvitationStatus());
            throw new BusinessRuleViolationException(ErrorCode.USER_INACTIVE);
        }

        String oldEmail = user.getEmail();
        boolean emailChanged = data.getEmail() != null && !data.getEmail().equals(oldEmail);

        user.updateInvitedUser(
                data.getName(),
                data.getEmail(),
                data.getCompany(),
                data.getWorkTime(),
                data.getJoinDate(),
                data.getCountryCode()
        );

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
                .countryCode(user.getCountryCode())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .invitationSentAt(user.getInvitationSentAt())
                .invitationExpiresAt(user.getInvitationExpiresAt())
                .invitationStatus(user.getInvitationStatus())
                .build();
    }

    @Override
    @Transactional
    public String completeInvitedUserRegistration(UserServiceDto data) {
        log.debug("초대 수락 및 회원가입 시작: token={}", data.getInvitationToken());
        Optional<User> findUser = userRepository.findByInvitationToken(data.getInvitationToken());
        if (findUser.isEmpty()) {
            log.warn("회원가입 실패 - 초대 토큰 없음: token={}", data.getInvitationToken());
            throw new EntityNotFoundException(ErrorCode.INVITATION_NOT_FOUND);
        }

        User user = findUser.get();
        if (!user.isInvitationValid()) {
            log.warn("회원가입 실패 - 초대 만료: userId={}", user.getId());
            throw new BusinessRuleViolationException(ErrorCode.INVITATION_EXPIRED);
        }

        user.completeRegistration(
                data.getBirth(),
                data.getLunarYN()
        );
        log.info("회원가입 완료: userId={}", user.getId());

        return user.getId();
    }

    @Override
    public boolean checkUserIdDuplicate(String userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        return existingUser.isPresent();
    }

    @Override
    public YNType checkUserHasMainDepartment(String userId) {
        checkUserExist(userId);

        boolean hasMainDepartment = departmentRepository.hasMainDepartment(userId);

        return hasMainDepartment ? YNType.Y : YNType.N;
    }

    @Override
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

    @Override
    public List<UserServiceDto> getUserApprovers(String userId) {
        log.debug("승인권자 목록 조회 시작: userId={}", userId);
        checkUserExist(userId);

        List<com.lshdainty.porest.department.domain.Department> approverDepartments =
                departmentRepository.findApproversByUserId(userId);
        log.debug("승인권자 목록 조회 완료: userId={}, count={}", userId, approverDepartments.size());

        return approverDepartments.stream()
                .map(dept -> {
                    User approver = userRepository.findByIdWithRolesAndPermissions(dept.getHeadUserId())
                            .orElse(null);

                    if (approver == null || YNType.isY(approver.getIsDeleted())) {
                        return null;
                    }

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
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> getUserWithRolesById(String userId) {
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

    @Override
    public Optional<User> getUserWithRolesByInvitationToken(String token) {
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
