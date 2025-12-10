package com.lshdainty.porest.permission.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.permission.domain.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role Repository 구현체<br>
 * QueryDSL을 활용한 역할 조회 구현
 */
@Repository
@Primary
@RequiredArgsConstructor
public class RoleQueryDslRepository implements RoleRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(Role role) {
        em.persist(role);
    }

    @Override
    public Optional<Role> findById(Long id) {
        QRole role = QRole.role;

        Role result = queryFactory
                .selectFrom(role)
                .where(
                        role.id.eq(id),
                        role.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        QRole role = QRole.role;

        Role result = queryFactory
                .selectFrom(role)
                .where(
                        role.code.eq(code),
                        role.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Role> findByName(String name) {
        QRole role = QRole.role;

        Role result = queryFactory
                .selectFrom(role)
                .where(
                        role.name.eq(name),
                        role.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Role> findByIdWithPermissions(Long id) {
        QRole role = QRole.role;
        QRolePermission rolePermission = QRolePermission.rolePermission;
        QPermission permission = QPermission.permission;

        Role result = queryFactory
                .selectFrom(role)
                .leftJoin(role.rolePermissions, rolePermission).fetchJoin()
                .leftJoin(rolePermission.permission, permission).fetchJoin()
                .where(
                        role.id.eq(id),
                        role.isDeleted.eq(YNType.N),
                        rolePermission.isDeleted.eq(YNType.N).or(rolePermission.isNull())
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Role> findByCodeWithPermissions(String code) {
        QRole role = QRole.role;
        QRolePermission rolePermission = QRolePermission.rolePermission;
        QPermission permission = QPermission.permission;

        Role result = queryFactory
                .selectFrom(role)
                .leftJoin(role.rolePermissions, rolePermission).fetchJoin()
                .leftJoin(rolePermission.permission, permission).fetchJoin()
                .where(
                        role.code.eq(code),
                        role.isDeleted.eq(YNType.N),
                        rolePermission.isDeleted.eq(YNType.N).or(rolePermission.isNull())
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Role> findAllRoles() {
        QRole role = QRole.role;

        return queryFactory
                .selectFrom(role)
                .where(role.isDeleted.eq(YNType.N))
                .orderBy(role.code.asc())
                .fetch();
    }

    @Override
    public List<Role> findAllRolesWithPermissions() {
        QRole role = QRole.role;
        QRolePermission rolePermission = QRolePermission.rolePermission;
        QPermission permission = QPermission.permission;

        return queryFactory
                .selectFrom(role)
                .distinct()
                .leftJoin(role.rolePermissions, rolePermission).fetchJoin()
                .leftJoin(rolePermission.permission, permission).fetchJoin()
                .where(
                        role.isDeleted.eq(YNType.N),
                        rolePermission.isDeleted.eq(YNType.N).or(rolePermission.isNull())
                )
                .orderBy(role.code.asc())
                .fetch();
    }
}
