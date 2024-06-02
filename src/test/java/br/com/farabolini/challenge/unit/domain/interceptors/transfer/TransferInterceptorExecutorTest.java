package br.com.farabolini.challenge.unit.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.interceptors.BaseInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.RecipientAccountInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.SenderAccountInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.SenderDailyLimitNotExceededInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.SufficientBalanceInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorExecutor;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.MockitoAnnotations.openMocks;

public class TransferInterceptorExecutorTest {

    @Mock
    private SenderAccountInterceptor senderAccountInterceptor;

    @Mock
    private RecipientAccountInterceptor recipientAccountInterceptor;

    @Mock
    private SufficientBalanceInterceptor sufficientBalanceInterceptor;

    @Mock
    private SenderDailyLimitNotExceededInterceptor senderDailyLimitNotExceededInterceptor;

    private TransferInterceptorExecutor transferInterceptorExecutor;

    @BeforeEach
    public void setup() {
        openMocks(this);
        transferInterceptorExecutor = new TransferInterceptorExecutor(List.of(senderAccountInterceptor, recipientAccountInterceptor, sufficientBalanceInterceptor, senderDailyLimitNotExceededInterceptor));
    }

    @Test
    public void shouldExecuteInterceptorsInSpecificOrder() {
        InOrder inOrder = inOrder(senderAccountInterceptor, recipientAccountInterceptor, sufficientBalanceInterceptor, senderDailyLimitNotExceededInterceptor);
        transferInterceptorExecutor.execute(new TransferInterceptorMessage(true, true, 1.0, 1.0, 1.0));

        inOrder.verify(senderAccountInterceptor).intercept(any(TransferInterceptorMessage.class));
        inOrder.verify(recipientAccountInterceptor).intercept(any(TransferInterceptorMessage.class));
        inOrder.verify(sufficientBalanceInterceptor).intercept(any(TransferInterceptorMessage.class));
        inOrder.verify(senderDailyLimitNotExceededInterceptor).intercept(any(TransferInterceptorMessage.class));

    }

}
