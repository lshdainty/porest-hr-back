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
}
