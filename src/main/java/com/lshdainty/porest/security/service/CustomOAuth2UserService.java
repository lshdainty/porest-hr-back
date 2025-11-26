package com.lshdainty.porest.security.service;

import com.lshdainty.porest.security.dto.OAuthAttributes;
import com.lshdainty.porest.security.principal.CustomOAuth2User;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.domain.UserProvider;
import com.lshdainty.porest.user.repository.UserRepositoryImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.lshdainty.porest.permission.domain.Role;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepositoryImpl userRepository;
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
        // 1. 세션에 저장된 토큰정보 가져오기
        String invitationToken = (String) httpSession.getAttribute("invitationToken");
        String oauthStep = (String) httpSession.getAttribute("oauthStep");
        String invitedUserId = (String) httpSession.getAttribute("invitedUserId");

        log.info("Invitation Token: " + invitationToken);
        log.info("OAuth Step: " + oauthStep);
        log.info("Invited UserId: " + invitedUserId);

        // 2. 3가지의 값이 다 있다면 회원가입 아니면 로그인으로 간주
        if (invitationToken != null && "signup".equals(oauthStep) && invitedUserId != null) {
            // 회원 가입 부분
            // 2-1. 초대 토큰으로 사용자 찾기
            User user = userRepository.findByInvitationToken(invitationToken)
                    .orElseThrow(() -> new OAuth2AuthenticationException("유효하지 않은 초대 토큰입니다."));

            // 2-2. 토큰 재검증
            if (!user.isInvitationValid()) {
                throw new OAuth2AuthenticationException("초대 토큰이 만료되었거나 유효하지 않습니다.");
            }

            if (!user.getId().equals(invitedUserId)) {
                throw new OAuth2AuthenticationException("초대된 사용자 정보가 일치하지 않습니다.");
            }

            // 2-3. OAuth2 제공자 연결
            UserProvider userProvider = UserProvider.createProvider(
                    user,
                    attributes.getProvider(),
                    attributes.getProviderId()
            );
            userProviderRepository.save(userProvider);

            log.info("OAuth2 신규 연결 완료: userId={}, provider={}", user.getId(), attributes.getProvider());
            return user;
        } else {
            // 로그인 부분
            // 기존 소셜 로그인 연결 확인
            UserProvider userProvider = userProviderRepository
                    .findByProviderTypeAndProviderId(attributes.getProvider(), attributes.getProviderId())
                    .orElseThrow(() -> new OAuth2AuthenticationException("등록되지 않은 소셜 계정입니다. 먼저 회원가입을 진행해주세요."));

            // 사용자 id를 기반으로 한 유저 권한 정보 및 역할 조회 (역할 및 권한 정보 포함)
            String userId = userProvider.getUser().getId();
            User user = userRepository.findByIdWithRolesAndPermissions(userId)
                    .orElseThrow(() -> new OAuth2AuthenticationException("사용자 정보를 찾을 수 없습니다."));

            log.info("OAuth2 로그인 성공: userId={}, provider={}", user.getId(), attributes.getProvider());
            return user;
        }
    }
}

