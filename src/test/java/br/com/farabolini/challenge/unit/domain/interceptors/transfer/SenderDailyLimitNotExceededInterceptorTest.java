package br.com.farabolini.challenge.unit.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.exceptions.DailyLimitExceededException;
import br.com.farabolini.challenge.domain.interceptors.transfer.SenderDailyLimitNotExceededInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

public class SenderDailyLimitNotExceededInterceptorTest {

    @InjectMocks
    SenderDailyLimitNotExceededInterceptor senderDailyLimitNotExceededInterceptor;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldDoNothingWhenAmountIsLessThanDailyLimit() {
        assertDoesNotThrow(() -> senderDailyLimitNotExceededInterceptor.intercept(new TransferInterceptorMessage(true, false, 100.0, 1.0, 100.0)));
    }

    @Test
    public void shouldThrowExceptionAmountIsGreaterThanDailyLimit() {
        DailyLimitExceededException e = assertThrows(DailyLimitExceededException.class,
                () -> senderDailyLimitNotExceededInterceptor.intercept(new TransferInterceptorMessage(false, true, 100.0, 1.0, 99.99)));
        assertEquals("Unable to perform transfer, daily limit exceeded. Current daily limit: 99.99", e.getMessage());
    }

}
