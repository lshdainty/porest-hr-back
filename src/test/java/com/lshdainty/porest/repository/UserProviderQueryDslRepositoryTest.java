package com.lshdainty.porest.repository;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.domain.UserProvider;
import com.lshdainty.porest.user.repository.UserProviderQueryDslRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserProviderQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 유저프로바이더 레포지토리 테스트")
class UserProviderQueryDslRepositoryTest {
    @Autowired
    private UserProviderQueryDslRepository userProviderRepository;

    @Autowired
    private TestEntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.createUser("user1");
        em.persist(user);
    }

    @Test
    @DisplayName("유저 프로바이더 저장")
    void save() {
        // given
        UserProvider provider = UserProvider.createProvider(user, "GOOGLE", "google_12345");

        // when
        userProviderRepository.save(provider);
        em.flush();
        em.clear();

        // then
        Optional<UserProvider> findProvider = userProviderRepository
                .findByProviderTypeAndProviderId("GOOGLE", "google_12345");
        assertThat(findProvider.isPresent()).isTrue();
        assertThat(findProvider.get().getType()).isEqualTo("GOOGLE");
        assertThat(findProvider.get().getId()).isEqualTo("google_12345");
    }

    @Test
    @DisplayName("프로바이더 타입과 ID로 조회")
    void findByProviderTypeAndProviderId() {
        // given
        UserProvider provider = UserProvider.createProvider(user, "KAKAO", "kakao_67890");
        userProviderRepository.save(provider);
        em.flush();
        em.clear();

        // when
        Optional<UserProvider> findProvider = userProviderRepository
                .findByProviderTypeAndProviderId("KAKAO", "kakao_67890");

        // then
        assertThat(findProvider.isPresent()).isTrue();
        assertThat(findProvider.get().getUser()).isNotNull();
        assertThat(findProvider.get().getUser().getId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("프로바이더 타입과 ID로 조회 시 없으면 빈 Optional 반환")
    void findByProviderTypeAndProviderIdEmpty() {
        // when
        Optional<UserProvider> findProvider = userProviderRepository
                .findByProviderTypeAndProviderId("GOOGLE", "nonexistent");

        // then
        assertThat(findProvider.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("프로바이더 타입만 일치해도 조회 실패")
    void findByProviderTypeAndProviderIdTypeMismatch() {
        // given
        UserProvider provider = UserProvider.createProvider(user, "GOOGLE", "google_12345");
        userProviderRepository.save(provider);
        em.flush();
        em.clear();

        // when
        Optional<UserProvider> findProvider = userProviderRepository
                .findByProviderTypeAndProviderId("GOOGLE", "different_id");

        // then
        assertThat(findProvider.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("프로바이더 ID만 일치해도 조회 실패")
    void findByProviderTypeAndProviderIdIdMismatch() {
        // given
        UserProvider provider = UserProvider.createProvider(user, "GOOGLE", "google_12345");
        userProviderRepository.save(provider);
        em.flush();
        em.clear();

        // when
        Optional<UserProvider> findProvider = userProviderRepository
                .findByProviderTypeAndProviderId("KAKAO", "google_12345");

        // then
        assertThat(findProvider.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("여러 프로바이더를 가진 유저")
    void multipleProvidersForSameUser() {
        // given
        UserProvider googleProvider = UserProvider.createProvider(user, "GOOGLE", "google_12345");
        UserProvider kakaoProvider = UserProvider.createProvider(user, "KAKAO", "kakao_67890");
        userProviderRepository.save(googleProvider);
        userProviderRepository.save(kakaoProvider);
        em.flush();
        em.clear();

        // when
        Optional<UserProvider> findGoogle = userProviderRepository
                .findByProviderTypeAndProviderId("GOOGLE", "google_12345");
        Optional<UserProvider> findKakao = userProviderRepository
                .findByProviderTypeAndProviderId("KAKAO", "kakao_67890");

        // then
        assertThat(findGoogle.isPresent()).isTrue();
        assertThat(findKakao.isPresent()).isTrue();
        assertThat(findGoogle.get().getUser().getId()).isEqualTo("user1");
        assertThat(findKakao.get().getUser().getId()).isEqualTo("user1");
    }

    @Test
    @DisplayName("다른 유저가 같은 타입의 프로바이더 사용")
    void differentUsersWithSameProviderType() {
        // given
        User user2 = User.createUser("user2");
        em.persist(user2);

        UserProvider provider1 = UserProvider.createProvider(user, "GOOGLE", "google_user1");
        UserProvider provider2 = UserProvider.createProvider(user2, "GOOGLE", "google_user2");
        userProviderRepository.save(provider1);
        userProviderRepository.save(provider2);
        em.flush();
        em.clear();

        // when
        Optional<UserProvider> findProvider1 = userProviderRepository
                .findByProviderTypeAndProviderId("GOOGLE", "google_user1");
        Optional<UserProvider> findProvider2 = userProviderRepository
                .findByProviderTypeAndProviderId("GOOGLE", "google_user2");

        // then
        assertThat(findProvider1.isPresent()).isTrue();
        assertThat(findProvider2.isPresent()).isTrue();
        assertThat(findProvider1.get().getUser().getId()).isEqualTo("user1");
        assertThat(findProvider2.get().getUser().getId()).isEqualTo("user2");
    }

    @Test
    @DisplayName("유저 fetch join 확인")
    void fetchJoinUser() {
        // given
        UserProvider provider = UserProvider.createProvider(user, "NAVER", "naver_11111");
        userProviderRepository.save(provider);
        em.flush();
        em.clear();

        // when
        Optional<UserProvider> findProvider = userProviderRepository
                .findByProviderTypeAndProviderId("NAVER", "naver_11111");

        // then
        assertThat(findProvider.isPresent()).isTrue();
        // fetch join으로 인해 추가 쿼리 없이 user 접근 가능
        assertThat(findProvider.get().getUser()).isNotNull();
        assertThat(findProvider.get().getUser().getId()).isEqualTo("user1");
    }
}
