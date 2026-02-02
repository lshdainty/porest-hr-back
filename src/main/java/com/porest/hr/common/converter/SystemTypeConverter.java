package com.porest.hr.common.converter;

import com.porest.hr.common.type.SystemType;
import com.porest.hr.common.type.DefaultSystemType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * SystemType 인터페이스를 DB에 저장/조회하기 위한 JPA Converter
 * 등록된 모든 SystemType 구현체에서 값을 찾아 변환합니다.
 */
@Converter(autoApply = true)
public class SystemTypeConverter implements AttributeConverter<SystemType, String> {

    private static final List<Class<? extends Enum<? extends SystemType>>> IMPLEMENTATIONS = new ArrayList<>();

    static {
        // 기본 구현체는 항상 등록
        IMPLEMENTATIONS.add(DefaultSystemType.class);

        // skc 모듈의 OriginSystemType 자동 등록 시도
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
     * skc 모듈 등에서 호출하여 구현체를 등록
     */
    @SuppressWarnings("unchecked")
    public static void register(Class<? extends Enum<?>> implClass) {
        IMPLEMENTATIONS.add((Class<? extends Enum<? extends SystemType>>) implClass);
    }

    @Override
    public String convertToDatabaseColumn(SystemType attribute) {
        if (attribute == null) {
            return null;
        }
        return ((Enum<?>) attribute).name();
    }

    @Override
    public SystemType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        // 등록된 모든 구현체에서 해당 name을 찾기
        for (Class<? extends Enum<? extends SystemType>> implClass : IMPLEMENTATIONS) {
            for (Enum<? extends SystemType> enumConstant : implClass.getEnumConstants()) {
                if (enumConstant.name().equals(dbData)) {
                    return (SystemType) enumConstant;
                }
            }
        }

        throw new IllegalArgumentException("Unknown SystemType: " + dbData);
    }
}
