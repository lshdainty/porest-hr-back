package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
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
        String name = "이서준";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "ADMIN";
        String lunar = "N";
        willDoNothing().given(userRepositoryImpl).save(any(User.class));

        // When
        userService.join(name, birth, employ, workTime, lunar);

        // Then
        then(userRepositoryImpl).should().save(any(User.class));
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 성공")
    void findUserSuccessTest() {
        // Given
        Long id = 1L;
        String name = "이서준";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "ADMIN";
        String lunar = "N";
        User user = User.createUser(name, birth, employ, workTime, lunar);

        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When
        User findUser = userService.findUser(id);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(findUser).isNotNull();
        assertThat(findUser.getName()).isEqualTo(name);
        assertThat(findUser.getBirth()).isEqualTo(birth);
        assertThat(findUser.getWorkTime()).isEqualTo(workTime);
        assertThat(findUser.getEmploy()).isEqualTo(employ);
        assertThat(findUser.getLunarYN()).isEqualTo(lunar);
        assertThat(findUser.getDelYN()).isEqualTo("N");
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 실패 (유저 없음)")
    void findUserFailTestNotFoundUser() {
        // Given
        Long id = 900L;
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());

        // When, Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.findUser(id));
        then(userRepositoryImpl).should().findById(id);
    }

    @Test
    @DisplayName("단건 유저 조회 테스트 - 실패 (삭제된 유저 조회)")
    void findUserFailTestDeletedUser() {
        // Given
        Long id = 1L;
        String name = "이서준";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "ADMIN";
        String lunar = "N";
        User user = User.createUser(name, birth, employ, workTime, lunar);

        user.deleteUser();
        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When, Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.findUser(id));
        then(userRepositoryImpl).should().findById(id);
    }

    @Test
    @DisplayName("전체 유저 조회 테스트 - 성공")
    void findUsersSuccessTest() {
        // Given
        given(userRepositoryImpl.findUsers()).willReturn(List.of(
                User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N"),
                User.createUser("김서연", "19701026", "BP", "8 ~ 5", "N"),
                User.createUser("김지후", "19740115", "BP", "10 ~ 7", "Y")
        ));

        // When
        List<User> findUsers = userService.findUsers();

        // Then
        then(userRepositoryImpl).should().findUsers();
        assertThat(findUsers).hasSize(3);
        assertThat(findUsers)
                .extracting(User::getName)
                .containsExactlyInAnyOrder("이서준", "김서연", "김지후");
    }

    @Test
    @DisplayName("유저 수정 테스트 - 성공")
    void editUserSuccessTest() {
        // Given
        Long id = 1L;
        String name = "이서준";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "ADMIN";
        String lunar = "N";
        User user = User.createUser(name, birth, employ, workTime, lunar);

        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When
        name = "이하은";
        workTime = "10 ~ 7";
        userService.editUser(id, name, null, null, workTime, null);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getBirth()).isEqualTo(birth);
        assertThat(user.getWorkTime()).isEqualTo(workTime);
        assertThat(user.getEmploy()).isEqualTo(employ);
        assertThat(user.getLunarYN()).isEqualTo(lunar);
    }

    @Test
    @DisplayName("유저 수정 테스트 - 실패 (유저 없음)")
    void editUserFailTestNotFoundUser() {
        // Given
        Long id = 900L;
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.editUser(id, "이하은", null, null, null, null));
        then(userRepositoryImpl).should().findById(id);
    }

    @Test
    @DisplayName("유저 삭제 테스트 - 성공")
    void deleteUserSuccessTest() {
        // Given
        Long id = 1L;
        String name = "이서준";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "ADMIN";
        String lunar = "N";
        User user = User.createUser(name, birth, employ, workTime, lunar);

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
        Long id = 900L;
        given(userRepositoryImpl.findById(id)).willReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(id));
        then(userRepositoryImpl).should().findById(id);
    }
}
