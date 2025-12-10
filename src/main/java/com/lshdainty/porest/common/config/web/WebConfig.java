package com.lshdainty.porest.common.config.web;

import com.lshdainty.porest.common.config.properties.AppProperties;
import com.lshdainty.porest.common.config.security.RequestResponseLoggingFilter;
import com.lshdainty.porest.security.resolver.LoginUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final LoginUserArgumentResolver loginUserArgumentResolver;
    private final RequestResponseLoggingFilter requestResponseLoggingFilter;
    private final AppProperties appProperties;
    private final LocaleChangeInterceptor localeChangeInterceptor;

    @Value("${file.resource-handler}")
    private String resourceHandler;

    @Value("${file.resource-locations}")
    private String resourceLocations;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(appProperties.getFrontend().getBaseUrl())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourceHandler)
                .addResourceLocations(resourceLocations);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
    }

    /**
     * RequestResponseLoggingFilter를 Servlet 컨테이너 레벨에서 등록
     * Spring Security 필터 체인보다 먼저 실행되어 모든 요청(OAuth, 회원가입 포함)을 캡처
     */
    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(requestResponseLoggingFilter);
        registrationBean.addUrlPatterns("/*"); // 모든 URL 패턴에 적용
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // 가장 높은 우선순위

        return registrationBean;
    }
}