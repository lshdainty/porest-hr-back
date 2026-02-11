package com.porest.hr.common.config;

import com.porest.hr.common.event.SsoUserEventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Redis 설정
 * SSO 서비스로부터 사용자 이벤트 수신용
 */
@Configuration
public class RedisConfig {

    private static final String USER_EVENT_CHANNEL = "porest:sso:user-events";

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public ChannelTopic userEventTopic() {
        return new ChannelTopic(USER_EVENT_CHANNEL);
    }

    @Bean
    public MessageListenerAdapter userEventListenerAdapter(SsoUserEventSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleUserEvent");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter userEventListenerAdapter,
            ChannelTopic userEventTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(userEventListenerAdapter, userEventTopic);
        return container;
    }
}
