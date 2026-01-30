package com.porest.hr.permission.repository;

import com.lshdainty.porest.common.type.YNType;
import com.porest.hr.permission.domain.Permission;
import com.porest.hr.permission.type.ActionType;
import com.porest.hr.permission.type.ResourceType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("permissionJpaRepository")
@RequiredArgsConstructor
public class PermissionJpaRepository implements PermissionRepository {
    private final EntityManager em;

    @Override
    public void save(Permission permission) {
        em.persist(permission);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        List<Permission> result = em.createQuery(
                "select p from Permission p where p.code = :code and p.isDeleted = :isDeleted", Permission.class)
                .setParameter("code", code)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<Permission> findAllPermissions() {
        return em.createQuery(
                "select p from Permission p where p.isDeleted = :isDeleted order by p.resource asc, p.action asc", Permission.class)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }

    @Override
    public List<Permission> findByResource(String resource) {
        ResourceType resourceType = ResourceType.valueOf(resource);
        return em.createQuery(
                "select p from Permission p where p.resource = :resource and p.isDeleted = :isDeleted order by p.action asc", Permission.class)
                .setParameter("resource", resourceType)
                .setParameter("isDeleted", YNType.N)
                .getResultList();
    }
}
