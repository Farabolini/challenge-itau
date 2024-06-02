package br.com.farabolini.challenge.unit.adapters.customers;

import br.com.farabolini.challenge.adapters.customers.CustomersCommand;
import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;
import br.com.farabolini.challenge.infrastructure.configurations.WiremockConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class CustomersCommandTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WiremockConfig wiremockConfig;

    @InjectMocks
    private CustomersCommand customersCommand;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldGetCustomerInfo() {
        UUID customerId = UUID.randomUUID();
        CustomerInfoResponse customerInfoResponse = new CustomerInfoResponse(customerId, "Name", "Phone", "Type");

        when(wiremockConfig.getHost()).thenReturn("host");
        when(restTemplate.exchange(eq("host/clientes/%s".formatted(customerId)), eq(HttpMethod.GET), any(HttpEntity.class), eq(CustomerInfoResponse.class)))
                .thenReturn(new ResponseEntity<>(customerInfoResponse, HttpStatus.OK));

        customersCommand.getCustomerInfo(customerId);

        verify(restTemplate, times(1))
                .exchange(eq("host/clientes/%s".formatted(customerId)), eq(HttpMethod.GET), any(HttpEntity.class), eq(CustomerInfoResponse.class));
    }

}
