package br.com.farabolini.challenge.infrastructure.configurations;

import br.com.farabolini.challenge.infrastructure.ApplicationProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisConfig {

    private static final long SIXTY_MINUTES = 60 * 60 * (long) 1000;
    private static final long THIRTY_MINUTES = 30 * 60 * (long) 1000;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(Config redissonConfig) {
        return Redisson.create(redissonConfig);
    }

    @Bean
    public Config redissonConfig(ApplicationProperties applicationProperties) {
        Config config = new Config();
        config.setNettyThreads(applicationProperties.getRedissonNettyThreads());
        config.useSingleServer()
                .setAddress(String.format("redis://%s", applicationProperties.getRedisAddress()[0]))
                .setUsername(applicationProperties.getRedisUsername())
                .setPassword(applicationProperties.getRedisPassword());

        return config;
    }

    @Bean
    public CacheManager customersCacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();
        config.put("customers", new CacheConfig(SIXTY_MINUTES, THIRTY_MINUTES));

        return new RedissonSpringCacheManager(redissonClient, config);
    }

}
