package br.com.farabolini.challenge.unit.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.exceptions.DailyLimitExceededException;
import br.com.farabolini.challenge.domain.exceptions.InsufficientBalanceException;
import br.com.farabolini.challenge.domain.interceptors.transfer.SenderDailyLimitNotExceededInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.SufficientBalanceInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

public class SufficientBalanceInterceptorTest {

    @InjectMocks
    SufficientBalanceInterceptor sufficientBalanceInterceptor;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldDoNothingWhenAmountIsLessThanDailyLimit() {
        assertDoesNotThrow(() -> sufficientBalanceInterceptor.intercept(new TransferInterceptorMessage(true, false, 100.0, 101.0, 1.0)));
    }

    @Test
    public void shouldThrowExceptionWhenBalanceIsInsufficient() {
        InsufficientBalanceException e = assertThrows(InsufficientBalanceException.class,
                () -> sufficientBalanceInterceptor.intercept(new TransferInterceptorMessage(false, true, 100.0, 1.0, 99.99)));
        assertEquals("Insufficient balance to perform transfer, current balance: 1.00", e.getMessage());
    }

}
