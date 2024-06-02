package br.com.farabolini.challenge.domain.services;

import br.com.farabolini.challenge.adapters.accounts.AccountsAdapter;
import br.com.farabolini.challenge.adapters.bacen.BacenAdapter;
import br.com.farabolini.challenge.adapters.customers.CustomersAdapter;
import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;
import br.com.farabolini.challenge.application.dtos.TransferInfo;
import br.com.farabolini.challenge.domain.contracts.TransferService;
import br.com.farabolini.challenge.domain.entities.Transfer;
import br.com.farabolini.challenge.domain.exceptions.DuplicateBankTransferException;
import br.com.farabolini.challenge.domain.exceptions.TransferException;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorExecutor;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorMessage;
import br.com.farabolini.challenge.infrastructure.repositories.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class TransferServiceImpl implements TransferService {

    private static final String BANK_TRANSFER_LOCK = "%s->%s=%.2f";

    private final TransferRepository transferRepository;
    private final TransferInterceptorExecutor transferInterceptorExecutor;
    private final CustomersAdapter customersAdapter;
    private final AccountsAdapter accountsAdapter;
    private final BacenAdapter bacenAdapter;
    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public UUID transfer(UUID senderAccountId, UUID recipientAccountId, Double amount) {
        String bankTransferLock = BANK_TRANSFER_LOCK.formatted(senderAccountId, recipientAccountId, amount);
        RLock lock = redissonClient.getFairLock(bankTransferLock);

        try {
            boolean gotLock = lock.tryLock();
            if (gotLock) {
                return performBankTransfer(senderAccountId, recipientAccountId, amount);
            }

            throw new DuplicateBankTransferException();
        } catch (Exception e) {
            log.error("Bank transfer failed due to: ", e);
            throw e;
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                log.info("Releasing lock {}", bankTransferLock);
                lock.unlock();
            }
        }
    }

    private UUID performBankTransfer(UUID senderAccountId, UUID recipientAccountId, Double amount) {
        AccountInfoResponse senderAccountInfo = accountsAdapter.getAccountInfo(senderAccountId);
        CustomerInfoResponse senderInfo = customersAdapter.getCustomerInfo(senderAccountInfo.customerId());
        AccountInfoResponse recipientAccountInfo = accountsAdapter.getAccountInfo(recipientAccountId);
        CustomerInfoResponse recipientInfo = customersAdapter.getCustomerInfo(recipientAccountInfo.customerId());

        validateBankTransfer(new TransferInterceptorMessage(senderAccountInfo.active(), recipientAccountInfo.active(),
                amount, senderAccountInfo.balance(), senderAccountInfo.dailyLimit()));

        UUID bankTransferId = saveBankTransfer(senderInfo.id(), senderAccountId, recipientInfo.id(), recipientAccountId, amount);
        updateBalanceAndNotifyBACEN(new BankTransfer(bankTransferId, amount, new TransferInfo(senderAccountId, recipientAccountId)));

        return bankTransferId;
    }

    private void updateBalanceAndNotifyBACEN(BankTransfer bankTransfer) {
        accountsAdapter.updateBalance(bankTransfer);
        bacenAdapter.notify(bankTransfer);
    }

    private void validateBankTransfer(TransferInterceptorMessage message) {
        try {
            transferInterceptorExecutor.execute(message);
        } catch (TransferException e) {
            log.error("Unable to perform bank transfer, due to: {}", e.getMessage());
            throw e;
        }
    }

    private UUID saveBankTransfer(UUID senderId, UUID senderAccountId, UUID recipientId, UUID recipientAccountId, Double amount) {
        Transfer transfer = Transfer.builder()
                .senderId(senderId)
                .senderAccountId(senderAccountId)
                .recipientId(recipientId)
                .recipientAccountId(recipientAccountId)
                .amount(amount).build();

        return transferRepository.save(transfer).getId();
    }

}
