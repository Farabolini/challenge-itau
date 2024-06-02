package br.com.farabolini.challenge.domain.interceptors.transfer;

import br.com.farabolini.challenge.domain.interceptors.BaseInterceptor;
import br.com.farabolini.challenge.domain.interceptors.InterceptorExecutor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TransferInterceptorExecutor implements InterceptorExecutor<TransferInterceptorMessage> {

    private final List<BaseInterceptor<TransferInterceptorMessage>> interceptors;

    @Override
    public void execute(TransferInterceptorMessage message) {
        interceptors.forEach(interceptor -> interceptor.intercept(message));
    }
}
