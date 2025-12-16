package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lshdainty.porest.permission.domain.QRole.role;
import static com.lshdainty.porest.permission.domain.QRolePermission.rolePermission;
import static com.lshdainty.porest.permission.domain.QUserRole.userRole;
import static com.lshdainty.porest.user.domain.QUser.user;

@Repository
@Primary
@RequiredArgsConstructor
public class UserQueryDslRepository implements UserRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(User user) {
        em.persist(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(query
                .selectFrom(user)
                .where(user.id.eq(userId))
                .fetchOne()
        );
    }

    @Override
    public List<User> findUsers() {
        return query
                .selectFrom(user)
                .where(user.isDeleted.eq(YNType.N)
                        .and(user.company.ne(OriginCompanyType.SYSTEM)))
                .fetch();
    }

    @Override
    public Optional<User> findByIdWithRolesAndPermissions(String userId) {
        // 1단계: User + UserRole + Role 조회
        User result = query
                .selectFrom(user)
                .distinct()
                .leftJoin(user.userRoles, userRole).fetchJoin()
                .leftJoin(userRole.role, role).fetchJoin()
                .where(user.id.eq(userId)
                        .and(user.isDeleted.eq(YNType.N)))
                .fetchOne();

        if (result == null) {
            return Optional.empty();
        }

        // 2단계: Role의 RolePermission + Permission 조회
        if (!result.getRoles().isEmpty()) {
            query
                    .selectFrom(role)
                    .distinct()
                    .leftJoin(role.rolePermissions, rolePermission).fetchJoin()
                    .leftJoin(rolePermission.permission).fetchJoin()
                    .where(role.in(result.getRoles()))
                    .fetch();
        }

        return Optional.of(result);
    }

    @Override
    public Optional<User> findByInvitationToken(String token) {
        User result = query
                .selectFrom(user)
                .where(user.invitationToken.eq(token)
                        .and(user.isDeleted.eq(YNType.N)))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public List<User> findUsersWithRolesAndPermissions() {
        // 1단계: User + UserRole + Role 조회
        List<User> users = query
                .selectFrom(user)
                .distinct()
                .leftJoin(user.userRoles, userRole).fetchJoin()
                .leftJoin(userRole.role, role).fetchJoin()
                .where(user.isDeleted.eq(YNType.N)
                        .and(user.company.ne(OriginCompanyType.SYSTEM)))
                .fetch();

        if (users.isEmpty()) {
            return users;
        }

        // 2단계: 모든 사용자의 Role들에 대한 RolePermission + Permission 조회
        List<Role> allRoles = users.stream()
                .flatMap(u -> u.getRoles().stream())
                .distinct()
                .collect(Collectors.toList());

        if (!allRoles.isEmpty()) {
            query
                    .selectFrom(role)
                    .distinct()
                    .leftJoin(role.rolePermissions, rolePermission).fetchJoin()
                    .leftJoin(rolePermission.permission).fetchJoin()
                    .where(role.in(allRoles))
                    .fetch();
        }

        return users;
    }

    @Override
    public List<User> findDeletedUsersByModifyDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return query
                .selectFrom(user)
                .where(user.isDeleted.eq(YNType.Y)
                        .and(user.company.ne(OriginCompanyType.SYSTEM))
                        .and(user.modifyDate.goe(startDate))
                        .and(user.modifyDate.lt(endDate)))
                .fetch();
    }
}
