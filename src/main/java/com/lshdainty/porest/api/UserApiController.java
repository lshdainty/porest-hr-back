package com.lshdainty.porest.api;

import com.lshdainty.porest.api.dto.UserDto;
import com.lshdainty.porest.service.UserService;
import com.lshdainty.porest.service.dto.UserServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
                .profileUrl(data.getProfileUrl())
                .profileUUID(data.getProfileUUID())
                .build()
        );

        return ApiResponse.success(UserDto.builder().userId(userId).build());
    }

    @GetMapping("/api/v1/user/{id}")
    public ApiResponse user(@PathVariable("id") String userId) {
        UserServiceDto user = userService.findUser(userId);

        return ApiResponse.success(UserDto.builder()
                .userId(user.getId())
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
                .profileUrl(user.getProfileUrl())
                .build()
        );
    }

    @GetMapping("/api/v1/users")
    public ApiResponse users() {
        List<UserServiceDto> users = userService.findUsers();

        List<UserDto> resps = users.stream()
                .map(u -> UserDto.builder()
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
                        .profileUrl(u.getProfileUrl())
                        .build()
                )
                .collect(Collectors.toList());

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
                .profileUrl(data.getProfileUrl())
                .profileUUID(data.getProfileUUID())
                .build()
        );

        UserServiceDto findUser = userService.findUser(userId);

        return ApiResponse.success(UserDto.builder()
                .userId(findUser.getId())
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
                .profileUrl(findUser.getProfileUrl())
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
        UserServiceDto dto = userService.saveProfileImgInTempFolder(data.getProfile());
        return ApiResponse.success(UserDto.builder()
                .profileUrl(dto.getProfileUrl())
                .profileUUID(dto.getProfileUUID())
                .build());
    }
}
