package br.com.farabolini.challenge.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.exceptions.DailyLimitExceededException;
import br.com.farabolini.challenge.domain.interceptors.BaseInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorOrder.SENDER_DAILY_LIMIT_NOT_EXCEEDED_INTERCEPTOR_ORDER;

@Component
@Slf4j
@Order(SENDER_DAILY_LIMIT_NOT_EXCEEDED_INTERCEPTOR_ORDER)
public class SenderDailyLimitNotExceededInterceptor implements BaseInterceptor<TransferInterceptorMessage> {

    @Override
    public void intercept(TransferInterceptorMessage message) {
        if (message.senderDailyLimit() < message.amount()) {
            throw new DailyLimitExceededException(message.senderDailyLimit());
        }
    }
}
