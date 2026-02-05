package com.porest.hr.client.sso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

/**
 * SSO API 클라이언트 설정<br>
 * SSO 서비스와의 HTTP 통신을 위한 RestTemplate 설정
 */
@Configuration
public class SsoClientConfig {

    @Value("${sso.api-url}")
    private String ssoApiUrl;

    @Value("${sso.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${sso.timeout.read:10000}")
    private int readTimeout;

    @Bean(name = "ssoRestTemplate")
    public RestTemplate ssoRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(ssoApiUrl));

        return restTemplate;
    }
}
