package com.porest.hr.user.service;

import com.porest.core.exception.EntityNotFoundException;
import com.porest.hr.common.exception.HrErrorCode;
import com.porest.core.type.YNType;
import com.porest.core.util.MessageResolver;
import com.porest.core.util.FileUtils;
import com.porest.hr.department.repository.DepartmentRepository;
import com.porest.hr.permission.domain.Role;
import com.porest.hr.permission.repository.RoleRepository;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.repository.UserRepository;
import com.porest.hr.user.service.dto.UserServiceDto;
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
        log.debug("사용자 생성 시작: ssoUserNo={}, id={}, name={}, email={}",
                data.getSsoUserNo(), data.getId(), data.getName(), data.getEmail());

        if (data.getSsoUserNo() == null) {
            throw new IllegalArgumentException("ssoUserNo는 필수입니다. SSO에서 사용자 생성 후 진행해주세요.");
        }

        UserServiceDto profileDto = UserServiceDto.builder().build();
        if (StringUtils.hasText(data.getProfileUUID()) && StringUtils.hasText(data.getProfileUrl())) {
            log.debug("프로필 이미지 복사 진행: uuid={}", data.getProfileUUID());
            profileDto = copyTempProfileToOrigin(data);
        }

        User user = User.createUser(
                data.getSsoUserNo(),
                data.getId(),
                data.getName(),
                data.getEmail(),
                data.getBirth(),
                data.getCompany(),
                data.getWorkTime(),
                data.getJoinDate(),
                data.getLunarYN(),
                profileDto.getProfileName(),
                profileDto.getProfileUUID(),
                data.getCountryCode()
        );

        userRepository.save(user);
        log.info("사용자 생성 완료: ssoUserNo={}, id={}", user.getSsoUserNo(), user.getId());
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

        String physicalFilename = FileUtils.generatePhysicalFilename(originalFilename, uuid);

        FileUtils.save(file, tempPath, physicalFilename, messageResolver);

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
            throw new EntityNotFoundException(HrErrorCode.USER_NOT_FOUND);
        }
        return findUser.get();
    }

    @Override
    public User findUserById(String userId) {
        Optional<User> findUser = userRepository.findByIdWithRolesAndPermissions(userId);
        if ((findUser.isEmpty()) || YNType.isY(findUser.get().getIsDeleted())) {
            log.warn("사용자 조회 실패 - 존재하지 않거나 삭제된 사용자: userId={}", userId);
            throw new EntityNotFoundException(HrErrorCode.USER_NOT_FOUND);
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
        String physicalFilename = FileUtils.generatePhysicalFilename(originalFilename, uuid);
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

            if (FileUtils.copy(tempFilePath, originFilePath, messageResolver)) {
                profileName = FileUtils.extractOriginalFilename(physicalFileName, null);
                profileUUID = data.getProfileUUID();
            }
        }

        return UserServiceDto.builder()
                .profileName(profileName)
                .profileUUID(profileUUID)
                .build();
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

        List<com.porest.hr.department.domain.Department> approverDepartments =
                departmentRepository.findApproversByUserId(userId);
        log.debug("승인권자 목록 조회 완료: userId={}, count={}", userId, approverDepartments.size());

        return approverDepartments.stream()
                .map(dept -> {
                    if (dept.getHeadUser() == null) {
                        return null;
                    }
                    User approver = userRepository.findByIdWithRolesAndPermissions(dept.getHeadUser().getId())
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
    public List<User> findAllUsers() {
        log.debug("전체 사용자 엔티티 목록 조회 시작");
        List<User> users = userRepository.findUsersWithRolesAndPermissions();
        log.debug("전체 사용자 엔티티 목록 조회 완료: count={}", users.size());
        return users;
    }
}
