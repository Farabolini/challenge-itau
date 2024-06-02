package br.com.farabolini.challenge.infrastructure.rest;

import br.com.farabolini.challenge.domain.exceptions.UnsupportedHttpMethodException;
import br.com.farabolini.challenge.infrastructure.configurations.WiremockConfig;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Slf4j
public abstract class RestCommand {

    private final RestTemplate restTemplate;
    private final WiremockConfig wiremockConfig;

    protected RestCommand(RestTemplate restTemplate, WiremockConfig wiremockConfig) {
        this.restTemplate = restTemplate;
        this.wiremockConfig = wiremockConfig;
    }

    protected final  <S, T> T execute(String url, HttpMethod httpMethod,
                                      @Nullable S body, @Nullable Map<String, ?> queryParams, Class<T> responseType) {
        return switch (httpMethod.name()) {
            case "GET" -> Objects.isNull(queryParams) ? restTemplate.exchange(url, httpMethod, new HttpEntity<>(httpHeaders()), responseType).getBody()
                        : restTemplate.exchange(url, httpMethod, new HttpEntity<>(body, httpHeaders()), responseType, queryParams).getBody();

            case "POST", "PUT" -> Objects.isNull(body) ? restTemplate.exchange(url, httpMethod, new HttpEntity<>(httpHeaders()), responseType).getBody()
                    : restTemplate.exchange(url, httpMethod, new HttpEntity<>(body, httpHeaders()), responseType).getBody();

            default -> throw new UnsupportedHttpMethodException();
        };
    }

    protected final String buildUrl(String endpoint) {
        return "%s/%s".formatted(wiremockConfig.getHost(), endpoint);
    }

    private HttpHeaders httpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return httpHeaders;
    }

}
