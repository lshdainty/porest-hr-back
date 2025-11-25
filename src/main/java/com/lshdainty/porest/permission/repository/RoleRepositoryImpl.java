package com.lshdainty.porest.permission.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.permission.domain.QPermission;
import com.lshdainty.porest.permission.domain.QRole;
import com.lshdainty.porest.permission.domain.Role;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role Repository 구현체<br>
 * QueryDSL을 활용한 역할 조회 구현
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(Role role) {
        em.persist(role);
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
    public Optional<Role> findByNameWithPermissions(String name) {
        QRole role = QRole.role;
        QPermission permission = QPermission.permission;

        Role result = queryFactory
                .selectFrom(role)
                .leftJoin(role.permissions, permission).fetchJoin()
                .where(
                        role.name.eq(name),
                        role.isDeleted.eq(YNType.N)
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
                .orderBy(role.name.asc())
                .fetch();
    }

    @Override
    public List<Role> findAllRolesWithPermissions() {
        QRole role = QRole.role;
        QPermission permission = QPermission.permission;

        return queryFactory
                .selectFrom(role)
                .distinct()
                .leftJoin(role.permissions, permission).fetchJoin()
                .where(role.isDeleted.eq(YNType.N))
                .orderBy(role.name.asc())
                .fetch();
    }
}
