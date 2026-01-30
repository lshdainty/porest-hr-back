package com.porest.hr.common.config;

import com.porest.hr.common.converter.CompanyTypeConverter;
import com.porest.hr.common.converter.SystemTypeConverter;
import org.springframework.context.annotation.Configuration;

/**
 * CompanyType, SystemType 구현체 자동 등록 설정
 * classpath에 skc 모듈이 있으면 해당 구현체들을 Converter에 등록합니다.
 *
 * 중요: static 블록에서 등록하여 Hibernate 초기화 전에 실행되도록 함
 */
@Configuration
public class TypeConverterConfig {

    static {
        // Hibernate Converter 초기화 전에 실행되어야 함
        registerTypeImplementations();
    }

    @SuppressWarnings("unchecked")
    private static void registerTypeImplementations() {
        // OriginCompanyType 등록 시도
        try {
            Class<?> originCompanyType = Class.forName("com.lshdainty.porest.company.type.OriginCompanyType");
            CompanyTypeConverter.register((Class<? extends Enum<?>>) originCompanyType);
            System.out.println("[TypeConverterConfig] Registered OriginCompanyType to CompanyTypeConverter");
        } catch (ClassNotFoundException e) {
            // skc 모듈이 없으면 무시
        }

        // OriginSystemType 등록 시도
        try {
            Class<?> originSystemType = Class.forName("com.porest.hr.work.type.OriginSystemType");
            SystemTypeConverter.register((Class<? extends Enum<?>>) originSystemType);
            System.out.println("[TypeConverterConfig] Registered OriginSystemType to SystemTypeConverter");
        } catch (ClassNotFoundException e) {
            // skc 모듈이 없으면 무시
        }
    }
}
