package com.lshdainty.porest.service;

import com.lshdainty.porest.domain.User;
import com.lshdainty.porest.repository.UserRepositoryImpl;
import com.lshdainty.porest.service.dto.UserServiceDto;
import com.lshdainty.porest.type.CompanyType;
import com.lshdainty.porest.type.DepartmentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("유저 서비스 테스트")
class UserServiceTest {
    // 삭제하지 말 것 (NullpointException 발생)
    @Mock
    private MessageSource ms;
    @Mock
    private UserRepositoryImpl userRepositoryImpl;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원 가입 테스트 - 성공")
    void signUpSuccessTest() {
        // Given
        willDoNothing().given(userRepositoryImpl).save(any(User.class));

        // When
        userService.join(UserServiceDto.builder().build());

        // Then
        then(userRepositoryImpl).should().save(any(User.class));
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
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 실패 (유저 없음)")
    void findUserFailTestNotFoundUser() {
        // Given
        String id = "";
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());

        // When, Then
        assertThrows(IllegalArgumentException.class, () -> userService.findUser(id));
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

        // When, Then
        assertThrows(IllegalArgumentException.class, () -> userService.findUser(id));
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
    @DisplayName("유저 수정 테스트 - 성공")
    void editUserSuccessTest() {
        // Given
        String id = "user1";
        User user = User.createUser(id, "", "", "", "", CompanyType.SKAX, DepartmentType.SKC, "", "");

        setUserId(user, id);
        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When
        userService.editUser(UserServiceDto.builder()
                .id(id)
                .name("이하은")
                .workTime("10 ~ 7")
                .build()
        );

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(user.getName()).isEqualTo("이하은");
        assertThat(user.getWorkTime()).isEqualTo("10 ~ 7");
    }

    @Test
    @DisplayName("유저 수정 테스트 - 실패 (유저 없음)")
    void editUserFailTestNotFoundUser() {
        // Given
        String id = "";
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.editUser(UserServiceDto.builder().id(id).build()));
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
        String id = "";
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(id));
        then(userRepositoryImpl).should().findById(id);
    }

    // 테스트 헬퍼 메서드
    private void setUserId(User user, String id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
