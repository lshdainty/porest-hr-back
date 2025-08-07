package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.repository.UserRepositoryImpl;
import com.lshdainty.myhr.service.dto.UserServiceDto;
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
        String id = "test1";
        String name = "이서준";
        String email = "test1@gmail.com";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "company";
        String lunar = "N";
        User user = User.createUser(id, "", name, email, birth, employ, workTime, lunar);

        given(userRepositoryImpl.findById(id)).willReturn(Optional.of(user));

        // When
        User findUser = userService.findUser(id);

        // Then
        then(userRepositoryImpl).should().findById(id);
        assertThat(findUser).isNotNull();
        assertThat(findUser.getName()).isEqualTo(name);
        assertThat(findUser.getEmail()).isEqualTo(email);
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
        String id = "test1";
        String name = "이서준";
        String email = "test1@gmail.com";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "company";
        String lunar = "N";
        User user = User.createUser(id, "", name, email, birth, employ, workTime, lunar);

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
                User.createUser("test1", "", "이서준", "test1@gmail.com", "19700723", "company", "9 ~ 6", "N"),
                User.createUser("test2", "", "김서연", "test2@gmail.com", "19701026", "company2", "8 ~ 5", "N"),
                User.createUser("test3", "", "김지후", "test3@gmail.com", "19740115", "company3", "10 ~ 7", "Y")
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
        String id = "test1";
        String name = "이서준";
        String email = "test1@gmail.com";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "company";
        String lunar = "N";
        User user = User.createUser(id, "", name, email, birth, employ, workTime, lunar);

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
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getBirth()).isEqualTo(birth);
        assertThat(user.getWorkTime()).isEqualTo("10 ~ 7");
        assertThat(user.getEmploy()).isEqualTo(employ);
        assertThat(user.getLunarYN()).isEqualTo(lunar);
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
        String id = "test1";
        String name = "이서준";
        String email = "test1@gmail.com";
        String birth = "19700723";
        String workTime = "9 ~ 6";
        String employ = "company";
        String lunar = "N";
        User user = User.createUser(id, "", name, email, birth, employ, workTime, lunar);

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
