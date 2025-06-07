//package com.lshdainty.myhr.service;
//
//import com.lshdainty.myhr.domain.User;
//import com.lshdainty.myhr.repository.UserRepositoryImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.MessageSource;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.*;
//
//@Slf4j
//@ExtendWith(MockitoExtension.class)
//@DisplayName("유저 서비스 테스트")
//class UserServiceTest {
//    @Mock
//    private MessageSource ms;
//
//    @Mock
//    private UserRepositoryImpl userRepositoryImpl;
//
//    @InjectMocks
//    private UserService userService;
//
//    @Test
//    @DisplayName("회원 가입 테스트 - 성공")
//    void signUpSuccessTest() {
//        // given
//        String name = "이서준";
//        String birth = "19700723";
//        String workTime = "9 ~ 6";
//        String employ = "ADMIN";
//        String lunar = "N";
//        willDoNothing().given(userRepositoryImpl).save(any(User.class));
//
//        // when
//        Long userId = userService.join(name, birth, employ, workTime, lunar);
//
//        // then
//        then(userRepositoryImpl).should().save(any(User.class));
//    }
//
//    @Test
//    @DisplayName("단건 유저 조회 테스트 - 성공")
//    void findUserSuccessTest() {
//        // given
//        Long id = 1L;
//        String name = "이서준";
//        String birth = "19700723";
//        String workTime = "9 ~ 6";
//        String employ = "ADMIN";
//        String lunar = "N";
//        User user = User.createUser(name, birth, employ, workTime, lunar);
//
//        // Reflection을 사용하여 seq 설정 (테스트용)
//        try {
//            java.lang.reflect.Field field = User.class.getDeclaredField("id");
//            field.setAccessible(true);
//            field.set(user, id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        given(userRepositoryImpl.findById(id)).willReturn(user);
//
//        // when
//        User findUser = userRepositoryImpl.findById(id);
//
//        // then
//        then(userRepositoryImpl).should().findById(id);
//        assertThat(findUser).isNotNull();
//        assertThat(findUser.getId()).isEqualTo(id);
//        assertThat(findUser.getName()).isEqualTo(name);
//        assertThat(findUser.getBirth()).isEqualTo(birth);
//        assertThat(findUser.getWorkTime()).isEqualTo(workTime);
//        assertThat(findUser.getEmploy()).isEqualTo(employ);
//        assertThat(findUser.getLunarYN()).isEqualTo(lunar);
//        assertThat(findUser.getDelYN()).isEqualTo("N");
//    }
//
//    @Test
//    @DisplayName("단건 유저 조회 테스트 - 실패 (유저 없음)")
//    void findUserFailTest() {
//        // given
//        Long id = 900L;
//        given(userRepositoryImpl.findById(id)).willReturn(null);
//
//        // when, then
//        assertThrows(IllegalArgumentException.class,
//                () -> userService.findUser(id));
//        then(userRepositoryImpl).should().findById(id);
//    }
//
//    @Test
//    @DisplayName("전체 유저 조회 테스트 - 성공")
//    void findUsersSuccessTest() {
//        // given
//        given(userRepositoryImpl.findUsers()).willReturn(List.of(
//                User.createUser("이서준", "19700723", "ADMIN", "9 ~ 6", "N"),
//                User.createUser("김서연", "19701026", "BP", "8 ~ 5", "N"),
//                User.createUser("김지후", "19740115", "BP", "10 ~ 7", "Y")
//        ));
//
//        // when
//        List<User> findUsers = userService.findUsers();
//
//        // then
//        then(userRepositoryImpl).should().findUsers();
//        assertThat(findUsers).hasSize(3);
//        assertThat(findUsers)
//                .extracting(User::getName)
//                .containsExactlyInAnyOrder("이서준", "김서연", "김지후");
//    }
//
//    @Test
//    @DisplayName("유저 수정 테스트 - 성공")
//    void editUserSuccessTest() {
//        // given
//        Long id = 1L;
//        String name = "이서준";
//        String birth = "19700723";
//        String workTime = "9 ~ 6";
//        String employ = "ADMIN";
//        String lunar = "N";
//        User user = User.createUser(name, birth, employ, workTime, lunar);
//
//        try {
//            java.lang.reflect.Field field = User.class.getDeclaredField("id");
//            field.setAccessible(true);
//            field.set(user, id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        given(userRepositoryImpl.findById(id)).willReturn(user);
//
//        // when
//        name = "이하은";
//        workTime = "10 ~ 7";
//        userService.editUser(id, name, null, null, workTime, null);
//
//        // then
//        then(userRepositoryImpl).should().findById(id);
//        assertThat(user.getId()).isEqualTo(id);
//        assertThat(user.getName()).isEqualTo(name);
//        assertThat(user.getBirth()).isEqualTo(birth);
//        assertThat(user.getWorkTime()).isEqualTo(workTime);
//        assertThat(user.getEmploy()).isEqualTo(employ);
//        assertThat(user.getLunarYN()).isEqualTo(lunar);
//    }
//
//    @Test
//    @DisplayName("유저 수정 테스트 - 실패 (유저 없음)")
//    void editUserFailTest() {
//        // given
//        Long id = 900L;
//        given(userRepositoryImpl.findById(id)).willReturn(null);
//
//        // when & then
//        assertThrows(IllegalArgumentException.class,
//                () -> userService.editUser(id, "이하은", null, null, null, null));
//        then(userRepositoryImpl).should().findById(id);
//    }
//
//    @Test
//    @DisplayName("유저 삭제 테스트 - 성공")
//    void deleteUserSuccessTest() {
//        // given
//        Long id = 1L;
//        String name = "이서준";
//        String birth = "19700723";
//        String workTime = "9 ~ 6";
//        String employ = "ADMIN";
//        String lunar = "N";
//        User user = User.createUser(name, birth, employ, workTime, lunar);
//
//        try {
//            java.lang.reflect.Field field = User.class.getDeclaredField("id");
//            field.setAccessible(true);
//            field.set(user, id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        given(userRepositoryImpl.findById(id)).willReturn(user);
//
//        // when
//        userService.deleteUser(id);
//
//        // then
//        then(userRepositoryImpl).should().findById(id);
//        assertThat(user.getDelYN()).isEqualTo("Y");
//    }
//
//    @Test
//    @DisplayName("유저 삭제 테스트 - 실패 (유저 없음)")
//    void deleteUserFailTest() {
//        // given
//        Long id = 900L;
//        given(userRepositoryImpl.findById(id)).willReturn(null);
//
//        // when & then
//        assertThrows(IllegalArgumentException.class,
//                () -> userService.deleteUser(id));
//        then(userRepositoryImpl).should().findById(id);
//    }
//}
