package com.lshdainty.porest.user.repository;

import com.lshdainty.porest.user.domain.UserProvider;

import java.util.Optional;

public interface UserProviderRepository {
    void save(UserProvider userProvider);
    Optional<UserProvider> findByProviderTypeAndProviderId(String type, String id);
}