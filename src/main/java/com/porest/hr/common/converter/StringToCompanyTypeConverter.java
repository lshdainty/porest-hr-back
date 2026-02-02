package com.porest.hr.common.converter;

import com.porest.core.exception.ErrorCode;
import com.porest.core.exception.InvalidValueException;
import com.porest.hr.common.type.CompanyType;
import com.porest.hr.common.type.DefaultCompanyType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 요청 파라미터(String)를 CompanyType 인터페이스로 변환하는 Spring MVC Converter
 */
@Component
public class StringToCompanyTypeConverter implements Converter<String, CompanyType> {

    private static final List<Class<? extends Enum<? extends CompanyType>>> IMPLEMENTATIONS = new ArrayList<>();

    static {
        IMPLEMENTATIONS.add(DefaultCompanyType.class);
        tryRegister("com.lshdainty.porest.company.type.OriginCompanyType");
    }

    @SuppressWarnings("unchecked")
    private static void tryRegister(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            IMPLEMENTATIONS.add((Class<? extends Enum<? extends CompanyType>>) clazz);
        } catch (ClassNotFoundException e) {
            // 해당 모듈이 classpath에 없으면 무시
        }
    }

    @Override
    public CompanyType convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        for (Class<? extends Enum<? extends CompanyType>> implClass : IMPLEMENTATIONS) {
            for (Enum<? extends CompanyType> enumConstant : implClass.getEnumConstants()) {
                if (enumConstant.name().equals(source)) {
                    return (CompanyType) enumConstant;
                }
            }
        }

        throw new InvalidValueException(ErrorCode.UNSUPPORTED_TYPE, "Unknown CompanyType: " + source);
    }
}
