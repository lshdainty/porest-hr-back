package com.lshdainty.porest.security.dto;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.security.oauth2.factory.OAuth2UserInfoFactory;
import com.lshdainty.porest.security.oauth2.provider.OAuth2UserInfo;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.user.domain.UserProvider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;
    private String providerId;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        // Factory 패턴 사용으로 간소화
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        return OAuthAttributes.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .picture(oAuth2UserInfo.getPicture())
                .attributes(attributes)
                .provider(oAuth2UserInfo.getProvider())
                .providerId(oAuth2UserInfo.getProviderId())
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 새로운 사용자 생성 (소셜 로그인용)
    public User toNewUser() {
        // 소셜 로그인의 경우 고유한 아이디 생성 (이메일 기반)
        String uniqueUserId = "social_" + this.email.replace("@", "_").replace(".", "_");

        return User.createUser(
                uniqueUserId, // 고유한 사용자 ID
                "", // 소셜 로그인은 비밀번호 없음
                this.name,
                this.email,
                "", // 생년월일 없음
                null, // 회사 정보 없음 (추후 설정)
                "9 ~ 6", // 기본 근무시간
                YNType.N, // 음력 기본값
                this.picture, // profileName으로 picture URL 사용
                UUID.randomUUID().toString() // profileUUID 생성
        );
    }

    // UserProvider 생성
    public UserProvider toUserProvider(User user) {
        return UserProvider.createProvider(
                user,
                this.provider,
                this.providerId
        );
    }
}
