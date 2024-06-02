package br.com.farabolini.challenge.infrastructure.configurations;

import br.com.farabolini.challenge.infrastructure.ApplicationProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RetryTemplate bacenRetryTemplate(ApplicationProperties applicationProperties) {
        return RetryTemplate.builder()
                .maxAttempts(applicationProperties.getBacenRetryAttempts())
                .fixedBackoff(applicationProperties.getBacenRateLimitThreshold() * 1000)
                .retryOn(Exception.class).build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.findAndRegisterModules();

        return objectMapper;
    }

}
