package funfit.community.config;

import funfit.community.post.dto.ReadBestPostsResponse;
import funfit.community.rabbitMq.dto.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration()
                .master("mymaster")
                .sentinel("redis-sentinel-1", 26379)
                .sentinel("redis-sentinel-2", 26380)
                .sentinel("redis-sentinel-3", 26381);
        sentinelConfiguration.setPassword("1234");

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(sentinelConfiguration);

        return connectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> StringRedisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, ReadBestPostsResponse> ReadPostListResponseRedisTemplate() {
        RedisTemplate<String, ReadBestPostsResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ReadBestPostsResponse.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, User> UserDtoRedisTemplate() {
        RedisTemplate<String, User> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(User.class));
        return redisTemplate;
    }
}
