package com.lshdainty.porest.user.service;

import com.lshdainty.porest.department.repository.DepartmentCustomRepositoryImpl;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepositoryImpl;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.common.util.PorestFile;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.RoleRepository;
import com.lshdainty.porest.user.type.StatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
    private final MessageSource ms;
    private final UserRepositoryImpl userRepositoryImpl;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final DepartmentCustomRepositoryImpl departmentRepository;

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
        UserServiceDto profileDto = UserServiceDto.builder().build();
        if (StringUtils.hasText(data.getProfileUUID()) && StringUtils.hasText(data.getProfileUrl())) {
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

        userRepositoryImpl.save(user);
        return user.getId();
    }

    public UserServiceDto searchUser(String userId) {
        User user = checkUserExist(userId);

        // 메인 부서의 한글명 조회
        String mainDepartmentNameKR = user.getUserDepartments().stream()
                .filter(ud -> ud.getMainYN() == YNType.Y && ud.getIsDeleted() == YNType.N)
                .findFirst()
                .map(ud -> ud.getDepartment().getNameKR())
                .orElse(null);

        return UserServiceDto.builder()
                .id(user.getId())
                .pwd(user.getPwd())
                .name(user.getName())
                .email(user.getEmail())
                .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
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
        List<User> users = userRepositoryImpl.findUsers();

        return users.stream()
                .map(user -> {
                    // 메인 부서의 한글명 조회
                    String mainDepartmentNameKR = user.getUserDepartments().stream()
                            .filter(ud -> ud.getMainYN() == YNType.Y && ud.getIsDeleted() == YNType.N)
                            .findFirst()
                            .map(ud -> ud.getDepartment().getNameKR())
                            .orElse(null);

                    return UserServiceDto.builder()
                            .id(user.getId())
                            .pwd(user.getPwd())
                            .name(user.getName())
                            .email(user.getEmail())
                            .roleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
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
        User user = checkUserExist(data.getId());

        UserServiceDto profileDto = UserServiceDto.builder().build();
        if (StringUtils.hasText(data.getProfileUUID()) && !data.getProfileUUID().equals(user.getProfileUUID())) {
            profileDto = copyTempProfileToOrigin(data);
        }

        List<Role> roles = null;
        if (data.getRoleNames() != null) {
            roles = data.getRoleNames().stream()
                    .map(name -> roleRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Role not found: " + name)))
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
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = checkUserExist(userId);
        user.deleteUser();
    }

    public UserServiceDto saveProfileImgInTempFolder(MultipartFile file) {
        // 원본 파일명 추출
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String uuid = UUID.randomUUID().toString();

        // PorestFile의 static 메소드를 사용하여 물리적 파일명 생성
        String physicalFilename = PorestFile.generatePhysicalFilename(originalFilename, uuid);

        // 임시 폴더에 물리적 파일명으로 저장
        PorestFile.save(file, tempPath, physicalFilename, ms);

        // 저장된 파일의 절대 경로 생성 (물리적 파일명 기준)
        String absolutePath = Paths.get(tempPath, physicalFilename).toString();

        return UserServiceDto.builder()
                .profileUrl(absolutePath.replace(fileRootPath, webUrlPrefix))
                .profileUUID(uuid)
                .build();
    }

    public User checkUserExist(String userId) {
        Optional<User> findUser = userRepositoryImpl.findById(userId);
        if ((findUser.isEmpty()) || findUser.get().getIsDeleted().equals(YNType.Y)) {
            throw new IllegalArgumentException(ms.getMessage("error.notfound.user", null, null));
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

            if (PorestFile.copy(tempFilePath, originFilePath, ms)) {
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
        // 아이디 중복 체크
        if (checkUserIdDuplicate(data.getId())) {
            throw new IllegalArgumentException(ms.getMessage("error.duplicate.userId", null, null));
        }

        User user = User.createInvitedUser(
                data.getId(),
                data.getName(),
                data.getEmail(),
                data.getCompany(),
                data.getWorkTime(),
                data.getJoinDate()
        );

        userRepositoryImpl.save(user);

        // 초대 이메일 발송
        emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());

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
        User user = checkUserExist(userId);
        user.renewInvitationToken();

        // 초대 이메일 재발송
        emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());

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
        User user = checkUserExist(userId);

        // PENDING 상태인지 확인
        if (user.getInvitationStatus() != StatusType.PENDING) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.not.pending.user", null, null));
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
            emailService.sendInvitationEmail(user.getEmail(), user.getName(), user.getInvitationToken());
        }

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
        Optional<User> findUser = userRepositoryImpl.findByInvitationToken(data.getInvitationToken());
        if (findUser.isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("error.notfound.invitation", null, null));
        }

        User user = findUser.get();
        if (!user.isInvitationValid()) {
            throw new IllegalArgumentException(ms.getMessage("error.expired.invitation", null, null));
        }

        user.completeRegistration(
                data.getBirth(),
                data.getLunarYN()
        );

        return user.getId();
    }

    /**
     * 아이디 중복 체크
     */
    public boolean checkUserIdDuplicate(String userId) {
        Optional<User> existingUser = userRepositoryImpl.findById(userId);
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
        User user = checkUserExist(userId);
        user.updateDashboard(dashboard);

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
        // 유저 존재 확인
        checkUserExist(userId);

        // 상위 부서장 목록 조회
        List<com.lshdainty.porest.department.domain.Department> approverDepartments =
                departmentRepository.findApproversByUserId(userId);

        // Department 정보를 UserServiceDto로 변환
        return approverDepartments.stream()
                .map(dept -> {
                    // headUserId로 User 조회
                    User approver = userRepositoryImpl.findById(dept.getHeadUserId())
                            .orElse(null);

                    if (approver == null || approver.getIsDeleted() == YNType.Y) {
                        return null;
                    }

                    return UserServiceDto.builder()
                            .id(approver.getId())
                            .name(approver.getName())
                            .email(approver.getEmail())
                            .roleNames(approver.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                            .departmentId(dept.getId())
                            .departmentName(dept.getName())
                            .departmentNameKR(dept.getNameKR())
                            .departmentLevel(dept.getLevel())
                            .build();
                })
                .filter(dto -> dto != null) // null 제거
                .collect(Collectors.toList());
    }
}