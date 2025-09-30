package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final EntityManager em;

    @Override
    public void save(User user) {
        em.persist(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(em.find(User.class, userId));
    }

    @Override
    public List<User> findUsers() {
        return em.createQuery("select u from User u where u.delYN = :delYN", User.class)
                .setParameter("delYN", YNType.N)
                .getResultList();
    }

    @Override
    public List<User> findUsersWithVacations() {
        return em.createQuery("select u from User u left join fetch u.vacations v where u.delYN = :uDelYN", User.class)
                .setParameter("uDelYN", YNType.N)
                .getResultList();
    }

    @Override
    public Optional<User> findByInvitationToken(String token) {
        List<User> result = em.createQuery("select u from User u where u.invitationToken = :token and u.delYN = :delYN", User.class)
                .setParameter("token", token)
                .setParameter("delYN", YNType.N)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}


