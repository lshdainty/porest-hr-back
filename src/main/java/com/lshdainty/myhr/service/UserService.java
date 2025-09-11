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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {
    private final MessageSource ms;
    private final UserRepositoryImpl userRepositoryImpl;

    @Value("${file.temp.path.profile}")
    private String tempPath;

    @Transactional
    public String join(UserServiceDto data) {
        User user = User.createUser(
                data.getId(),
                data.getPwd(),
                data.getName(),
                data.getEmail(),
                data.getBirth(),
                data.getCompany(),
                data.getDepartment(),
                data.getWorkTime(),
                data.getLunarYN()
        );
        userRepositoryImpl.save(user);
        return user.getId();
    }

    public User findUser(String userId) {
        return checkUserExist(userId);
    }

    public List<User> findUsers() {
        return userRepositoryImpl.findUsers();
    }

    @Transactional
    public void editUser(UserServiceDto data) {
        User user = checkUserExist(data.getId());
        user.updateUser(
                data.getName(),
                data.getEmail(),
                data.getRole(),
                data.getBirth(),
                data.getCompany(),
                data.getDepartment(),
                data.getWorkTime(),
                data.getLunarYN()
        );
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = checkUserExist(userId);
        user.deleteUser();
    }

    public String saveProfileImgInTempFolder(MultipartFile file) {
        // PorestFile.save를 호출하여 파일을 임시 디렉토리에 원본 파일명으로 저장
        PorestFile.save(file, tempPath, ms);

        // 저장된 파일의 전체 경로를 생성하여 반환
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        Path filePath = Paths.get(tempPath, originalFilename);

        return filePath.toString();
    }

    public User checkUserExist(String userId) {
        Optional<User> findUser = userRepositoryImpl.findById(userId);
        if ((findUser.isEmpty()) || findUser.get().getDelYN().equals("Y")) { throw new IllegalArgumentException(ms.getMessage("error.notfound.user", null, null)); }
        return findUser.get();
    }
}