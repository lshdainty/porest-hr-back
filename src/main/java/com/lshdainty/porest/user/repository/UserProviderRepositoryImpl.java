package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.user.domain.UserProvider;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserProviderRepositoryImpl implements UserProviderRepository {
    private final EntityManager em;

    @Override
    public void save(UserProvider userProvider) {
        em.persist(userProvider);
    }

    @Override
    public Optional<UserProvider> findByProviderTypeAndProviderId(String type, String id) {
        List<UserProvider> result = em.createQuery(
                "select up from UserProvider up " +
                "join fetch up.user u " +
                "where up.type = :type and up.id = :id", UserProvider.class)
                .setParameter("type", type)
                .setParameter("id", id)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}