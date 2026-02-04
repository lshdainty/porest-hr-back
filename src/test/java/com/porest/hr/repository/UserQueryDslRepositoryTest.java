package com.porest.hr.repository;

import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.common.type.CompanyType;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.permission.domain.Permission;
import com.porest.hr.permission.domain.Role;
import com.porest.hr.permission.type.ActionType;
import com.porest.hr.permission.type.ResourceType;
import com.porest.hr.user.domain.User;
import com.porest.hr.user.repository.UserQueryDslRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // 테스트용 User 생성 헬퍼 메소드
    private User createTestUser(String id, String name, String email, CompanyType company) {
        return User.createUser(
                null, id, name, email,
                LocalDate.of(1990, 1, 1), company, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    private User createTestUser(String id, String name, String email) {
        return createTestUser(id, name, email, DefaultCompanyType.NONE);
    }

    @Test
    @DisplayName("유저 저장 및 단건 조회")
    void save() {
        // given
        User user = createTestUser("testUser", "홍길동", "hong@test.com");

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
        User user = createTestUser("deletedUser", "삭제유저", "deleted@test.com");
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
        userRepository.save(createTestUser("user1", "유저1", "user1@test.com"));
        userRepository.save(createTestUser("user2", "유저2", "user2@test.com"));
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
        User activeUser = createTestUser("activeUser", "활성유저", "active@test.com");
        User deletedUser = createTestUser("deletedUser", "삭제유저", "deleted@test.com");
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
        User user = createTestUser("testUser", "원래이름", "original@test.com");
        userRepository.save(user);
        em.flush();
        em.clear();

        // when
        User foundUser = userRepository.findById("testUser").orElseThrow();
        foundUser.updateUser("수정이름", "updated@test.com", null,
                LocalDate.of(1991, 1, 1), DefaultCompanyType.NONE, "8 ~ 17",
                YNType.Y, null, null, null, null);
        em.flush();
        em.clear();

        // then
        User updatedUser = userRepository.findById("testUser").orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("수정이름");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@test.com");
        assertThat(updatedUser.getWorkTime()).isEqualTo("8 ~ 17");
    }

    @Test
    @DisplayName("유저 삭제 (소프트 딜리트)")
    void deleteUser() {
        // given
        User user = createTestUser("testUser", "홍길동", "hong@test.com");
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
    @DisplayName("역할과 권한과 함께 유저 조회")
    void findByIdWithRolesAndPermissions() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        em.persist(permission);

        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        role.addPermission(permission);
        em.persist(role);

        User user = createTestUser("testUser", "홍길동", "hong@test.com");
        user.addRole(role);
        userRepository.save(user);
        em.flush();
        em.clear();

        // when
        Optional<User> findUser = userRepository.findByIdWithRolesAndPermissions("testUser");

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getRoles()).hasSize(1);
        assertThat(findUser.get().getRoles().get(0).getCode()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("역할과 권한과 함께 유저 조회 - 유저가 없으면 빈 Optional")
    void findByIdWithRolesAndPermissionsEmpty() {
        // when
        Optional<User> findUser = userRepository.findByIdWithRolesAndPermissions("nonExistent");

        // then
        assertThat(findUser.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("역할과 권한과 함께 유저 조회 - 역할이 없는 유저")
    void findByIdWithRolesAndPermissionsNoRoles() {
        // given
        User user = createTestUser("testUser", "홍길동", "hong@test.com");
        userRepository.save(user);
        em.flush();
        em.clear();

        // when
        Optional<User> findUser = userRepository.findByIdWithRolesAndPermissions("testUser");

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getRoles()).isEmpty();
    }

    @Test
    @DisplayName("전체 유저를 역할과 권한과 함께 조회")
    void findUsersWithRolesAndPermissions() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        em.persist(permission);

        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        role.addPermission(permission);
        em.persist(role);

        User user1 = createTestUser("user1", "유저1", "user1@test.com");
        user1.addRole(role);
        userRepository.save(user1);

        User user2 = createTestUser("user2", "유저2", "user2@test.com");
        userRepository.save(user2);
        em.flush();
        em.clear();

        // when
        List<User> users = userRepository.findUsersWithRolesAndPermissions();

        // then
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("전체 유저를 역할과 권한과 함께 조회 - 유저가 없으면 빈 리스트")
    void findUsersWithRolesAndPermissionsEmpty() {
        // when
        List<User> users = userRepository.findUsersWithRolesAndPermissions();

        // then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("전체 유저를 역할과 권한과 함께 조회 - 역할이 없는 유저들")
    void findUsersWithRolesAndPermissionsNoRoles() {
        // given
        User user1 = createTestUser("user1", "유저1", "user1@test.com");
        userRepository.save(user1);

        User user2 = createTestUser("user2", "유저2", "user2@test.com");
        userRepository.save(user2);
        em.flush();
        em.clear();

        // when
        List<User> users = userRepository.findUsersWithRolesAndPermissions();

        // then
        assertThat(users).hasSize(2);
        assertThat(users).allSatisfy(u -> assertThat(u.getRoles()).isEmpty());
    }

    @Test
    @DisplayName("수정일 기간으로 삭제된 유저 조회")
    void findDeletedUsersByModifyDateBetween() {
        // given
        User deletedUser = createTestUser("deletedUser", "삭제유저", "deleted@test.com");
        userRepository.save(deletedUser);
        deletedUser.deleteUser();
        em.flush();
        em.clear();

        LocalDateTime now = LocalDateTime.now();

        // when
        List<User> users = userRepository.findDeletedUsersByModifyDateBetween(
                now.minusDays(1), now.plusDays(1)
        );

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo("deletedUser");
        assertThat(users.get(0).getIsDeleted()).isEqualTo(YNType.Y);
    }

    @Test
    @DisplayName("수정일 기간으로 삭제된 유저 조회 - 활성 유저는 제외")
    void findDeletedUsersByModifyDateBetweenExcludesActive() {
        // given
        User activeUser = createTestUser("activeUser", "활성유저", "active@test.com");
        userRepository.save(activeUser);
        em.flush();
        em.clear();

        LocalDateTime now = LocalDateTime.now();

        // when
        List<User> users = userRepository.findDeletedUsersByModifyDateBetween(
                now.minusDays(1), now.plusDays(1)
        );

        // then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("수정일 기간으로 삭제된 유저 조회 - 기간 외 제외")
    void findDeletedUsersByModifyDateBetweenOutOfRange() {
        // given
        User deletedUser = createTestUser("deletedUser", "삭제유저", "deleted@test.com");
        userRepository.save(deletedUser);
        deletedUser.deleteUser();
        em.flush();
        em.clear();

        LocalDateTime now = LocalDateTime.now();

        // when - 과거 기간으로 조회
        List<User> users = userRepository.findDeletedUsersByModifyDateBetween(
                now.minusDays(10), now.minusDays(5)
        );

        // then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("전체 유저 조회 시 SYSTEM 계정 제외")
    void findUsersExcludesSystemAccount() {
        // given
        User normalUser = createTestUser("normalUser", "일반유저", "normal@test.com", DefaultCompanyType.NONE);
        User systemUser = createTestUser("systemUser", "시스템유저", "system@test.com", DefaultCompanyType.SYSTEM);
        userRepository.save(normalUser);
        userRepository.save(systemUser);
        em.flush();
        em.clear();

        // when
        List<User> users = userRepository.findUsers();

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo("normalUser");
        assertThat(users.get(0).getCompany()).isNotEqualTo(DefaultCompanyType.SYSTEM);
    }

    @Test
    @DisplayName("전체 유저를 역할과 권한과 함께 조회 시 SYSTEM 계정 제외")
    void findUsersWithRolesAndPermissionsExcludesSystemAccount() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        em.persist(permission);

        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        role.addPermission(permission);
        em.persist(role);

        User normalUser = createTestUser("normalUser", "일반유저", "normal@test.com", DefaultCompanyType.NONE);
        normalUser.addRole(role);
        userRepository.save(normalUser);

        User systemUser = createTestUser("systemUser", "시스템유저", "system@test.com", DefaultCompanyType.SYSTEM);
        systemUser.addRole(role);
        userRepository.save(systemUser);
        em.flush();
        em.clear();

        // when
        List<User> users = userRepository.findUsersWithRolesAndPermissions();

        // then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isEqualTo("normalUser");
        assertThat(users.get(0).getCompany()).isNotEqualTo(DefaultCompanyType.SYSTEM);
    }

    @Test
    @DisplayName("SSO User Row Id로 유저 조회")
    void findBySsoUserRowId() {
        // given
        User user = User.createUser(
                100L, "testUser", "홍길동", "hong@test.com",
                LocalDate.of(1990, 1, 1), DefaultCompanyType.NONE, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
        userRepository.save(user);
        em.flush();
        em.clear();

        // when
        Optional<User> findUser = userRepository.findBySsoUserRowId(100L);

        // then
        assertThat(findUser.isPresent()).isTrue();
        assertThat(findUser.get().getId()).isEqualTo("testUser");
        assertThat(findUser.get().getSsoUserRowId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("SSO User Row Id로 유저 조회 - 없으면 빈 Optional")
    void findBySsoUserRowIdEmpty() {
        // when
        Optional<User> findUser = userRepository.findBySsoUserRowId(999L);

        // then
        assertThat(findUser.isEmpty()).isTrue();
    }
}
