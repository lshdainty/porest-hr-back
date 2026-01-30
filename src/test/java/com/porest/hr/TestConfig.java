package com.porest.hr;

import com.porest.hr.common.converter.StringToSystemTypeConverter;
import com.porest.hr.common.converter.SystemTypeConverter;
import com.porest.hr.work.type.TestSystemType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    static {
        // 테스트용 SystemType 구현체 등록
        SystemTypeConverter.register(TestSystemType.class);
        StringToSystemTypeConverter.register(TestSystemType.class);
    }
    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
