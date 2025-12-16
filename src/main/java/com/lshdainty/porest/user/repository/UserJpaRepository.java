package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository("userJpaRepository")
@RequiredArgsConstructor
public class UserJpaRepository implements UserRepository {
    private final EntityManager em;

    @Override
    public void save(User user) {
        em.persist(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        List<User> result = em.createQuery(
                        "select u from User u where u.id = :userId", User.class)
                .setParameter("userId", userId)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<User> findUsers() {
        return em.createQuery("select u from User u where u.isDeleted = :isDeleted and u.company != :systemCompany", User.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("systemCompany", OriginCompanyType.SYSTEM)
                .getResultList();
    }

    @Override
    public Optional<User> findByIdWithRolesAndPermissions(String userId) {
        // 1단계: User + UserRole + Role 조회
        List<User> result = em.createQuery(
                "select distinct u from User u " +
                "left join fetch u.userRoles ur " +
                "left join fetch ur.role r " +
                "where u.id = :userId and u.isDeleted = :isDeleted", User.class)
                .setParameter("userId", userId)
                .setParameter("isDeleted", YNType.N)
                .getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        }

        User user = result.get(0);

        // 2단계: Role의 RolePermission + Permission 조회
        // Role 목록이 있는 경우에만 실행
        if (!user.getRoles().isEmpty()) {
            em.createQuery(
                    "select distinct r from Role r " +
                    "left join fetch r.rolePermissions rp " +
                    "left join fetch rp.permission p " +
                    "where r in :roles", Role.class)
                    .setParameter("roles", user.getRoles())
                    .getResultList();
        }

        return Optional.of(user);
    }

    @Override
    public Optional<User> findByInvitationToken(String token) {
        List<User> result = em.createQuery("select u from User u where u.invitationToken = :token and u.isDeleted = :isDeleted", User.class)
                .setParameter("token", token)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<User> findUsersWithRolesAndPermissions() {
        // 1단계: User + UserRole + Role 조회
        List<User> users = em.createQuery(
                "select distinct u from User u " +
                "left join fetch u.userRoles ur " +
                "left join fetch ur.role r " +
                "where u.isDeleted = :isDeleted and u.company != :systemCompany", User.class)
                .setParameter("isDeleted", YNType.N)
                .setParameter("systemCompany", OriginCompanyType.SYSTEM)
                .getResultList();

        if (users.isEmpty()) {
            return users;
        }

        // 2단계: 모든 사용자의 Role들에 대한 RolePermission + Permission 조회
        // 각 사용자의 역할 목록을 수집
        List<Role> allRoles = users.stream()
                .flatMap(u -> u.getRoles().stream())
                .distinct()
                .collect(Collectors.toList());

        if (!allRoles.isEmpty()) {
            em.createQuery(
                "select distinct r from Role r " +
                "left join fetch r.rolePermissions rp " +
                "left join fetch rp.permission p " +
                "where r in :roles", Role.class)
                .setParameter("roles", allRoles)
                .getResultList();
        }

        return users;
    }

    @Override
    public List<User> findDeletedUsersByModifyDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return em.createQuery(
                "select u from User u " +
                "where u.isDeleted = :isDeleted " +
                "and u.company != :systemCompany " +
                "and u.modifyDate >= :startDate " +
                "and u.modifyDate < :endDate", User.class)
                .setParameter("isDeleted", YNType.Y)
                .setParameter("systemCompany", OriginCompanyType.SYSTEM)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
}


