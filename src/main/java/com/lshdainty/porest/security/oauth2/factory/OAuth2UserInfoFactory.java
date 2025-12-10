package com.lshdainty.porest.security.oauth2.factory;

import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
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
                throw new InvalidValueException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
    }
}
