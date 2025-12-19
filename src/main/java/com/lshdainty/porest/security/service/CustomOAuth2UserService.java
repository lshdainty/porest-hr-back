package com.lshdainty.porest.security.service;

import com.lshdainty.porest.security.dto.OAuthAttributes;
import com.lshdainty.porest.security.principal.CustomOAuth2User;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.domain.UserProvider;
import com.lshdainty.porest.user.repository.UserRepository;
import com.lshdainty.porest.user.repository.UserProviderRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final UserProviderRepository userProviderRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        log.info("Username attribute name = " + userNameAttributeName);
        log.info("Registration id = " + registrationId);
        log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());
        oAuth2User.getAttributes().forEach((key, value) ->
            log.info("  Attribute - key: {}, value: {}, type: {}", key, value, value != null ? value.getClass().getSimpleName() : "null")
        );

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = processOAuth2User(attributes);

        log.info("OAuth2 User loaded: {}, Roles: {}, Authorities: {}",
                user.getId(),
                user.getRoles().stream().map(role -> role.getCode()).collect(Collectors.joining(", ")),
                String.join(", ", user.getAllAuthorities()));

        return new CustomOAuth2User(
                user,
                attributes.getAttributes(),
                user.getAllAuthorities().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()),
                attributes.getNameAttributeKey());
    }

    private User processOAuth2User(OAuthAttributes attributes) {
        // 1. 세션에 저장된 정보 가져오기
        String oauthStep = (String) httpSession.getAttribute("oauthStep");
        String loginUserId = (String) httpSession.getAttribute("loginUserId");

        log.info("OAuth Step: {}", oauthStep);
        log.info("Login UserId: {}", loginUserId);

        // 2. OAuth 연동 또는 로그인으로 분기
        if ("link".equals(oauthStep) && loginUserId != null) {
            // OAuth 연동 (로그인된 사용자가 소셜 계정 연동)
            return processOAuthLink(attributes, loginUserId);
        } else {
            // 로그인 (기존 소셜 계정으로 로그인)
            return processOAuthLogin(attributes);
        }
    }

    /**
     * OAuth 계정 연동 처리
     * 로그인된 사용자에게 소셜 계정을 연동합니다.
     */
    private User processOAuthLink(OAuthAttributes attributes, String loginUserId) {
        // 1. 해당 OAuth 계정이 이미 연동되어 있는지 확인
        Optional<UserProvider> existingProvider = userProviderRepository
                .findByProviderTypeAndProviderId(attributes.getProvider(), attributes.getProviderId());

        if (existingProvider.isPresent()) {
            String linkedUserId = existingProvider.get().getUser().getId();
            if (linkedUserId.equals(loginUserId)) {
                // 이미 본인 계정에 연동됨
                log.warn("OAuth 연동 실패 - 이미 연동된 계정: userId={}, provider={}", loginUserId, attributes.getProvider());
                throw new OAuth2AuthenticationException("already_linked_self");
            } else {
                // 다른 사용자에게 연동됨
                log.warn("OAuth 연동 실패 - 다른 사용자와 연동된 계정: provider={}, linkedUserId={}", attributes.getProvider(), linkedUserId);
                throw new OAuth2AuthenticationException("already_linked_other");
            }
        }

        // 2. 로그인된 사용자 조회
        User user = userRepository.findByIdWithRolesAndPermissions(loginUserId)
                .orElseThrow(() -> {
                    log.warn("OAuth 연동 실패 - 사용자 없음: userId={}", loginUserId);
                    return new OAuth2AuthenticationException("user_not_found");
                });

        // 3. 새 OAuth 제공자 연결
        UserProvider userProvider = UserProvider.createProvider(
                user,
                attributes.getProvider(),
                attributes.getProviderId()
        );
        userProviderRepository.save(userProvider);

        log.info("OAuth 연동 완료: userId={}, provider={}", loginUserId, attributes.getProvider());

        // 4. 세션 정리
        httpSession.removeAttribute("oauthStep");
        httpSession.removeAttribute("loginUserId");

        return user;
    }

    /**
     * OAuth 로그인 처리
     * 기존에 연동된 소셜 계정으로 로그인합니다.
     */
    private User processOAuthLogin(OAuthAttributes attributes) {
        // 기존 소셜 로그인 연결 확인
        UserProvider userProvider = userProviderRepository
                .findByProviderTypeAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElseThrow(() -> {
                    log.warn("OAuth2 로그인 실패 - 등록되지 않은 소셜 계정: provider={}, providerId={}", attributes.getProvider(), attributes.getProviderId());
                    return new OAuth2AuthenticationException("등록되지 않은 소셜 계정입니다. 먼저 회원가입을 진행해주세요.");
                });

        // 사용자 id를 기반으로 한 유저 권한 정보 및 역할 조회 (역할 및 권한 정보 포함)
        String userId = userProvider.getUser().getId();
        User user = userRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> {
                    log.warn("OAuth2 로그인 실패 - 사용자 정보 없음: userId={}", userId);
                    return new OAuth2AuthenticationException("사용자 정보를 찾을 수 없습니다.");
                });

        log.info("OAuth2 로그인 성공: userId={}, provider={}", user.getId(), attributes.getProvider());
        return user;
    }
}

