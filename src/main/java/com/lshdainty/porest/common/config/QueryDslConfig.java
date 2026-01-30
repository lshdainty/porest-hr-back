package com.lshdainty.porest.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

/**
 * QueryDSL 설정
 *
 * <p>QueryDSL 사용을 위한 JPAQueryFactory와 EntityManager 빈을 생성합니다.
 * 모든 QueryDSL Repository에서 이 빈들을 주입받아 사용합니다.</p>
 */
@Configuration
public class QueryDslConfig {

    /**
     * EntityManager 빈 생성
     * Repository에서 생성자 주입으로 사용할 수 있도록 빈으로 등록
     */
    @Bean
    public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    /**
     * JPAQueryFactory 빈 생성
     */
    @Bean
    @Primary
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
