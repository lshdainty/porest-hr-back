package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.repository.UserQueryDslRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 유저 레포지토리 테스트")
class UserQueryDslRepositoryTest {
    @Autowired
    private UserQueryDslRepository userRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("유저 저장 및 단건 조회")
    void save() {
        // given
        User user = User.createUser(
                "testUser", "password", "홍길동", "hong@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );

        // when
        userRepository.save(user);
        em.flush();
        em.clear();

        // then
        Optional<User> findUser = userRepository.findById("testUser");
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getName()).isEqualTo("홍길동");
        assertThat(findUser.get().getEmail()).isEqualTo("hong@test.com");
    }

    @Test
    @DisplayName("단건 조회 시 유저가 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<User> findUser = userRepository.findById("nonExistent");

        // then
        assertThat(findUser.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("삭제된 유저도 findById로 조회됨 (em.find 사용)")
    void findByIdIncludesDeleted() {
        // given
        User user = User.createUser(
                "deletedUser", "password", "삭제유저", "deleted@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        userRepository.save(user);
        user.deleteUser();
        em.flush();
        em.clear();

        // when
        Optional<User> findUser = userRepository.findById("deletedUser");

        // then - findById는 em.find 사용으로 삭제 여부와 무관하게 조회됨
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getIsDeleted()).isEqualTo(YNType.Y);
    }

    @Test
    @DisplayName("전체 유저 조회")
    void findUsers() {
        // given
        userRepository.save(User.createUser(
                "user1", "password", "유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        ));
        userRepository.save(User.createUser(
                "user2", "password", "유저2", "user2@test.com",
                LocalDate.of(1991, 2, 2), OriginCompanyType.DTOL, "8 ~ 5",
                YNType.N, null, null, CountryCode.KR
        ));
        em.flush();
        em.clear();

        // when
        List<User> users = userRepository.findUsers();

        // then
        assertThat(users).hasSize(2);
        assertThat(users).extracting("id").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    @DisplayName("전체 유저 조회 시 삭제된 유저 제외")
    void findUsersExcludesDeleted() {
        // given
        User activeUser = User.createUser(
                "activeUser", "password", "활성유저", "active@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        User deletedUser = User.createUser(
                "deletedUser", "password", "삭제유저", "deleted@test.com",
                LocalDate.of(1991, 2, 2), OriginCompanyType.DTOL, "8 ~ 5",
                YNType.N, null, null, CountryCode.KR
        );
        userRepository.save(activeUser);
        userRepository.save(deletedUser);
        deletedUser.deleteUser();
        em.flush();
        em.clear();

        // when
        List<User> users = userRepository.findUsers();

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo("activeUser");
    }

    @Test
    @DisplayName("전체 유저 조회 시 유저가 없으면 빈 리스트 반환")
    void findUsersEmpty() {
        // when
        List<User> users = userRepository.findUsers();

        // then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("유저 수정")
    void updateUser() {
        // given
        User user = User.createUser(
                "testUser", "password", "원래이름", "original@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        userRepository.save(user);
        em.flush();
        em.clear();

        // when
        User foundUser = userRepository.findById("testUser").orElseThrow();
        foundUser.updateUser("수정이름", "updated@test.com", null,
                LocalDate.of(1991, 1, 1), OriginCompanyType.DTOL, "8 ~ 5",
                YNType.Y, null, null, null, null);
        em.flush();
        em.clear();

        // then
        User updatedUser = userRepository.findById("testUser").orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("수정이름");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
        assertThat(updatedUser.getWorkTime()).isEqualTo("8 ~ 5");
    }

    @Test
    @DisplayName("유저 삭제 (소프트 딜리트)")
    void deleteUser() {
        // given
        User user = User.createUser(
                "testUser", "password", "홍길동", "hong@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
        userRepository.save(user);
        em.flush();
        em.clear();

        // when
        User foundUser = userRepository.findById("testUser").orElseThrow();
        foundUser.deleteUser();
        em.flush();
        em.clear();

        // then - findById는 em.find 사용으로 삭제된 유저도 조회됨
        Optional<User> deletedUser = userRepository.findById("testUser");
        assertThat(deletedUser.isPresent()).isTrue();
        assertThat(deletedUser.get().getIsDeleted()).isEqualTo(YNType.Y);

        // findUsers에서는 삭제된 유저 제외
        List<User> users = userRepository.findUsers();
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("초대된 유저 생성 및 조회")
    void createInvitedUser() {
        // given
        User invitedUser = User.createInvitedUser(
                "invitedUser", "초대유저", "invited@test.com",
                OriginCompanyType.DTOL, "9 ~ 6",
                LocalDate.of(2025, 1, 1), CountryCode.KR
        );

        // when
        userRepository.save(invitedUser);
        em.flush();
        em.clear();

        // then
        Optional<User> findUser = userRepository.findById("invitedUser");
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getName()).isEqualTo("초대유저");
        assertThat(findUser.get().getInvitationToken()).isNotNull();
        assertThat(findUser.get().isInvitationValid()).isTrue();
    }
}
