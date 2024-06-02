package br.com.farabolini.challenge.unit.adapters.bacen;

import br.com.farabolini.challenge.adapters.bacen.BacenAdapterImpl;
import br.com.farabolini.challenge.adapters.bacen.BacenCommand;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.TransferInfo;
import br.com.farabolini.challenge.infrastructure.repositories.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class BacenAdapterImplTest {

    @Mock
    private BacenCommand bacenCommand;

    @Mock
    private RetryTemplate retryTemplate;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private BacenAdapterImpl bacenAdapter;

    private final ExecutorService bacenExecutor = mock(ExecutorService.class);

    @BeforeEach
    public void setup() {
        openMocks(this);
        ReflectionTestUtils.setField(bacenAdapter, "bacenExecutor", bacenExecutor);
    }

    @Test
    public void shouldNotifyBacenAndNotScheduleRetryTemplateOnRetryTemplate() {
        BankTransfer bankTransfer = new BankTransfer(UUID.randomUUID(), 10.0, new TransferInfo(UUID.randomUUID(), UUID.randomUUID()));

        bacenAdapter.notify(bankTransfer);

        verify(bacenCommand, times(1)).notify(bankTransfer);
        verify(transferRepository, times(1)).setBacenNotified(bankTransfer.bankTransferId());
        verify(bacenExecutor, never()).execute(any());
    }

    @Test
    public void shouldNotifyBacenOnRetryTemplate() {
        BankTransfer bankTransfer = new BankTransfer(UUID.randomUUID(), 10.0, new TransferInfo(UUID.randomUUID(), UUID.randomUUID()));
        doThrow(new RuntimeException()).when(bacenCommand).notify(bankTransfer);

        bacenAdapter.notify(bankTransfer);

        verify(bacenCommand, times(1)).notify(bankTransfer);
        verify(bacenExecutor, times(1)).execute(any());
        verify(transferRepository, never()).setBacenNotified(bankTransfer.bankTransferId());
    }

}
