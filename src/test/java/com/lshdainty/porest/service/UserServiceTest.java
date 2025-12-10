package com.lshdainty.porest.service;

import com.lshdainty.porest.common.exception.BusinessRuleViolationException;
import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.common.util.PorestFile;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.department.repository.DepartmentRepository;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepository;
import com.lshdainty.porest.user.service.EmailService;
import com.lshdainty.porest.user.service.UserService;
import com.lshdainty.porest.user.service.UserServiceImpl;
import com.lshdainty.porest.user.service.dto.UserServiceDto;
import com.lshdainty.porest.user.type.StatusType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.lshdainty.porest.common.util.MessageResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.lshdainty.porest.department.domain.Department;
import com.lshdainty.porest.department.domain.UserDepartment;
import com.lshdainty.porest.company.domain.Company;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.RoleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mockStatic;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("유저 서비스 테스트")
class UserServiceTest {
    @Mock
    private MessageResolver messageResolver;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "fileRootPath", "/var/www/media");
        ReflectionTestUtils.setField(userService, "webUrlPrefix", "/media");
        ReflectionTestUtils.setField(userService, "tempPath", "/var/www/media/temp/profile");
        ReflectionTestUtils.setField(userService, "originPath", "/var/www/media/origin/profile");
    }

    @Nested
    @DisplayName("회원 가입")
    class JoinUser {
        @Test
        @DisplayName("성공 - 유저가 정상적으로 저장된다")
        void joinUserSuccess() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .id("user1")
                    .name("테스트유저")
                    .email("test@test.com")
                    .build();
            willDoNothing().given(userRepository).save(any(User.class));

            // when
            String result = userService.joinUser(data);

            // then
            then(userRepository).should().save(any(User.class));
            assertThat(result).isEqualTo("user1");
        }

        @Test
        @DisplayName("성공 - 프로필 이미지와 함께 저장된다")
        void joinUserWithProfileSuccess() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .id("user1")
                    .name("테스트유저")
                    .profileUrl("/media/temp/profile/test.jpg_some-uuid")
                    .profileUUID("some-uuid")
                    .build();
            willDoNothing().given(userRepository).save(any(User.class));

            try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
                mocked.when(() -> PorestFile.copy(anyString(), anyString(), any(MessageResolver.class)))
                        .thenReturn(true);
                mocked.when(() -> PorestFile.extractOriginalFilename(anyString(), isNull()))
                        .thenReturn("test.jpg");

                // when
                String result = userService.joinUser(data);

                // then
                then(userRepository).should().save(any(User.class));
                assertThat(result).isEqualTo("user1");
            }
        }

        @Test
        @DisplayName("성공 - profileUUID만 있으면 프로필 복사 없이 저장된다")
        void joinUserWithOnlyProfileUUID() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .id("user1")
                    .name("테스트유저")
                    .profileUUID("some-uuid")
                    .build();
            willDoNothing().given(userRepository).save(any(User.class));

            // when
            String result = userService.joinUser(data);

            // then
            then(userRepository).should().save(any(User.class));
            assertThat(result).isEqualTo("user1");
        }

        @Test
        @DisplayName("성공 - profileUrl만 있으면 프로필 복사 없이 저장된다")
        void joinUserWithOnlyProfileUrl() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .id("user1")
                    .name("테스트유저")
                    .profileUrl("/media/temp/profile/test.jpg")
                    .build();
            willDoNothing().given(userRepository).save(any(User.class));

            // when
            String result = userService.joinUser(data);

            // then
            then(userRepository).should().save(any(User.class));
            assertThat(result).isEqualTo("user1");
        }
    }

    @Nested
    @DisplayName("유저 조회")
    class SearchUser {
        @Test
        @DisplayName("성공 - 존재하는 유저를 반환한다")
        void searchUserSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "test@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = userService.searchUser(userId);

            // then
            then(userRepository).should().findByIdWithRolesAndPermissions(userId);
            assertThat(result.getName()).isEqualTo("이서준");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저면 예외가 발생한다")
        void searchUserFailNotFound() {
            // given
            String userId = "nonexistent";
            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.searchUser(userId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 유저면 예외가 발생한다")
        void searchUserFailDeleted() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "test@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            user.deleteUser();
            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userService.searchUser(userId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("성공 - 프로필이 있는 유저를 반환한다")
        void searchUserWithProfileSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "test@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, "test.jpg", "some-uuid", CountryCode.KR);
            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.of(user));

            try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
                mocked.when(() -> PorestFile.generatePhysicalFilename("test.jpg", "some-uuid"))
                        .thenReturn("test.jpg_some-uuid");

                // when
                UserServiceDto result = userService.searchUser(userId);

                // then
                assertThat(result.getName()).isEqualTo("이서준");
                assertThat(result.getProfileName()).isEqualTo("test.jpg");
                assertThat(result.getProfileUrl()).contains("test.jpg_some-uuid");
            }
        }

        @Test
        @DisplayName("성공 - profileName만 있으면 profileUrl은 null이다")
        void searchUserWithOnlyProfileName() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "test@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, "test.jpg", null, CountryCode.KR);
            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = userService.searchUser(userId);

            // then
            assertThat(result.getProfileName()).isEqualTo("test.jpg");
            assertThat(result.getProfileUrl()).isNull();
        }

        @Test
        @DisplayName("성공 - 메인 부서가 있는 유저를 반환한다")
        void searchUserWithMainDepartment() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "test@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);

            Company company = Company.createCompany("회사", "Company", "desc");
            Department dept = Department.createDepartment("부서", "개발팀", null, "head1", 1L, "desc", "#000", company);
            UserDepartment userDept = UserDepartment.createUserDepartment(user, dept, YNType.Y);

            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = userService.searchUser(userId);

            // then
            assertThat(result.getName()).isEqualTo("이서준");
            assertThat(result.getMainDepartmentNameKR()).isEqualTo("개발팀");
        }

        @Test
        @DisplayName("성공 - 삭제된 메인 부서는 무시한다")
        void searchUserWithDeletedMainDepartment() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "test@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);

            Company company = Company.createCompany("회사", "Company", "desc");
            Department dept = Department.createDepartment("부서", "개발팀", null, "head1", 1L, "desc", "#000", company);
            UserDepartment userDept = UserDepartment.createUserDepartment(user, dept, YNType.Y);
            userDept.deleteUserDepartment();

            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = userService.searchUser(userId);

            // then
            assertThat(result.getName()).isEqualTo("이서준");
            assertThat(result.getMainDepartmentNameKR()).isNull();
        }

        @Test
        @DisplayName("성공 - 메인이 아닌 부서는 무시한다")
        void searchUserWithNonMainDepartment() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "test@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);

            Company company = Company.createCompany("회사", "Company", "desc");
            Department dept = Department.createDepartment("부서", "개발팀", null, "head1", 1L, "desc", "#000", company);
            UserDepartment userDept = UserDepartment.createUserDepartment(user, dept, YNType.N);

            given(userRepository.findByIdWithRolesAndPermissions(userId)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = userService.searchUser(userId);

            // then
            assertThat(result.getName()).isEqualTo("이서준");
            assertThat(result.getMainDepartmentNameKR()).isNull();
        }
    }

    @Nested
    @DisplayName("전체 유저 조회")
    class SearchUsers {
        @Test
        @DisplayName("성공 - 유저 목록을 반환한다")
        void searchUsersSuccess() {
            // given
            given(userRepository.findUsersWithRolesAndPermissions()).willReturn(List.of(
                    User.createUser("user1", "", "이서준", "", LocalDate.now(), OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR),
                    User.createUser("user2", "", "김서연", "", LocalDate.now(), OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR)
            ));

            // when
            List<UserServiceDto> result = userService.searchUsers();

            // then
            then(userRepository).should().findUsersWithRolesAndPermissions();
            assertThat(result).hasSize(2);
            assertThat(result).extracting("name").containsExactly("이서준", "김서연");
        }

        @Test
        @DisplayName("성공 - 유저가 없으면 빈 리스트를 반환한다")
        void searchUsersEmptyList() {
            // given
            given(userRepository.findUsersWithRolesAndPermissions()).willReturn(List.of());

            // when
            List<UserServiceDto> result = userService.searchUsers();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("유저 수정")
    class EditUser {
        @Test
        @DisplayName("성공 - 유저 정보가 수정된다")
        void editUserSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            UserServiceDto data = UserServiceDto.builder()
                    .id(userId)
                    .name("이하은")
                    .workTime("10 ~ 7")
                    .build();

            // when
            userService.editUser(data);

            // then
            then(userRepository).should().findById(userId);
            assertThat(user.getName()).isEqualTo("이하은");
            assertThat(user.getWorkTime()).isEqualTo("10 ~ 7");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저를 수정하려 하면 예외가 발생한다")
        void editUserFailNotFound() {
            // given
            String userId = "nonexistent";
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            UserServiceDto data = UserServiceDto.builder().id(userId).build();

            // when & then
            assertThatThrownBy(() -> userService.editUser(data))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("성공 - 대시보드 데이터가 수정된다")
        void editUserDashboardSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            UserServiceDto data = UserServiceDto.builder()
                    .id(userId)
                    .dashboard("{\"layout\": []}")
                    .build();

            // when
            userService.editUser(data);

            // then
            then(userRepository).should().findById(userId);
            assertThat(user.getDashboard()).isEqualTo("{\"layout\": []}");
        }

        @Test
        @DisplayName("성공 - 유저 권한이 수정된다")
        void editUserRolesSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            Role role = Role.createRole("ADMIN", "관리자", "관리자 역할");
            given(roleRepository.findByCode("ADMIN")).willReturn(Optional.of(role));

            UserServiceDto data = UserServiceDto.builder()
                    .id(userId)
                    .roleNames(List.of("ADMIN"))
                    .build();

            // when
            userService.editUser(data);

            // then
            then(userRepository).should().findById(userId);
            then(roleRepository).should().findByCode("ADMIN");
            assertThat(user.getRoles()).contains(role);
        }
    }

    @Nested
    @DisplayName("유저 삭제")
    class DeleteUser {
        @Test
        @DisplayName("성공 - 유저가 삭제된다")
        void deleteUserSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "이서준", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            userService.deleteUser(userId);

            // then
            then(userRepository).should().findById(userId);
            assertThat(user.getIsDeleted()).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저를 삭제하려 하면 예외가 발생한다")
        void deleteUserFailNotFound() {
            // given
            String userId = "nonexistent";
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("프로필 이미지 임시 저장")
    class SaveProfileImgInTempFolder {
        @Test
        @DisplayName("성공 - 프로필 이미지가 임시 폴더에 저장된다")
        void saveProfileImgSuccess() {
            // given
            MockMultipartFile mockFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
            String physicalFilename = "test.jpg_some-uuid";

            try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
                mocked.when(() -> PorestFile.generatePhysicalFilename(eq("test.jpg"), anyString()))
                        .thenReturn(physicalFilename);
                mocked.when(() -> PorestFile.save(eq(mockFile), anyString(), eq(physicalFilename), eq(messageResolver)))
                        .thenReturn(true);

                // when
                UserServiceDto result = userService.saveProfileImgInTempFolder(mockFile);

                // then
                assertThat(result.getProfileUrl()).contains(physicalFilename);
                assertThat(result.getProfileUUID()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("유저 초대")
    class InviteUser {
        @Test
        @DisplayName("성공 - 유저가 초대되고 이메일이 발송된다")
        void inviteUserSuccess() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .id("newuser")
                    .name("신규유저")
                    .email("new@test.com")
                    .company(OriginCompanyType.SKAX)
                    .workTime("9 ~ 6")
                    .joinDate(LocalDate.now())
                    .build();

            given(userRepository.findById("newuser")).willReturn(Optional.empty());
            willDoNothing().given(userRepository).save(any(User.class));
            willDoNothing().given(emailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // when
            UserServiceDto result = userService.inviteUser(data);

            // then
            then(userRepository).should().save(any(User.class));
            then(emailService).should().sendInvitationEmail(eq("new@test.com"), eq("신규유저"), anyString());
            assertThat(result.getId()).isEqualTo("newuser");
            assertThat(result.getInvitationStatus()).isEqualTo(StatusType.PENDING);
        }

        @Test
        @DisplayName("실패 - 중복된 ID로 초대하면 예외가 발생한다")
        void inviteUserFailDuplicateId() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .id("existinguser")
                    .name("신규유저")
                    .email("new@test.com")
                    .build();

            User existingUser = User.createUser("existinguser");
            given(userRepository.findById("existinguser")).willReturn(Optional.of(existingUser));

            // when & then
            assertThatThrownBy(() -> userService.inviteUser(data))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("초대 재전송")
    class ResendInvitation {
        @Test
        @DisplayName("성공 - 초대가 재전송된다")
        void resendInvitationSuccess() {
            // given
            String userId = "user1";
            User user = User.createInvitedUser(userId, "유저", "user@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            willDoNothing().given(emailService).sendInvitationEmail(anyString(), anyString(), anyString());

            // when
            UserServiceDto result = userService.resendInvitation(userId);

            // then
            then(emailService).should().sendInvitationEmail(eq("user@test.com"), eq("유저"), anyString());
            assertThat(result.getInvitationStatus()).isEqualTo(StatusType.PENDING);
        }
    }

    @Nested
    @DisplayName("초대된 유저 정보 수정")
    class EditInvitedUser {
        @Test
        @DisplayName("성공 - PENDING 상태 유저의 정보가 수정된다")
        void editInvitedUserSuccess() {
            // given
            String userId = "user1";
            User user = User.createInvitedUser(userId, "유저", "old@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            UserServiceDto data = UserServiceDto.builder()
                    .name("수정된유저")
                    .email("old@test.com")
                    .build();

            // when
            UserServiceDto result = userService.editInvitedUser(userId, data);

            // then
            assertThat(user.getName()).isEqualTo("수정된유저");
        }

        @Test
        @DisplayName("성공 - 이메일 변경 시 초대 이메일이 재발송된다")
        void editInvitedUserEmailChangedSuccess() {
            // given
            String userId = "user1";
            User user = User.createInvitedUser(userId, "유저", "old@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            willDoNothing().given(emailService).sendInvitationEmail(anyString(), anyString(), anyString());

            UserServiceDto data = UserServiceDto.builder()
                    .name("유저")
                    .email("new@test.com")
                    .build();

            // when
            userService.editInvitedUser(userId, data);

            // then
            then(emailService).should().sendInvitationEmail(eq("new@test.com"), eq("유저"), anyString());
        }

        @Test
        @DisplayName("실패 - PENDING 상태가 아닌 유저를 수정하면 예외가 발생한다")
        void editInvitedUserFailNotPending() {
            // given
            String userId = "user1";
            User user = User.createInvitedUser(userId, "유저", "user@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            user.completeRegistration(LocalDate.now(), YNType.N);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            UserServiceDto data = UserServiceDto.builder().name("수정").build();

            // when & then
            assertThatThrownBy(() -> userService.editInvitedUser(userId, data))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("회원가입 완료")
    class CompleteInvitedUserRegistration {
        @Test
        @DisplayName("성공 - 초대된 유저가 회원가입을 완료한다")
        void completeRegistrationSuccess() {
            // given
            User user = User.createInvitedUser("user1", "유저", "user@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            String token = user.getInvitationToken();

            given(userRepository.findByInvitationToken(token)).willReturn(Optional.of(user));

            UserServiceDto data = UserServiceDto.builder()
                    .invitationToken(token)
                    .birth(LocalDate.of(1990, 1, 1))
                    .lunarYN(YNType.N)
                    .build();

            // when
            String result = userService.completeInvitedUserRegistration(data);

            // then
            assertThat(result).isEqualTo("user1");
            assertThat(user.getInvitationStatus()).isEqualTo(StatusType.ACTIVE);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 토큰이면 예외가 발생한다")
        void completeRegistrationFailInvalidToken() {
            // given
            given(userRepository.findByInvitationToken("invalid-token")).willReturn(Optional.empty());

            UserServiceDto data = UserServiceDto.builder()
                    .invitationToken("invalid-token")
                    .build();

            // when & then
            assertThatThrownBy(() -> userService.completeInvitedUserRegistration(data))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 만료된 초대 토큰이면 예외가 발생한다")
        void completeRegistrationFailExpiredToken() {
            // given
            User user = User.createInvitedUser("user1", "유저", "user@test.com",
                    OriginCompanyType.SKAX, "9 ~ 6", LocalDate.now(), CountryCode.KR);
            String token = user.getInvitationToken();

            // 만료일을 과거로 설정
            ReflectionTestUtils.setField(user, "invitationExpiresAt", LocalDateTime.now().minusDays(1));

            given(userRepository.findByInvitationToken(token)).willReturn(Optional.of(user));

            UserServiceDto data = UserServiceDto.builder()
                    .invitationToken(token)
                    .birth(LocalDate.of(1990, 1, 1))
                    .lunarYN(YNType.N)
                    .build();

            // when & then
            assertThatThrownBy(() -> userService.completeInvitedUserRegistration(data))
                    .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("아이디 중복 확인")
    class CheckUserIdDuplicate {
        @Test
        @DisplayName("성공 - 중복이면 true를 반환한다")
        void checkUserIdDuplicateTrue() {
            // given
            User user = User.createUser("user1");
            given(userRepository.findById("user1")).willReturn(Optional.of(user));

            // when
            boolean result = userService.checkUserIdDuplicate("user1");

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 중복이 아니면 false를 반환한다")
        void checkUserIdDuplicateFalse() {
            // given
            given(userRepository.findById("newuser")).willReturn(Optional.empty());

            // when
            boolean result = userService.checkUserIdDuplicate("newuser");

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("메인 부서 존재 확인")
    class CheckUserHasMainDepartment {
        @Test
        @DisplayName("성공 - 메인 부서가 있으면 Y를 반환한다")
        void checkUserHasMainDepartmentY() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(departmentRepository.hasMainDepartment(userId)).willReturn(true);

            // when
            YNType result = userService.checkUserHasMainDepartment(userId);

            // then
            assertThat(result).isEqualTo(YNType.Y);
        }

        @Test
        @DisplayName("성공 - 메인 부서가 없으면 N을 반환한다")
        void checkUserHasMainDepartmentN() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(departmentRepository.hasMainDepartment(userId)).willReturn(false);

            // when
            YNType result = userService.checkUserHasMainDepartment(userId);

            // then
            assertThat(result).isEqualTo(YNType.N);
        }
    }

    @Nested
    @DisplayName("유저 존재 확인")
    class CheckUserExist {
        @Test
        @DisplayName("성공 - 존재하는 유저를 반환한다")
        void checkUserExistSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            User result = userService.checkUserExist(userId);

            // then
            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저면 예외가 발생한다")
        void checkUserExistFailNotFound() {
            // given
            given(userRepository.findById("nonexistent")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.checkUserExist("nonexistent"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 유저면 예외가 발생한다")
        void checkUserExistFailDeleted() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            user.deleteUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userService.checkUserExist(userId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("프로필 URL에서 물리적 파일명 추출")
    class ExtractPhysicalFileNameFromUrl {
        @Test
        @DisplayName("성공 - 물리적 파일명을 추출한다")
        void extractPhysicalFileNameFromUrlSuccess() {
            // given
            String profileUrl = "/media/temp/profile/test.jpg_some-uuid";

            // when
            String result = userService.extractPhysicalFileNameFromUrl(profileUrl);

            // then
            assertThat(result).isEqualTo("test.jpg_some-uuid");
        }

        @Test
        @DisplayName("성공 - profileUrl이 null이면 null을 반환한다")
        void extractPhysicalFileNameFromUrlNull() {
            // when
            String result = userService.extractPhysicalFileNameFromUrl(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("성공 - profileUrl이 빈 문자열이면 null을 반환한다")
        void extractPhysicalFileNameFromUrlEmpty() {
            // when
            String result = userService.extractPhysicalFileNameFromUrl("");

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("프로필 URL 생성")
    class GenerateProfileUrl {
        @Test
        @DisplayName("성공 - 프로필 URL을 생성한다")
        void generateProfileUrlSuccess() {
            // given
            String originalFilename = "test.jpg";
            String uuid = "some-uuid";

            try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
                mocked.when(() -> PorestFile.generatePhysicalFilename(originalFilename, uuid))
                        .thenReturn("test.jpg_some-uuid");

                // when
                String result = userService.generateProfileUrl(originalFilename, uuid);

                // then
                assertThat(result).contains("test.jpg_some-uuid");
            }
        }

        @Test
        @DisplayName("성공 - physicalFilename이 null이면 null을 반환한다")
        void generateProfileUrlNull() {
            // given
            String originalFilename = null;
            String uuid = "some-uuid";

            try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
                mocked.when(() -> PorestFile.generatePhysicalFilename(originalFilename, uuid))
                        .thenReturn(null);

                // when
                String result = userService.generateProfileUrl(originalFilename, uuid);

                // then
                assertThat(result).isNull();
            }
        }
    }

    @Nested
    @DisplayName("임시 프로필 이미지를 원본 폴더로 복사")
    class CopyTempProfileToOrigin {
        @Test
        @DisplayName("성공 - 프로필 이미지가 복사된다")
        void copyTempProfileToOriginSuccess() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .profileUrl("/media/temp/profile/test.jpg_some-uuid")
                    .profileUUID("some-uuid")
                    .build();

            try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
                mocked.when(() -> PorestFile.copy(anyString(), anyString(), eq(messageResolver)))
                        .thenReturn(true);
                mocked.when(() -> PorestFile.extractOriginalFilename(anyString(), isNull()))
                        .thenReturn("test.jpg");

                // when
                UserServiceDto result = userService.copyTempProfileToOrigin(data);

                // then
                assertThat(result.getProfileName()).isEqualTo("test.jpg");
                assertThat(result.getProfileUUID()).isEqualTo("some-uuid");
            }
        }

        @Test
        @DisplayName("성공 - profileUrl이 null이면 빈 결과를 반환한다")
        void copyTempProfileToOriginNullUrl() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .profileUrl(null)
                    .build();

            // when
            UserServiceDto result = userService.copyTempProfileToOrigin(data);

            // then
            assertThat(result.getProfileName()).isNull();
            assertThat(result.getProfileUUID()).isNull();
        }

        @Test
        @DisplayName("성공 - 파일 복사 실패 시 빈 결과를 반환한다")
        void copyTempProfileToOriginCopyFail() {
            // given
            UserServiceDto data = UserServiceDto.builder()
                    .profileUrl("/media/temp/profile/test.jpg_some-uuid")
                    .profileUUID("some-uuid")
                    .build();

            try (MockedStatic<PorestFile> mocked = mockStatic(PorestFile.class)) {
                mocked.when(() -> PorestFile.copy(anyString(), anyString(), eq(messageResolver)))
                        .thenReturn(false);

                // when
                UserServiceDto result = userService.copyTempProfileToOrigin(data);

                // then
                assertThat(result.getProfileName()).isNull();
                assertThat(result.getProfileUUID()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("대시보드 수정")
    class UpdateDashboard {
        @Test
        @DisplayName("성공 - 대시보드 데이터가 수정된다")
        void updateDashboardSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            String dashboardData = "{\"widgets\": [{\"type\": \"chart\", \"position\": 1}]}";

            // when
            UserServiceDto result = userService.updateDashboard(userId, dashboardData);

            // then
            then(userRepository).should().findById(userId);
            assertThat(result.getId()).isEqualTo(userId);
            assertThat(result.getDashboard()).isEqualTo(dashboardData);
            assertThat(user.getDashboard()).isEqualTo(dashboardData);
        }

        @Test
        @DisplayName("성공 - null로 대시보드를 초기화한다")
        void updateDashboardWithNull() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            user.updateDashboard("{\"old\": \"data\"}");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            UserServiceDto result = userService.updateDashboard(userId, null);

            // then
            assertThat(result.getDashboard()).isNull();
            assertThat(user.getDashboard()).isNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 유저면 예외가 발생한다")
        void updateDashboardFailNotFound() {
            // given
            String userId = "nonexistent";
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateDashboard(userId, "{\"test\": true}"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("실패 - 삭제된 유저면 예외가 발생한다")
        void updateDashboardFailDeleted() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);
            user.deleteUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> userService.updateDashboard(userId, "{\"test\": true}"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("승인권자 목록 조회")
    class GetUserApprovers {
        @Test
        @DisplayName("성공 - 승인권자 목록을 반환한다")
        void getUserApproversSuccess() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            Company company = Company.createCompany("회사", "Company", "desc");
            Department dept = Department.createDepartment("부서", "부서KR", null, "head1", 1L, "desc", "#000", company);
            ReflectionTestUtils.setField(dept, "id", 1L);

            User approver = User.createUser("head1", "", "부서장", "head@test.com", LocalDate.now(),
                    OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null, CountryCode.KR);

            given(departmentRepository.findApproversByUserId(userId)).willReturn(List.of(dept));
            given(userRepository.findByIdWithRolesAndPermissions("head1")).willReturn(Optional.of(approver));

            // when
            List<UserServiceDto> result = userService.getUserApprovers(userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("부서장");
            assertThat(result.get(0).getDepartmentName()).isEqualTo("부서");
        }

        @Test
        @DisplayName("성공 - 부서장이 없으면 해당 부서는 결과에서 제외된다")
        void getUserApproversWithNullApprover() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            Company company = Company.createCompany("회사", "Company", "desc");
            Department dept = Department.createDepartment("부서", "부서KR", null, "nonexistent", 1L, "desc", "#000", company);
            ReflectionTestUtils.setField(dept, "id", 1L);

            given(departmentRepository.findApproversByUserId(userId)).willReturn(List.of(dept));
            given(userRepository.findByIdWithRolesAndPermissions("nonexistent")).willReturn(Optional.empty());

            // when
            List<UserServiceDto> result = userService.getUserApprovers(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 삭제된 부서장은 결과에서 제외된다")
        void getUserApproversWithDeletedApprover() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            Company company = Company.createCompany("회사", "Company", "desc");
            Department dept = Department.createDepartment("부서", "부서KR", null, "deleted", 1L, "desc", "#000", company);
            ReflectionTestUtils.setField(dept, "id", 1L);

            User deletedApprover = User.createUser("deleted", "", "삭제된부서장", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            deletedApprover.deleteUser();

            given(departmentRepository.findApproversByUserId(userId)).willReturn(List.of(dept));
            given(userRepository.findByIdWithRolesAndPermissions("deleted")).willReturn(Optional.of(deletedApprover));

            // when
            List<UserServiceDto> result = userService.getUserApprovers(userId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 빈 목록을 반환한다")
        void getUserApproversEmpty() {
            // given
            String userId = "user1";
            User user = User.createUser(userId, "", "유저", "", LocalDate.now(),
                    OriginCompanyType.SKAX, "", YNType.N, null, null, CountryCode.KR);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(departmentRepository.findApproversByUserId(userId)).willReturn(List.of());

            // when
            List<UserServiceDto> result = userService.getUserApprovers(userId);

            // then
            assertThat(result).isEmpty();
        }
    }
}
