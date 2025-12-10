package com.lshdainty.porest.repository;

import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.repository.PermissionQueryDslRepository;
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
@Import({PermissionQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 권한 레포지토리 테스트")
class PermissionQueryDslRepositoryTest {
    @Autowired
    private PermissionQueryDslRepository permissionRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("권한 저장 및 ID로 조회")
    void save() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "사용자 정보 조회 권한",
                ResourceType.USER, ActionType.READ
        );

        // when
        permissionRepository.save(permission);
        em.flush();
        em.clear();

        // then
        Optional<Permission> findPermission = permissionRepository.findById(permission.getId());
        assertThat(findPermission.isPresent()).isTrue();
        assertThat(findPermission.get().getCode()).isEqualTo("USER:READ");
        assertThat(findPermission.get().getName()).isEqualTo("사용자 조회");
    }

    @Test
    @DisplayName("ID로 조회 시 삭제된 권한 제외")
    void findByIdExcludesDeleted() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "사용자 정보 조회 권한",
                ResourceType.USER, ActionType.READ
        );
        permissionRepository.save(permission);
        permission.deletePermission();
        em.flush();
        em.clear();

        // when
        Optional<Permission> findPermission = permissionRepository.findById(permission.getId());

        // then
        assertThat(findPermission.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("ID로 조회 시 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<Permission> findPermission = permissionRepository.findById(999L);

        // then
        assertThat(findPermission.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("코드로 권한 조회")
    void findByCode() {
        // given
        Permission permission = Permission.createPermission(
                "VACATION:REQUEST", "휴가 신청", "휴가 신청 권한",
                ResourceType.VACATION, ActionType.REQUEST
        );
        permissionRepository.save(permission);
        em.flush();
        em.clear();

        // when
        Optional<Permission> findPermission = permissionRepository.findByCode("VACATION:REQUEST");

        // then
        assertThat(findPermission.isPresent()).isTrue();
        assertThat(findPermission.get().getResource()).isEqualTo(ResourceType.VACATION);
        assertThat(findPermission.get().getAction()).isEqualTo(ActionType.REQUEST);
    }

    @Test
    @DisplayName("코드로 조회 시 삭제된 권한 제외")
    void findByCodeExcludesDeleted() {
        // given
        Permission permission = Permission.createPermission(
                "USER:WRITE", "사용자 수정", "사용자 정보 수정 권한",
                ResourceType.USER, ActionType.WRITE
        );
        permissionRepository.save(permission);
        permission.deletePermission();
        em.flush();
        em.clear();

        // when
        Optional<Permission> findPermission = permissionRepository.findByCode("USER:WRITE");

        // then
        assertThat(findPermission.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("코드로 조회 시 없으면 빈 Optional 반환")
    void findByCodeEmpty() {
        // when
        Optional<Permission> findPermission = permissionRepository.findByCode("NONEXISTENT");

        // then
        assertThat(findPermission.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("전체 권한 조회")
    void findAllPermissions() {
        // given
        permissionRepository.save(Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        ));
        permissionRepository.save(Permission.createPermission(
                "USER:WRITE", "사용자 수정", "설명",
                ResourceType.USER, ActionType.WRITE
        ));
        permissionRepository.save(Permission.createPermission(
                "VACATION:READ", "휴가 조회", "설명",
                ResourceType.VACATION, ActionType.READ
        ));
        em.flush();
        em.clear();

        // when
        List<Permission> permissions = permissionRepository.findAllPermissions();

        // then
        assertThat(permissions).hasSize(3);
    }

    @Test
    @DisplayName("전체 권한 조회 시 삭제된 권한 제외")
    void findAllPermissionsExcludesDeleted() {
        // given
        Permission activePermission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        Permission deletedPermission = Permission.createPermission(
                "USER:WRITE", "사용자 수정", "설명",
                ResourceType.USER, ActionType.WRITE
        );
        permissionRepository.save(activePermission);
        permissionRepository.save(deletedPermission);
        deletedPermission.deletePermission();
        em.flush();
        em.clear();

        // when
        List<Permission> permissions = permissionRepository.findAllPermissions();

        // then
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getCode()).isEqualTo("USER:READ");
    }

    @Test
    @DisplayName("전체 권한 조회 시 정렬 확인 (리소스, 액션순)")
    void findAllPermissionsOrdered() {
        // given
        permissionRepository.save(Permission.createPermission(
                "VACATION:WRITE", "휴가 수정", "설명",
                ResourceType.VACATION, ActionType.WRITE
        ));
        permissionRepository.save(Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        ));
        permissionRepository.save(Permission.createPermission(
                "USER:WRITE", "사용자 수정", "설명",
                ResourceType.USER, ActionType.WRITE
        ));
        em.flush();
        em.clear();

        // when
        List<Permission> permissions = permissionRepository.findAllPermissions();

        // then
        assertThat(permissions).hasSize(3);
        // USER가 VACATION보다 먼저, 같은 리소스는 액션순
        assertThat(permissions.get(0).getResource()).isEqualTo(ResourceType.USER);
    }

    @Test
    @DisplayName("전체 권한이 없으면 빈 리스트 반환")
    void findAllPermissionsEmpty() {
        // when
        List<Permission> permissions = permissionRepository.findAllPermissions();

        // then
        assertThat(permissions).isEmpty();
    }

    @Test
    @DisplayName("리소스로 권한 조회")
    void findByResource() {
        // given
        permissionRepository.save(Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        ));
        permissionRepository.save(Permission.createPermission(
                "USER:WRITE", "사용자 수정", "설명",
                ResourceType.USER, ActionType.WRITE
        ));
        permissionRepository.save(Permission.createPermission(
                "VACATION:READ", "휴가 조회", "설명",
                ResourceType.VACATION, ActionType.READ
        ));
        em.flush();
        em.clear();

        // when
        List<Permission> permissions = permissionRepository.findByResource("USER");

        // then
        assertThat(permissions).hasSize(2);
        assertThat(permissions).extracting("resource").containsOnly(ResourceType.USER);
    }

    @Test
    @DisplayName("리소스로 조회 시 삭제된 권한 제외")
    void findByResourceExcludesDeleted() {
        // given
        Permission activePermission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        Permission deletedPermission = Permission.createPermission(
                "USER:WRITE", "사용자 수정", "설명",
                ResourceType.USER, ActionType.WRITE
        );
        permissionRepository.save(activePermission);
        permissionRepository.save(deletedPermission);
        deletedPermission.deletePermission();
        em.flush();
        em.clear();

        // when
        List<Permission> permissions = permissionRepository.findByResource("USER");

        // then
        assertThat(permissions).hasSize(1);
    }

    @Test
    @DisplayName("리소스와 액션으로 권한 조회")
    void findByResourceAndAction() {
        // given
        permissionRepository.save(Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        ));
        permissionRepository.save(Permission.createPermission(
                "USER:WRITE", "사용자 수정", "설명",
                ResourceType.USER, ActionType.WRITE
        ));
        em.flush();
        em.clear();

        // when
        Optional<Permission> permission = permissionRepository.findByResourceAndAction("USER", "READ");

        // then
        assertThat(permission.isPresent()).isTrue();
        assertThat(permission.get().getCode()).isEqualTo("USER:READ");
    }

    @Test
    @DisplayName("리소스와 액션으로 조회 시 삭제된 권한 제외")
    void findByResourceAndActionExcludesDeleted() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "사용자 조회", "설명",
                ResourceType.USER, ActionType.READ
        );
        permissionRepository.save(permission);
        permission.deletePermission();
        em.flush();
        em.clear();

        // when
        Optional<Permission> findPermission = permissionRepository.findByResourceAndAction("USER", "READ");

        // then
        assertThat(findPermission.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("리소스와 액션으로 조회 시 없으면 빈 Optional 반환")
    void findByResourceAndActionEmpty() {
        // when
        Optional<Permission> permission = permissionRepository.findByResourceAndAction("USER", "MANAGE");

        // then
        assertThat(permission.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("권한 수정")
    void updatePermission() {
        // given
        Permission permission = Permission.createPermission(
                "USER:READ", "원래 이름", "원래 설명",
                ResourceType.USER, ActionType.READ
        );
        permissionRepository.save(permission);
        em.flush();
        em.clear();

        // when
        Permission foundPermission = permissionRepository.findById(permission.getId()).orElseThrow();
        foundPermission.updatePermission("수정된 이름", "수정된 설명", ResourceType.VACATION, ActionType.WRITE);
        em.flush();
        em.clear();

        // then
        Permission updatedPermission = permissionRepository.findById(permission.getId()).orElseThrow();
        assertThat(updatedPermission.getName()).isEqualTo("수정된 이름");
        assertThat(updatedPermission.getDesc()).isEqualTo("수정된 설명");
        assertThat(updatedPermission.getResource()).isEqualTo(ResourceType.VACATION);
        assertThat(updatedPermission.getAction()).isEqualTo(ActionType.WRITE);
    }
}
