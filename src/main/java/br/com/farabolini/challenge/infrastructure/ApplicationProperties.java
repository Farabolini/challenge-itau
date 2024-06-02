package br.com.farabolini.challenge.infrastructure;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApplicationProperties {

    @Value("${redis.address:localhost:6379}")
    private String[] redisAddress;

    @Value("${redis.username:#{null}}")
    private String redisUsername;

    @Value("${redis.password:#{null}}")
    private String redisPassword;

    @Value("${redisson.netty.threads}")
    private int redissonNettyThreads;

    @Value("${bacen.rate.limit.threshold}")
    private long bacenRateLimitThreshold;

    @Value("${bacen.retry.attempts}")
    private int bacenRetryAttempts;

}
