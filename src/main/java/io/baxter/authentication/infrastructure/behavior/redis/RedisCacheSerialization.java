package io.baxter.authentication.infrastructure.behavior.redis;

import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisCacheSerialization {
    @Bean
    public ReactiveRedisTemplate<String, RefreshToken> refreshTokenRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        var keySerializer = new StringRedisSerializer();
        var valueSerializer = new Jackson2JsonRedisSerializer<>(RefreshToken.class);

        var context = RedisSerializationContext.<String, RefreshToken>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
