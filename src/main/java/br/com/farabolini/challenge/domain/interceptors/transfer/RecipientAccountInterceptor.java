package br.com.farabolini.challenge.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.exceptions.AccountInactiveException;
import br.com.farabolini.challenge.domain.interceptors.BaseInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorOrder.RECIPIENT_ACCOUNT_INTERCEPTOR_ORDER;

@Component
@Slf4j
@Order(RECIPIENT_ACCOUNT_INTERCEPTOR_ORDER)
public class RecipientAccountInterceptor implements BaseInterceptor<TransferInterceptorMessage> {

    @Override
    public void intercept(TransferInterceptorMessage message) {
        if (!message.recipientAccountIsActive()) {
            throw new AccountInactiveException("Recipient");
        }
    }
}
