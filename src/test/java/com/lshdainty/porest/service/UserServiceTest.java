package com.lshdainty.porest.service;

import com.lshdainty.porest.domain.User;
import com.lshdainty.porest.repository.UserRepositoryImpl;
import com.lshdainty.porest.service.dto.UserServiceDto;
import com.lshdainty.porest.type.CompanyType;
import com.lshdainty.porest.type.DepartmentType;
import com.lshdainty.porest.util.PorestFile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mockStatic;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("유저 서비스 테스트")
class UserServiceTest {

    @Mock
    private MessageSource ms;

    @Mock
    private UserRepositoryImpl userRepositoryImpl;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // @Value로 주입되는 필드들을 테스트용으로 설정
        ReflectionTestUtils.setField(userService, "fileRootPath", "/var/www/media");
        ReflectionTestUtils.setField(userService, "webUrlPrefix", "/media");
        ReflectionTestUtils.setField(userService, "tempPath", "/var/www/media/temp/profile");
        ReflectionTestUtils.setField(userService, "originPath", "/var/www/media/origin/profile");
    }

    @Test
    @DisplayName("회원 가입 테스트 - 성공")
    void signUpSuccessTest() {
        // Given
        UserServiceDto dto = UserServiceDto.builder()
                .id("user1")
                .name("테스트유저")
                .email("test@test.com")
                .build();

        willDoNothing().given(userRepositoryImpl).save(any(User.class));

        // When
        String result = userService.join(dto);

        // Then
        then(userRepositoryImpl).should().save(any(User.class));
        assertThat(result).isEqualTo("user1");
    }

    @Test
    @DisplayName("회원 가입 테스트 - 성공 (프로필 포함)")
    void signUpWithProfileSuccessTest() {
        // Given
        UserServiceDto dto = UserServiceDto.builder()
                .id("user1")
                .name("테스트유저")
                .profileUrl("/media/temp/profile/test.jpg_some-uuid")
                .profileUUID("some-uuid")
                .build();

        willDoNothing().given(userRepositoryImpl).save(any(User.class));

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            mocked.when(() -> PorestFile.copy(anyString(), anyString(), any(MessageSource.class)))
                    .thenReturn(true);
            mocked.when(() -> PorestFile.extractOriginalFilename(anyString(), isNull()))
                    .thenReturn("test.jpg");

            // When
            String result = userService.join(dto);

            // Then
            then(userRepositoryImpl).should().save(argThat(user -> {
                assertThat(user.getProfileName()).isEqualTo("test.jpg");
                assertThat(user.getProfileUUID()).isEqualTo("some-uuid");
                return true;
            }));

            assertThat(result).isEqualTo("user1");

            // verify static method calls
            mocked.verify(() -> PorestFile.copy(anyString(), anyString(), eq(ms)));
            mocked.verify(() -> PorestFile.extractOriginalFilename(anyString(), isNull()));
        }
    }

    @Test
    @DisplayName("회원 가입 테스트 - 프로필 복사 실패")
    void signUpWithProfileCopyFailTest() {
        // Given
        UserServiceDto dto = UserServiceDto.builder()
                .id("user1")
                .name("테스트유저")
                .profileUrl("/media/temp/profile/test.jpg_some-uuid")
                .profileUUID("some-uuid")
                .build();

        willDoNothing().given(userRepositoryImpl).save(any(User.class));

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            mocked.when(() -> PorestFile.copy(anyString(), anyString(), any(MessageSource.class)))
                    .thenReturn(false); // 복사 실패

            // When
            String result = userService.join(dto);

            // Then
            then(userRepositoryImpl).should().save(argThat(user -> {
                // 복사 실패 시 프로필 정보가 null이어야 함
                assertThat(user.getProfileName()).isNull();
                assertThat(user.getProfileUUID()).isNull();
                return true;
            }));

            assertThat(result).isEqualTo("user1");
        }
    }

    @Test
    @DisplayName("회원 가입 테스트 - 프로필 정보 없음")
    void signUpWithoutProfileTest() {
        // Given
        UserServiceDto dto = UserServiceDto.builder()
                .id("user1")
                .name("테스트유저")
                .build(); // 프로필 정보 없음

        willDoNothing().given(userRepositoryImpl).save(any(User.class));

        // When
        String result = userService.join(dto);

        // Then
        then(userRepositoryImpl).should().save(argThat(user -> {
            assertThat(user.getProfileName()).isNull();
            assertThat(user.getProfileUUID()).isNull();
            return true;
        }));

        assertThat(result).isEqualTo("user1");
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 성공")
    void findUserSuccessTest() {
        // Given
        String id = "user1";
        String name = "이서준";
        User user = User.createUser(id, "", name, "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");

        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When
        UserServiceDto findUser = userService.findUser(id);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(findUser).isNotNull();
        assertThat(findUser.getName()).isEqualTo(name);
        assertThat(findUser.getProfileUrl()).isNull();
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 성공 (프로필 포함)")
    void findUserWithProfileSuccessTest() {
        // Given
        String id = "user1";
        String name = "이서준";
        String profileName = "profile.png";
        String profileUUID = "some-uuid-1234";
        User user = User.createUser(id, "", name, "", "", CompanyType.SKAX, DepartmentType.SKC, "", "", profileName, profileUUID);

        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            String physicalName = profileName + "_" + profileUUID;
            mocked.when(() -> PorestFile.generatePhysicalFilename(profileName, profileUUID))
                    .thenReturn(physicalName);

            // When
            UserServiceDto findUser = userService.findUser(id);

            // Then
            then(userRepositoryImpl).should().findById(id);
            assertThat(findUser).isNotNull();
            assertThat(findUser.getProfileUrl()).isNotNull();
            assertThat(findUser.getProfileUrl()).contains(physicalName);

            mocked.verify(() -> PorestFile.generatePhysicalFilename(profileName, profileUUID));
        }
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 실패 (유저 없음)")
    void findUserFailTestNotFoundUser() {
        // Given
        String id = "nonexistent";
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.user", null, null)).willReturn("User not found");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.findUser(id));

        assertThat(exception.getMessage()).isEqualTo("User not found");
        then(userRepositoryImpl).should().findById(id);
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 실패 (삭제된 유저 조회)")
    void findUserFailTestDeletedUser() {
        // Given
        String id = "user1";
        User user = User.createUser(id, "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        user.deleteUser();

        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));
        given(ms.getMessage("error.notfound.user", null, null)).willReturn("User not found");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.findUser(id));

        assertThat(exception.getMessage()).isEqualTo("User not found");
        then(userRepositoryImpl).should().findById(id);
    }

    @Test
    @DisplayName("전체 유저 조회 테스트 - 성공")
    void findUsersSuccessTest() {
        // Given
        given(userRepositoryImpl.findUsers()).willReturn(List.of(
                User.createUser("user1", "", "이서준", "", "", CompanyType.SKAX, DepartmentType.SKC, "", ""),
                User.createUser("user2", "", "김서연", "", "", CompanyType.SKAX, DepartmentType.SKC, "", ""),
                User.createUser("user3", "", "김지후", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "")
        ));

        // When
        List<UserServiceDto> findUsers = userService.findUsers();

        // Then
        then(userRepositoryImpl).should().findUsers();
        assertThat(findUsers).hasSize(3);
        assertThat(findUsers)
                .extracting(UserServiceDto::getName)
                .containsExactlyInAnyOrder("이서준", "김서연", "김지후");
    }

    @Test
    @DisplayName("전체 유저 조회 테스트 - 빈 결과")
    void findUsersEmptyTest() {
        // Given
        given(userRepositoryImpl.findUsers()).willReturn(Collections.emptyList());

        // When
        List<UserServiceDto> findUsers = userService.findUsers();

        // Then
        then(userRepositoryImpl).should().findUsers();
        assertThat(findUsers).isEmpty();
    }

    @Test
    @DisplayName("유저 수정 테스트 - 성공")
    void editUserSuccessTest() {
        // Given
        String id = "user1";
        User user = User.createUser(id, "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");

        setUserId(user, id);
        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        UserServiceDto updateDto = UserServiceDto.builder()
                .id(id)
                .name("이하은")
                .workTime("10 ~ 7")
                .build();

        // When
        userService.editUser(updateDto);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(user.getName()).isEqualTo("이하은");
        assertThat(user.getWorkTime()).isEqualTo("10 ~ 7");
    }

    @Test
    @DisplayName("유저 수정 테스트 - 성공 (프로필 변경)")
    void editUserWithProfileSuccessTest() {
        // Given
        String id = "user1";
        User user = User.createUser(id, "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "", "old.jpg", "old-uuid");
        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        UserServiceDto dto = UserServiceDto.builder()
                .id(id)
                .profileUrl("/media/temp/profile/new.jpg_new-uuid")
                .profileUUID("new-uuid")
                .build();

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            mocked.when(() -> PorestFile.copy(anyString(), anyString(), any(MessageSource.class)))
                    .thenReturn(true);
            mocked.when(() -> PorestFile.extractOriginalFilename(anyString(), isNull()))
                    .thenReturn("new.jpg");

            // When
            userService.editUser(dto);

            // Then
            then(userRepositoryImpl).should().findById(id);
            assertThat(user.getProfileName()).isEqualTo("new.jpg");
            assertThat(user.getProfileUUID()).isEqualTo("new-uuid");

            // verify static method calls
            mocked.verify(() -> PorestFile.copy(anyString(), anyString(), eq(ms)));
            mocked.verify(() -> PorestFile.extractOriginalFilename(anyString(), isNull()));
        }
    }

    @Test
    @DisplayName("유저 수정 테스트 - 성공 (같은 프로필 UUID)")
    void editUserWithSameProfileUUIDTest() {
        // Given
        String id = "user1";
        String existingUUID = "existing-uuid";
        User user = User.createUser(id, "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "", "old.jpg", existingUUID);
        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        UserServiceDto dto = UserServiceDto.builder()
                .id(id)
                .name("변경된이름")
                .profileUUID(existingUUID) // 기존과 동일한 UUID
                .build();

        // When
        userService.editUser(dto);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(user.getName()).isEqualTo("변경된이름");
        // 프로필은 변경되지 않아야 함
        assertThat(user.getProfileName()).isEqualTo("old.jpg");
        assertThat(user.getProfileUUID()).isEqualTo(existingUUID);
    }

    @Test
    @DisplayName("유저 수정 테스트 - 실패 (유저 없음)")
    void editUserFailTestNotFoundUser() {
        // Given
        String id = "nonexistent";
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.user", null, null)).willReturn("User not found");

        UserServiceDto dto = UserServiceDto.builder().id(id).build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.editUser(dto));

        assertThat(exception.getMessage()).isEqualTo("User not found");
        then(userRepositoryImpl).should().findById(id);
    }

    @Test
    @DisplayName("유저 삭제 테스트 - 성공")
    void deleteUserSuccessTest() {
        // Given
        String id = "user1";
        User user = User.createUser(id, "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");

        setUserId(user, id);
        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When
        userService.deleteUser(id);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(user.getDelYN()).isEqualTo("Y");
    }

    @Test
    @DisplayName("유저 삭제 테스트 - 실패 (유저 없음)")
    void deleteUserFailTestNotFoundUser() {
        // Given
        String id = "nonexistent";
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());
        given(ms.getMessage("error.notfound.user", null, null)).willReturn("User not found");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(id));

        assertThat(exception.getMessage()).isEqualTo("User not found");
        then(userRepositoryImpl).should().findById(id);
    }

    @Test
    @DisplayName("프로필 이미지 임시 저장 테스트 - 성공")
    void saveProfileImgInTempFolderSuccessTest() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        String physicalFilename = "test.jpg_some-uuid";

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            mocked.when(() -> PorestFile.generatePhysicalFilename(eq("test.jpg"), anyString()))
                    .thenReturn(physicalFilename);
            mocked.when(() -> PorestFile.save(eq(mockFile), anyString(), eq(physicalFilename), eq(ms)))
                    .thenReturn(true);

            // When
            UserServiceDto result = userService.saveProfileImgInTempFolder(mockFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProfileUrl()).contains(physicalFilename);
            assertThat(result.getProfileUUID()).isNotNull();

            // verify 호출 확인
            mocked.verify(() -> PorestFile.generatePhysicalFilename(eq("test.jpg"), anyString()));
            mocked.verify(() -> PorestFile.save(eq(mockFile), anyString(), eq(physicalFilename), eq(ms)));
        }
    }

    @Test
    @DisplayName("프로필 이미지 임시 저장 테스트 - 실패 (파일 저장 실패)")
    void saveProfileImgInTempFolderFailTest() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        String physicalFilename = "test.jpg_some-uuid";

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            mocked.when(() -> PorestFile.generatePhysicalFilename(eq("test.jpg"), anyString()))
                    .thenReturn(physicalFilename);
            mocked.when(() -> PorestFile.save(eq(mockFile), anyString(), eq(physicalFilename), eq(ms)))
                    .thenReturn(false); // 저장 실패

            // When
            UserServiceDto result = userService.saveProfileImgInTempFolder(mockFile);

            // Then
            // 저장이 실패해도 URL은 생성되는지 확인 (서비스 로직에 따라)
            assertThat(result).isNotNull();
            assertThat(result.getProfileUrl()).contains(physicalFilename);
            assertThat(result.getProfileUUID()).isNotNull();
        }
    }

    @Test
    @DisplayName("프로필 이미지 임시 저장 테스트 - 빈 파일명")
    void saveProfileImgInTempFolderEmptyFilenameTest() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", "", "image/jpeg", "test image content".getBytes());
        String physicalFilename = "_some-uuid";

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            mocked.when(() -> PorestFile.generatePhysicalFilename(eq(""), anyString()))
                    .thenReturn(physicalFilename);
            mocked.when(() -> PorestFile.save(eq(mockFile), anyString(), eq(physicalFilename), eq(ms)))
                    .thenReturn(true);

            // When
            UserServiceDto result = userService.saveProfileImgInTempFolder(mockFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProfileUUID()).isNotNull();
            assertThat(result.getProfileUrl()).contains(physicalFilename);
        }
    }

    @Test
    @DisplayName("프로필 이미지 임시 저장 테스트 - null 파일명")
    void saveProfileImgInTempFolderNullFilenameTest() {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile("file", null, "image/jpeg", "test image content".getBytes());
        String physicalFilename = "null_some-uuid";

        try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
            mocked.when(() -> PorestFile.generatePhysicalFilename(eq(""), anyString()))
                    .thenReturn(physicalFilename);
            mocked.when(() -> PorestFile.save(eq(mockFile), anyString(), eq(physicalFilename), eq(ms)))
                    .thenReturn(true);

            // When
            UserServiceDto result = userService.saveProfileImgInTempFolder(mockFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProfileUUID()).isNotNull();
            assertThat(result.getProfileUrl()).contains(physicalFilename);
        }
    }

    @Test
    @DisplayName("유저 존재 확인 테스트 - 성공")
    void checkUserExistSuccessTest() {
        // Given
        String id = "user1";
        User user = User.createUser(id, "", "테스트유저", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When
        User result = userService.checkUserExist(id);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(result).isEqualTo(user);
        assertThat(result.getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("유저 존재 확인 테스트 - 실패 (삭제된 유저)")
    void checkUserExistFailDeletedUserTest() {
        // Given
        String id = "user1";
        User user = User.createUser(id, "", "테스트유저", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");
        user.deleteUser(); // 유저 삭제

        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));
        given(ms.getMessage("error.notfound.user", null, null)).willReturn("User not found");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.checkUserExist(id));

        assertThat(exception.getMessage()).isEqualTo("User not found");
        then(userRepositoryImpl).should().findById(id);
    }

    // 테스트 헬퍼 메서드
    private void setUserId(User user, String id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user id", e);
        }
    }
}
