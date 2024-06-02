package br.com.farabolini.challenge.unit.adapters.accounts;

import br.com.farabolini.challenge.adapters.accounts.AccountsAdapterImpl;
import br.com.farabolini.challenge.adapters.accounts.AccountsCommand;
import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.TransferInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class AccountsAdapterImplTest {

    @Mock
    private AccountsCommand accountsCommand;

    @InjectMocks
    private AccountsAdapterImpl accountsAdapter;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldReturnAccountInfo() {
        UUID accountId = UUID.randomUUID();
        AccountInfoResponse accountInfo = new AccountInfoResponse(accountId, UUID.randomUUID(), 10.0, true, 10.0);

        when(accountsCommand.getAccountInfo(accountId)).thenReturn(accountInfo);

        AccountInfoResponse accountInfoResponse = accountsAdapter.getAccountInfo(accountId);
        assertEquals(accountInfo, accountInfoResponse);
    }

    @Test
    public void shouldUpdateAccountBalance() {
        BankTransfer bankTransfer = new BankTransfer(UUID.randomUUID(), 10.0, new TransferInfo(UUID.randomUUID(), UUID.randomUUID()));

        accountsAdapter.updateBalance(bankTransfer);
        verify(accountsCommand, times(1)).updateBalance(bankTransfer);
    }

}
