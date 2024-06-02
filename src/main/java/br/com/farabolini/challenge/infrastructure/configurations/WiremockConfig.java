package br.com.farabolini.challenge.infrastructure.configurations;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Getter
public class WiremockConfig {

    @Value("${wiremock.host}")
    private String host;

}
