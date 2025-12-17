package com.lshdainty.porest.common.converter;

import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.DefaultSystemType;
import com.lshdainty.porest.common.type.SystemType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 요청 파라미터(String)를 SystemType 인터페이스로 변환하는 Spring MVC Converter
 */
@Component
public class StringToSystemTypeConverter implements Converter<String, SystemType> {

    private static final List<Class<? extends Enum<? extends SystemType>>> IMPLEMENTATIONS = new ArrayList<>();

    static {
        IMPLEMENTATIONS.add(DefaultSystemType.class);
        tryRegister("com.lshdainty.porest.work.type.OriginSystemType");
    }

    @SuppressWarnings("unchecked")
    private static void tryRegister(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            IMPLEMENTATIONS.add((Class<? extends Enum<? extends SystemType>>) clazz);
        } catch (ClassNotFoundException e) {
            // 해당 모듈이 classpath에 없으면 무시
        }
    }

    /**
     * 새로운 SystemType 구현체 등록
     * 테스트 등에서 호출하여 구현체를 등록
     */
    @SuppressWarnings("unchecked")
    public static void register(Class<? extends Enum<?>> implClass) {
        IMPLEMENTATIONS.add((Class<? extends Enum<? extends SystemType>>) implClass);
    }

    @Override
    public SystemType convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        for (Class<? extends Enum<? extends SystemType>> implClass : IMPLEMENTATIONS) {
            for (Enum<? extends SystemType> enumConstant : implClass.getEnumConstants()) {
                if (enumConstant.name().equals(source)) {
                    return (SystemType) enumConstant;
                }
            }
        }

        throw new InvalidValueException(ErrorCode.UNSUPPORTED_TYPE, "Unknown SystemType: " + source);
    }
}
