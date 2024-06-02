package br.com.farabolini.challenge.unit.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.exceptions.AccountInactiveException;
import br.com.farabolini.challenge.domain.interceptors.transfer.RecipientAccountInterceptor;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

public class RecipientAccountInterceptorTest {

    @InjectMocks
    RecipientAccountInterceptor recipientAccountInterceptor;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldDoNothingWhenRecipientAccountIsActive() {
        assertDoesNotThrow(() -> recipientAccountInterceptor.intercept(new TransferInterceptorMessage(false, true, 100.0, 100.0, 100.0)));
    }

    @Test
    public void shouldThrowAccountInactiveException() {
        AccountInactiveException e = assertThrows(AccountInactiveException.class,
                () -> recipientAccountInterceptor.intercept(new TransferInterceptorMessage(true, false, 100.0, 100.0, 100.0)));
        assertEquals("Recipient account is inactive", e.getMessage());
    }

}
