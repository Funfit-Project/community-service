package funfit.community.config;

import funfit.community.post.dto.BestPostsResponse;
import funfit.community.api.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    /*
    로컬에서 센티널 구성 시 코드
     */
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//
//        RedisSentinelConfiguration sentinelConfiguration = new RedisSentinelConfiguration()
//                .master("mymaster")
//                .sentinel("community_redis_sentinel_1", 26379)
//                .sentinel("community_redis_sentinel_2", 26379)
//                .sentinel("community_redis_sentinel_3", 26379);
//        sentinelConfiguration.setPassword("1234");
//
//        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(sentinelConfiguration);
//        connectionFactory.setTimeout(5000);
//        return connectionFactory;
//    }

    @Bean
    public RedisTemplate<String, BestPostsResponse> bestPostsRedisTemplate() {
        RedisTemplate<String, BestPostsResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(BestPostsResponse.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, User> userDtoRedisTemplate() {
        RedisTemplate<String, User> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(User.class));
        return redisTemplate;
    }
}
