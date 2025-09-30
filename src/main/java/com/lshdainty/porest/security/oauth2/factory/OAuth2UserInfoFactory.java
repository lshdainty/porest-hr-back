package com.lshdainty.porest.security.oauth2.factory;

import com.lshdainty.porest.security.oauth2.provider.GoogleOAuth2UserInfo;
import com.lshdainty.porest.security.oauth2.provider.KakaoOAuth2UserInfo;
import com.lshdainty.porest.security.oauth2.provider.NaverOAuth2UserInfo;
import com.lshdainty.porest.security.oauth2.provider.OAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleOAuth2UserInfo(attributes);
            case "naver":
                return new NaverOAuth2UserInfo(attributes);
            case "kakao":
                return new KakaoOAuth2UserInfo(attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 로그인 서비스입니다: " + registrationId);
        }
    }
}
