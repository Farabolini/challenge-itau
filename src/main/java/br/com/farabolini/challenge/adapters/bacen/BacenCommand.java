package br.com.farabolini.challenge.adapters.bacen;

import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.infrastructure.configurations.WiremockConfig;
import br.com.farabolini.challenge.infrastructure.rest.RestCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class BacenCommand extends RestCommand {

    private static final String NOTIFY_ENDPOINT = "notificacoes";

    protected BacenCommand(RestTemplate restTemplate, WiremockConfig wiremockConfig) {
        super(restTemplate, wiremockConfig);
    }

    public void notify(BankTransfer bankTransfer) {
        try {
            execute(buildUrl(NOTIFY_ENDPOINT), HttpMethod.POST, bankTransfer, null, Void.class);
        } catch (RestClientException e) {
            log.error("Notify Bacen request failed due to: ", e);
            throw e;
        }

    }
}
