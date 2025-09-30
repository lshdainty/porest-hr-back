package com.lshdainty.porest.security.service;

import com.lshdainty.porest.security.dto.OAuthAttributes;
import com.lshdainty.porest.user.controller.dto.UserDto;
import com.lshdainty.porest.user.domain.User;
//import com.lshdainty.porest.user.domain.UserProvider;
import com.lshdainty.porest.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
//    private final UserProviderRepository userProviderRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = processOAuth2User(attributes);
        httpSession.setAttribute("user", UserDto.builder().build());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    private User processOAuth2User(OAuthAttributes attributes) {
        // 1. 기존 소셜 로그인 연결이 있는지 확인
//        Optional<UserProvider> existingProvider = userProviderRepository
//                .findByTypeAndId(attributes.getProvider(), attributes.getProviderId());
//
//        if (existingProvider.isPresent()) {
//            // 기존 소셜 로그인이 있는 경우 - 사용자 정보 업데이트 없이 그냥 pass
//            User existingUser = existingProvider.get().getUser();
//            return existingUser;
//        } else {
//            // 2. 기존 소셜 로그인 연결이 없는 경우 - 회원가입 필요 예외 발생
//            throw new SocialLoginRequiresSignupException(
//                    attributes.getProvider(),
//                    attributes.getProviderId(),
//                    attributes.getEmail(),
//                    attributes.getName(),
//                    attributes.getPicture()
//            );
//        }
        return null;
    }
}

