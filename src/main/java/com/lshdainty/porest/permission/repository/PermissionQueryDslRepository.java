package com.lshdainty.porest.permission.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.QPermission;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Permission Repository 구현체<br>
 * QueryDSL을 활용한 권한 조회 구현
 */
@Repository
@Primary
@RequiredArgsConstructor
public class PermissionQueryDslRepository implements PermissionRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    @Override
    public void save(Permission permission) {
        em.persist(permission);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        QPermission permission = QPermission.permission;

        Permission result = queryFactory
                .selectFrom(permission)
                .where(
                        permission.code.eq(code),
                        permission.isDeleted.eq(YNType.N)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Permission> findAllPermissions() {
        QPermission permission = QPermission.permission;

        return queryFactory
                .selectFrom(permission)
                .where(permission.isDeleted.eq(YNType.N))
                .orderBy(permission.resource.asc(), permission.action.asc())
                .fetch();
    }

    @Override
    public List<Permission> findByResource(String resource) {
        QPermission permission = QPermission.permission;
        ResourceType resourceType = ResourceType.valueOf(resource);

        return queryFactory
                .selectFrom(permission)
                .where(
                        permission.resource.eq(resourceType),
                        permission.isDeleted.eq(YNType.N)
                )
                .orderBy(permission.action.asc())
                .fetch();
    }
}
