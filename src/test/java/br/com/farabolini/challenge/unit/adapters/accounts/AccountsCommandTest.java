package br.com.farabolini.challenge.unit.adapters.accounts;

import br.com.farabolini.challenge.adapters.accounts.AccountsCommand;
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

public class AccountsCommandTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WiremockConfig wiremockConfig;

    @InjectMocks
    private AccountsCommand accountsCommand;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldUpdateBalance() {
        BankTransfer bankTransfer = new BankTransfer(UUID.randomUUID(), 10.0, new TransferInfo(UUID.randomUUID(), UUID.randomUUID()));

        when(wiremockConfig.getHost()).thenReturn("host");
        when(restTemplate.exchange(eq("host/contas/saldos"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        accountsCommand.updateBalance(bankTransfer);

        verify(restTemplate, times(1))
                .exchange(eq("host/contas/saldos"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    public void shouldGetAccount() {
        UUID accountId = UUID.randomUUID();
        AccountInfoResponse accountInfo = new AccountInfoResponse(accountId, UUID.randomUUID(), 10.0, true, 10.0);

        when(wiremockConfig.getHost()).thenReturn("host");
        when(restTemplate.exchange(eq("host/contas/%s".formatted(accountId)), eq(HttpMethod.GET), any(HttpEntity.class), eq(AccountInfoResponse.class)))
                .thenReturn(new ResponseEntity<>(accountInfo, HttpStatus.OK));

        AccountInfoResponse response = accountsCommand.getAccountInfo(accountId);

        verify(restTemplate, times(1))
                .exchange(eq("host/contas/%s".formatted(accountId)), eq(HttpMethod.GET), any(HttpEntity.class), eq(AccountInfoResponse.class));
        assertEquals(accountInfo.id(), response.id());
        assertEquals(accountInfo.balance(), response.balance());
        assertEquals(accountInfo.active(), response.active());
        assertEquals(accountInfo.dailyLimit(), response.dailyLimit());
    }

}
