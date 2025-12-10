package com.lshdainty.porest.security.resolver;

import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.UnauthorizedException;
import com.lshdainty.porest.security.annotation.LoginUser;
import com.lshdainty.porest.security.principal.UserPrincipal;
import com.lshdainty.porest.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 파라미터에 @LoginUser 어노테이션이 붙어 있고, 파라미터 클래스 타입이 User인 경우 true를 반환한다.
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isLoginUserAnnotation = parameter.getParameterAnnotation(LoginUser.class) != null;
        boolean isUserClass = User.class.equals(parameter.getParameterType());

        return isLoginUserAnnotation && isUserClass;
    }

    /**
     * 파라미터에 전달할 객체를 생성한다.
     * Spring Security의 SecurityContextHolder에서 로그인한 사용자 정보를 가져온다.
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        return userPrincipal.getUser();
    }
}
