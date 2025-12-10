package com.lshdainty.porest.repository;

import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.RoleQueryDslRepository;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({RoleQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 역할 레포지토리 테스트")
class RoleQueryDslRepositoryTest {
    @Autowired
    private RoleQueryDslRepository roleRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("역할 저장 및 ID로 조회")
    void save() {
        // given
        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");

        // when
        roleRepository.save(role);
        em.flush();
        em.clear();

        // then
        Optional<Role> findRole = roleRepository.findById(role.getId());
        assertThat(findRole.isPresent()).isTrue();
        assertThat(findRole.get().getCode()).isEqualTo("ADMIN");
        assertThat(findRole.get().getName()).isEqualTo("관리자");
    }

    @Test
    @DisplayName("ID로 조회 시 삭제된 역할 제외")
    void findByIdExcludesDeleted() {
        // given
        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        roleRepository.save(role);
        role.deleteRole();
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findById(role.getId());

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("ID로 조회 시 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<Role> findRole = roleRepository.findById(999L);

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("코드로 역할 조회")
    void findByCode() {
        // given
        Role role = Role.createRole("MANAGER", "매니저", "부서 관리자");
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByCode("MANAGER");

        // then
        assertThat(findRole.isPresent()).isTrue();
        assertThat(findRole.get().getName()).isEqualTo("매니저");
    }

    @Test
    @DisplayName("코드로 조회 시 삭제된 역할 제외")
    void findByCodeExcludesDeleted() {
        // given
        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        roleRepository.save(role);
        role.deleteRole();
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByCode("ADMIN");

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("코드로 조회 시 없으면 빈 Optional 반환")
    void findByCodeEmpty() {
        // when
        Optional<Role> findRole = roleRepository.findByCode("NONEXISTENT");

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("이름으로 역할 조회")
    void findByName() {
        // given
        Role role = Role.createRole("USER", "일반 사용자", "기본 사용자");
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByName("일반 사용자");

        // then
        assertThat(findRole.isPresent()).isTrue();
        assertThat(findRole.get().getCode()).isEqualTo("USER");
    }

    @Test
    @DisplayName("이름으로 조회 시 삭제된 역할 제외")
    void findByNameExcludesDeleted() {
        // given
        Role role = Role.createRole("USER", "일반 사용자", "기본 사용자");
        roleRepository.save(role);
        role.deleteRole();
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByName("일반 사용자");

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("이름으로 조회 시 없으면 빈 Optional 반환")
    void findByNameEmpty() {
        // when
        Optional<Role> findRole = roleRepository.findByName("존재하지 않는 역할");

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("전체 역할 조회")
    void findAllRoles() {
        // given
        roleRepository.save(Role.createRole("ADMIN", "관리자", "시스템 관리자"));
        roleRepository.save(Role.createRole("MANAGER", "매니저", "부서 관리자"));
        roleRepository.save(Role.createRole("USER", "일반 사용자", "기본 사용자"));
        em.flush();
        em.clear();

        // when
        List<Role> roles = roleRepository.findAllRoles();

        // then
        assertThat(roles).hasSize(3);
    }

    @Test
    @DisplayName("전체 역할 조회 시 삭제된 역할 제외")
    void findAllRolesExcludesDeleted() {
        // given
        Role activeRole = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        Role deletedRole = Role.createRole("DELETED", "삭제된 역할", "삭제됨");
        roleRepository.save(activeRole);
        roleRepository.save(deletedRole);
        deletedRole.deleteRole();
        em.flush();
        em.clear();

        // when
        List<Role> roles = roleRepository.findAllRoles();

        // then
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getCode()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("전체 역할 조회 시 코드순 정렬")
    void findAllRolesOrdered() {
        // given
        roleRepository.save(Role.createRole("USER", "일반 사용자", "기본 사용자"));
        roleRepository.save(Role.createRole("ADMIN", "관리자", "시스템 관리자"));
        roleRepository.save(Role.createRole("MANAGER", "매니저", "부서 관리자"));
        em.flush();
        em.clear();

        // when
        List<Role> roles = roleRepository.findAllRoles();

        // then
        assertThat(roles).hasSize(3);
        assertThat(roles.get(0).getCode()).isEqualTo("ADMIN");
        assertThat(roles.get(1).getCode()).isEqualTo("MANAGER");
        assertThat(roles.get(2).getCode()).isEqualTo("USER");
    }

    @Test
    @DisplayName("전체 역할이 없으면 빈 리스트 반환")
    void findAllRolesEmpty() {
        // when
        List<Role> roles = roleRepository.findAllRoles();

        // then
        assertThat(roles).isEmpty();
    }

    @Test
    @DisplayName("권한과 함께 역할 생성 및 조회")
    void createRoleWithPermissions() {
        // given
        Permission permission1 = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        Permission permission2 = Permission.createPermission(
                "USER:WRITE", "사용자 수정", "설명",
                ResourceType.USER, ActionType.WRITE
        );
        em.persist(permission1);
        em.persist(permission2);

        Role role = Role.createRoleWithPermissions(
                "ADMIN", "관리자", "시스템 관리자",
                List.of(permission1, permission2)
        );
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByIdWithPermissions(role.getId());

        // then
        assertThat(findRole.isPresent()).isTrue();
        assertThat(findRole.get().getRolePermissions()).hasSize(2);
    }

    @Test
    @DisplayName("ID로 권한과 함께 역할 조회")
    void findByIdWithPermissions() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        em.persist(permission);

        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        role.addPermission(permission);
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByIdWithPermissions(role.getId());

        // then
        assertThat(findRole.isPresent()).isTrue();
        assertThat(findRole.get().getPermissions()).hasSize(1);
    }

    @Test
    @DisplayName("ID로 권한과 함께 조회 시 삭제된 역할 제외")
    void findByIdWithPermissionsExcludesDeleted() {
        // given
        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        roleRepository.save(role);
        role.deleteRole();
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByIdWithPermissions(role.getId());

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("코드로 권한과 함께 역할 조회")
    void findByCodeWithPermissions() {
        // given
        Permission permission = Permission.createPermission(
                "VACATION:REQUEST", "휴가 신청", "설명",
                ResourceType.VACATION, ActionType.REQUEST
        );
        em.persist(permission);

        Role role = Role.createRole("USER", "일반 사용자", "기본 사용자");
        role.addPermission(permission);
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByCodeWithPermissions("USER");

        // then
        assertThat(findRole.isPresent()).isTrue();
        assertThat(findRole.get().getPermissions()).hasSize(1);
    }

    @Test
    @DisplayName("코드로 권한과 함께 조회 시 삭제된 역할 제외")
    void findByCodeWithPermissionsExcludesDeleted() {
        // given
        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        roleRepository.save(role);
        role.deleteRole();
        em.flush();
        em.clear();

        // when
        Optional<Role> findRole = roleRepository.findByCodeWithPermissions("ADMIN");

        // then
        assertThat(findRole.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("전체 역할 권한과 함께 조회")
    void findAllRolesWithPermissions() {
        // given
        Permission permission1 = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        Permission permission2 = Permission.createPermission(
                "VACATION:REQUEST", "휴가 신청", "설명",
                ResourceType.VACATION, ActionType.REQUEST
        );
        em.persist(permission1);
        em.persist(permission2);

        Role adminRole = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        adminRole.addPermission(permission1);
        adminRole.addPermission(permission2);
        roleRepository.save(adminRole);

        Role userRole = Role.createRole("USER", "일반 사용자", "기본 사용자");
        userRole.addPermission(permission2);
        roleRepository.save(userRole);
        em.flush();
        em.clear();

        // when
        List<Role> roles = roleRepository.findAllRolesWithPermissions();

        // then
        assertThat(roles).hasSize(2);
    }

    @Test
    @DisplayName("역할 수정")
    void updateRole() {
        // given
        Role role = Role.createRole("ADMIN", "원래 이름", "원래 설명");
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Role foundRole = roleRepository.findById(role.getId()).orElseThrow();
        foundRole.updateRole("수정된 이름", "수정된 설명", null);
        em.flush();
        em.clear();

        // then
        Role updatedRole = roleRepository.findById(role.getId()).orElseThrow();
        assertThat(updatedRole.getName()).isEqualTo("수정된 이름");
        assertThat(updatedRole.getDesc()).isEqualTo("수정된 설명");
    }

    @Test
    @DisplayName("역할에 권한 추가")
    void addPermission() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        em.persist(permission);

        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Role foundRole = roleRepository.findById(role.getId()).orElseThrow();
        foundRole.addPermission(permission);
        em.flush();
        em.clear();

        // then
        Role updatedRole = roleRepository.findByIdWithPermissions(role.getId()).orElseThrow();
        assertThat(updatedRole.getPermissions()).hasSize(1);
    }

    @Test
    @DisplayName("역할에서 권한 제거")
    void removePermission() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        em.persist(permission);

        Role role = Role.createRole("ADMIN", "관리자", "시스템 관리자");
        role.addPermission(permission);
        roleRepository.save(role);
        em.flush();
        em.clear();

        // when
        Role foundRole = roleRepository.findByIdWithPermissions(role.getId()).orElseThrow();
        foundRole.removePermission(permission);
        em.flush();
        em.clear();

        // then - findById로 역할 조회하고 getPermissions()로 권한 확인 (soft delete 필터링)
        Role updatedRole = roleRepository.findById(role.getId()).orElseThrow();
        assertThat(updatedRole.getPermissions()).isEmpty();
    }
}
