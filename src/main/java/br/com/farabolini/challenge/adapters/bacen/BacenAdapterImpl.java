package br.com.farabolini.challenge.adapters.bacen;

import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.infrastructure.repositories.TransferRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@AllArgsConstructor
public class BacenAdapterImpl implements BacenAdapter {

    private final BacenCommand bacenCommand;
    private final RetryTemplate bacenRetryTemplate;
    private final TransferRepository transferRepository;
    private final ExecutorService bacenExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void notify(BankTransfer bankTransfer) {
        try {
            notifyBacenAndMarkAsNotified(bankTransfer);
        } catch (Exception e) {
            log.error("BACEN Notifier failed on first attempt, scheduling retry executor...");
            bacenExecutor.execute(() -> notifyOnRetryTemplate(bankTransfer));
        }
    }

    private void notifyOnRetryTemplate(BankTransfer bankTransfer) {
        bacenRetryTemplate.execute(retry -> {
            try {
                bacenCommand.notify(bankTransfer);
                transferRepository.setBacenNotified(bankTransfer.bankTransferId());
            } catch (Exception e) {
                log.error("BACEN Retry Notifier failed due to: ", e);
                throw e;
            }

            log.info("BACEN successfully notified...");
            return true;
        });
    }

    private void notifyBacenAndMarkAsNotified(BankTransfer bankTransfer) {
        bacenCommand.notify(bankTransfer);
        transferRepository.setBacenNotified(bankTransfer.bankTransferId());
    }

}
