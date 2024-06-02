package br.com.farabolini.challenge.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.exceptions.InsufficientBalanceException;
import br.com.farabolini.challenge.domain.interceptors.BaseInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorOrder.SUFFICIENT_BALANCE_INTERCEPTOR_ORDER;

@Component
@Slf4j
@Order(SUFFICIENT_BALANCE_INTERCEPTOR_ORDER)
public class SufficientBalanceInterceptor implements BaseInterceptor<TransferInterceptorMessage> {

    @Override
    public void intercept(TransferInterceptorMessage message) {
        if (message.senderBalance() < message.amount()) {
            throw new InsufficientBalanceException(message.senderBalance());
        }
    }

}
