package com.lshdainty.porest.repository;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserRepositoryImpl;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.common.type.YNType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
@Import({UserRepositoryImpl.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 유저 레포지토리 테스트")
class UserRepositoryImplTest {
    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("유저 등록 및 단건 조회")
    void save() {
        // given
        User user = User.createUser("user1", "", "홍길동", "", LocalDate.of(1970, 2, 4), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null);

        // when
        userRepositoryImpl.save(user);
        em.flush();
        em.clear();

        // then
        Optional<User> findUser = userRepositoryImpl.findById(user.getId());
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getId()).isEqualTo("user1");
        assertThat(findUser.get().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("단건 조회 시 유저가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<User> findUser = userRepositoryImpl.findById("");

        // then
        assertThat(findUser.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("isDeleted가 N인 모든 유저가 보여야한다.")
    void getUsers() {
        // given
        User user1 = User.createUser("user1", "", "", "", LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "", YNType.N, null, null);
        User user2 = User.createUser("user2", "", "", "", LocalDate.of(1991, 2, 2), OriginCompanyType.SKAX, "", YNType.N, null, null);
        User user3 = User.createUser("user3", "", "", "", LocalDate.of(1992, 3, 3), OriginCompanyType.SKAX, "", YNType.N, null, null);
        User user4 = User.createUser("user4", "", "", "", LocalDate.of(1993, 4, 4), OriginCompanyType.SKAX, "", YNType.N, null, null);

        for (User user : List.of(user1, user2, user3, user4)) {
            userRepositoryImpl.save(user);
        }

        // when
        user3.deleteUser();
        em.flush();
        em.clear();
        List<User> findUsers = userRepositoryImpl.findUsers();

        // then
        assertThat(findUsers.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("유저가 없어도 Null이 반환되면 안된다.")
    void getUsersEmpty() {
        // given & when
        List<User> users = userRepositoryImpl.findUsers();

        // then
        assertThat(users.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("삭제된 유저는 isDeleted 상태값이 Y여야 한다.")
    void deleteUser() {
        // given
        User user = User.createUser("user1", "", "", "", LocalDate.of(1990, 1, 1), OriginCompanyType.SKAX, "", YNType.N, null, null);
        userRepositoryImpl.save(user);

        // when
        user.deleteUser();
        em.flush();
        em.clear();
        Optional<User> findUser = userRepositoryImpl.findById(user.getId());

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getIsDeleted()).isEqualTo(YNType.Y);
    }

    @Test
    @DisplayName("유저 수정")
    void updateUser() {
        // given
        User user = User.createUser("user1", "", "홍길동", "", LocalDate.of(1970, 2, 4), OriginCompanyType.SKAX, "9 ~ 6", YNType.N, null, null);
        userRepositoryImpl.save(user);

        // when
        user.updateUser("이서준", null, null, null, null, "10 ~ 7", null, null, null, null);
        em.flush();
        em.clear();
        Optional<User> findUser = userRepositoryImpl.findById(user.getId());

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getName()).isEqualTo("이서준");
        assertThat(findUser.get().getWorkTime()).isEqualTo("10 ~ 7");
    }

    @Test
    @DisplayName("초대 토큰으로 유저 조회")
    void findByInvitationToken() {
        // given
        User user = User.createInvitedUser("user1", "홍길동", "test@example.com", OriginCompanyType.SKAX, "9 ~ 6", LocalDate.of(2025, 1, 1));
        userRepositoryImpl.save(user);
        String token = user.getInvitationToken();
        em.flush();
        em.clear();

        // when
        Optional<User> findUser = userRepositoryImpl.findByInvitationToken(token);

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("초대 토큰이 없는 경우 빈 Optional 반환")
    void findByInvitationTokenEmpty() {
        // given & when
        Optional<User> findUser = userRepositoryImpl.findByInvitationToken("invalid-token");

        // then
        assertThat(findUser.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("삭제된 유저는 초대 토큰으로 조회되지 않는다.")
    void findByInvitationTokenDeletedUser() {
        // given
        User user = User.createInvitedUser("user1", "홍길동", "test@example.com", OriginCompanyType.SKAX, "9 ~ 6", LocalDate.of(2025, 1, 1));
        userRepositoryImpl.save(user);
        String token = user.getInvitationToken();
        user.deleteUser();
        em.flush();
        em.clear();

        // when
        Optional<User> findUser = userRepositoryImpl.findByInvitationToken(token);

        // then
        assertThat(findUser.isEmpty()).isTrue();
    }
}
