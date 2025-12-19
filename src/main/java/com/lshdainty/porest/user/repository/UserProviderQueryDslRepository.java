package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.user.domain.UserProvider;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.lshdainty.porest.user.domain.QUserProvider.userProvider;

@Repository
@Primary
@RequiredArgsConstructor
public class UserProviderQueryDslRepository implements UserProviderRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(UserProvider userProvider) {
        em.persist(userProvider);
    }

    @Override
    public Optional<UserProvider> findByProviderTypeAndProviderId(String type, String id) {
        UserProvider result = query
                .selectFrom(userProvider)
                .join(userProvider.user).fetchJoin()
                .where(userProvider.type.eq(type)
                        .and(userProvider.id.eq(id)))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public List<UserProvider> findByUserId(String userId) {
        return query
                .selectFrom(userProvider)
                .where(userProvider.user.id.eq(userId))
                .fetch();
    }
}
