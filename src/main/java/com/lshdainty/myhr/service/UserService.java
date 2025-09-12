package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.service.dto.UserServiceDto;
import com.lshdainty.myhr.util.PorestFile;
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

    @Value("${file.root-path}")
    private String fileRootPath;

    @Value("${file.web-url-prefix}")
    private String webUrlPrefix;

    @Value("${file.temp.path.profile}")
    private String tempPath;

    @Value("${file.origin.path.profile}")
    private String originPath;

    @Transactional
    public String join(UserServiceDto data) {
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
                data.getDepartment(),
                data.getWorkTime(),
                data.getLunarYN(),
                profileDto.getProfileName(),
                profileDto.getProfileUUID()
        );

        userRepositoryImpl.save(user);
        return user.getId();
    }

    public UserServiceDto findUser(String userId) {
        User user = checkUserExist(userId);

        return UserServiceDto.builder()
                .id(user.getId())
                .pwd(user.getPwd())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .birth(user.getBirth())
                .workTime(user.getWorkTime())
                .company(user.getCompany())
                .department(user.getDepartment())
                .lunarYN(user.getLunarYN())
                .profileUrl(StringUtils.hasText(user.getProfileName()) ?
                                Paths.get(originPath, user.getProfileName()).toString().replace(fileRootPath, webUrlPrefix) : null)
                .build();
    }

    public List<UserServiceDto> findUsers() {
        List<User> users = userRepositoryImpl.findUsers();

        return users.stream()
                .map(user -> UserServiceDto.builder()
                        .id(user.getId())
                        .pwd(user.getPwd())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .birth(user.getBirth())
                        .workTime(user.getWorkTime())
                        .company(user.getCompany())
                        .department(user.getDepartment())
                        .lunarYN(user.getLunarYN())
                        .profileUrl(StringUtils.hasText(user.getProfileName()) ?
                                Paths.get(originPath, user.getProfileName()).toString().replace(fileRootPath, webUrlPrefix) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void editUser(UserServiceDto data) {
        User user = checkUserExist(data.getId());

        UserServiceDto profileDto = UserServiceDto.builder().build();
        if (StringUtils.hasText(data.getProfileUUID()) && !data.getProfileUUID().equals(user.getProfileUUID())) {
            profileDto = copyTempProfileToOrigin(data);
        }

        user.updateUser(
                data.getName(),
                data.getEmail(),
                data.getRole(),
                data.getBirth(),
                data.getCompany(),
                data.getDepartment(),
                data.getWorkTime(),
                data.getLunarYN(),
                profileDto.getProfileName(),
                profileDto.getProfileUUID()
        );
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = checkUserExist(userId);
        user.deleteUser();
    }

    public UserServiceDto saveProfileImgInTempFolder(MultipartFile file) {
        // PorestFile.save를 호출하여 파일을 임시 디렉토리에 원본 파일명으로 저장
        PorestFile.save(file, tempPath, ms);

        // 저장된 파일의 절대 경로 생성
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String absolutePath = Paths.get(tempPath, originalFilename).toString();

        // 절대 경로를 웹 접근 가능 URL로 변환
        return UserServiceDto.builder()
                .profileUrl(absolutePath.replace(fileRootPath, webUrlPrefix))
                .profileUUID(UUID.randomUUID().toString())
                .build();
    }

    public User checkUserExist(String userId) {
        Optional<User> findUser = userRepositoryImpl.findById(userId);
        if ((findUser.isEmpty()) || findUser.get().getDelYN().equals("Y")) {
            throw new IllegalArgumentException(ms.getMessage("error.notfound.user", null, null));
        }
        return findUser.get();
    }

    /**
     * 프로필 URL에서 파일명을 추출하는 헬퍼 메소드
     */
    private String extractFileNameFromUrl(String profileUrl) {
        if (!StringUtils.hasText(profileUrl)) {
            return null;
        }

        String relativePath = profileUrl.replace(webUrlPrefix, "");
        Path path = Paths.get(relativePath);
        return path.getFileName().toString();
    }

    /**
     * 임시 폴더에 저장된 프로필 이미지를</br>
     * 관리용 폴더로 복사하는 헬퍼 메소드
     */
    private UserServiceDto copyTempProfileToOrigin(UserServiceDto data) {
        String profileName = null;
        String profileUUID = null;

        String fileName = extractFileNameFromUrl(data.getProfileUrl());
        if (fileName != null) {
            String tempFilePath = Paths.get(tempPath, fileName).toString();
            String originFilePath = Paths.get(originPath, fileName).toString();

            if (PorestFile.copy(tempFilePath, originFilePath, ms)) {
                profileName = fileName;
                profileUUID = data.getProfileUUID();
            }
        }

        return UserServiceDto.builder()
                .profileName(profileName)
                .profileUUID(profileUUID)
                .build();
    }
}
