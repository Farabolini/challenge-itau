package br.com.farabolini.challenge.unit.adapters.bacen;

import br.com.farabolini.challenge.adapters.accounts.AccountsCommand;
import br.com.farabolini.challenge.adapters.bacen.BacenCommand;
import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.TransferInfo;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class BacenCommandTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WiremockConfig wiremockConfig;

    @InjectMocks
    private BacenCommand bacenCommand;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldNotifyBacen() {
        BankTransfer bankTransfer = new BankTransfer(UUID.randomUUID(), 10.0, new TransferInfo(UUID.randomUUID(), UUID.randomUUID()));

        when(wiremockConfig.getHost()).thenReturn("host");
        when(restTemplate.exchange(eq("host/notificacoes"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        bacenCommand.notify(bankTransfer);

        verify(restTemplate, times(1))
                .exchange(eq("host/notificacoes"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

}
