package com.lshdainty.myhr.api;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.api.dto.UserDto;
import com.lshdainty.myhr.service.UserService;
import com.lshdainty.myhr.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserApiController {
    private final UserService userService;

    @PostMapping("/api/v1/user")
    public ApiResponse join(@RequestBody UserDto data) {
        String userId = userService.join(UserServiceDto.builder()
                .id(data.getUserId())
                .pwd(data.getUserPwd())
                .name(data.getUserName())
                .email(data.getUserEmail())
                .birth(data.getUserBirth())
                .company(data.getUserCompanyType())
                .department(data.getUserDepartmentType())
                .workTime(data.getUserWorkTime())
                .lunarYN(data.getLunarYN())
                .build()
        );

        return ApiResponse.success(UserDto.builder().userId(userId).build());
    }

    @GetMapping("/api/v1/user/{id}")
    public ApiResponse user(@PathVariable("id") String userId) {
        User user = userService.findUser(userId);

        return ApiResponse.success(UserDto.builder()
                .userId(userId)
                .userName(user.getName())
                .userEmail(user.getEmail())
                .userBirth(user.getBirth())
                .userWorkTime(user.getWorkTime())
                .userRoleType(user.getRole())
                .userRoleName(user.getRole().name())
                .userCompanyType(user.getCompany())
                .userCompanyName(user.getCompany().getCompanyName())
                .userDepartmentType(user.getDepartment())
                .userDepartmentName(user.getDepartment().getDepartmentName())
                .lunarYN(user.getLunarYN())
                .build()
        );
    }

    @GetMapping("/api/v1/users")
    public ApiResponse users() {
        List<User> users = userService.findUsers();

        List<UserDto> resps = users.stream()
                .map(u -> UserDto
                        .builder()
                        .userId(u.getId())
                        .userName(u.getName())
                        .userEmail(u.getEmail())
                        .userBirth(u.getBirth())
                        .userWorkTime(u.getWorkTime())
                        .userRoleType(u.getRole())
                        .userRoleName(u.getRole().name())
                        .userCompanyType(u.getCompany())
                        .userCompanyName(u.getCompany().getCompanyName())
                        .userDepartmentType(u.getDepartment())
                        .userDepartmentName(u.getDepartment().getDepartmentName())
                        .lunarYN(u.getLunarYN())
                        .build()
                )
                .toList();

        return ApiResponse.success(resps);
    }

    @PutMapping("/api/v1/user/{id}")
    public ApiResponse editUser(@PathVariable("id") String userId, @RequestBody UserDto data) {
        userService.editUser(UserServiceDto.builder()
                .id(userId)
                .name(data.getUserName())
                .email(data.getUserEmail())
                .birth(data.getUserBirth())
                .role(data.getUserRoleType())
                .company(data.getUserCompanyType())
                .department(data.getUserDepartmentType())
                .workTime(data.getUserWorkTime())
                .lunarYN(data.getLunarYN())
                .build()
        );

        User findUser = userService.findUser(userId);

        return ApiResponse.success(UserDto
                .builder()
                .userId(userId)
                .userName(findUser.getName())
                .userEmail(findUser.getEmail())
                .userBirth(findUser.getBirth())
                .userWorkTime(findUser.getWorkTime())
                .userRoleType(findUser.getRole())
                .userRoleName(findUser.getRole().name())
                .userCompanyType(findUser.getCompany())
                .userCompanyName(findUser.getCompany().getCompanyName())
                .userDepartmentType(findUser.getDepartment())
                .userDepartmentName(findUser.getDepartment().getDepartmentName())
                .lunarYN(findUser.getLunarYN())
                .build()
        );
    }

    @DeleteMapping("/api/v1/user/{id}")
    public ApiResponse deleteUser(@PathVariable("id") String userId) {
        userService.deleteUser(userId);
        return ApiResponse.success();
    }

    @PostMapping(value = "/api/v1/user/upload/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse uploadProfile(@ModelAttribute UserDto data) {
        String tempFilePath = userService.saveProfileImgInTempFolder(data.getProfile());
        return ApiResponse.success(tempFilePath);
    }
}
