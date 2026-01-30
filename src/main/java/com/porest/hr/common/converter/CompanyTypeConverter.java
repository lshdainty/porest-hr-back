package com.porest.hr.common.converter;

import com.lshdainty.porest.common.type.CompanyType;
import com.lshdainty.porest.common.type.DefaultCompanyType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * CompanyType 인터페이스를 DB에 저장/조회하기 위한 JPA Converter
 * 등록된 모든 CompanyType 구현체에서 값을 찾아 변환합니다.
 */
@Converter(autoApply = true)
public class CompanyTypeConverter implements AttributeConverter<CompanyType, String> {

    private static final List<Class<? extends Enum<? extends CompanyType>>> IMPLEMENTATIONS = new ArrayList<>();

    static {
        // 기본 구현체는 항상 등록
        IMPLEMENTATIONS.add(DefaultCompanyType.class);

        // skc 모듈의 OriginCompanyType 자동 등록 시도
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

    /**
     * 새로운 CompanyType 구현체 등록
     * skc 모듈 등에서 호출하여 구현체를 등록
     */
    @SuppressWarnings("unchecked")
    public static void register(Class<? extends Enum<?>> implClass) {
        IMPLEMENTATIONS.add((Class<? extends Enum<? extends CompanyType>>) implClass);
    }

    @Override
    public String convertToDatabaseColumn(CompanyType attribute) {
        if (attribute == null) {
            return null;
        }
        return ((Enum<?>) attribute).name();
    }

    @Override
    public CompanyType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        // 등록된 모든 구현체에서 해당 name을 찾기
        for (Class<? extends Enum<? extends CompanyType>> implClass : IMPLEMENTATIONS) {
            for (Enum<? extends CompanyType> enumConstant : implClass.getEnumConstants()) {
                if (enumConstant.name().equals(dbData)) {
                    return (CompanyType) enumConstant;
                }
            }
        }

        throw new IllegalArgumentException("Unknown CompanyType: " + dbData);
    }
}
