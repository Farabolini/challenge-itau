package br.com.farabolini.challenge.adapters.customers;

import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;
import br.com.farabolini.challenge.domain.exceptions.GetCustomerException;
import br.com.farabolini.challenge.infrastructure.configurations.WiremockConfig;
import br.com.farabolini.challenge.infrastructure.rest.RestCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class CustomersCommand extends RestCommand {

    private static final String GET_CUSTOMER = "clientes/%s";

    public CustomersCommand(RestTemplate restTemplate, WiremockConfig wiremockConfig) {
        super(restTemplate, wiremockConfig);
    }

    @Cacheable(cacheNames = "customers", cacheManager = "customersCacheManager")
    public CustomerInfoResponse getCustomerInfo(UUID customerId) {
        try {
            return execute(buildUrl(GET_CUSTOMER.formatted(customerId)), HttpMethod.GET, null, null, CustomerInfoResponse.class);
        } catch (RestClientException e) {
            log.error("Unable to retrieve customer info, due to: ", e);
            throw new GetCustomerException();
        }
    }

}
