package br.com.farabolini.challenge.unit.domain.services;

import br.com.farabolini.challenge.adapters.accounts.AccountsAdapter;
import br.com.farabolini.challenge.adapters.bacen.BacenAdapter;
import br.com.farabolini.challenge.adapters.customers.CustomersAdapter;
import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;
import br.com.farabolini.challenge.domain.entities.Transfer;
import br.com.farabolini.challenge.domain.exceptions.DuplicateBankTransferException;
import br.com.farabolini.challenge.domain.exceptions.GetAccountException;
import br.com.farabolini.challenge.domain.exceptions.GetCustomerException;
import br.com.farabolini.challenge.domain.exceptions.InsufficientBalanceException;
import br.com.farabolini.challenge.domain.exceptions.TransferException;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorExecutor;
import br.com.farabolini.challenge.domain.interceptors.transfer.TransferInterceptorMessage;
import br.com.farabolini.challenge.domain.services.TransferServiceImpl;
import br.com.farabolini.challenge.infrastructure.repositories.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class TransferServiceImplTest {

    private static final String BANK_TRANSFER_LOCK_PATTERN = "%s->%s=%.2f";

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferInterceptorExecutor transferInterceptorExecutor;

    @Mock
    private CustomersAdapter customersAdapter;

    @Mock
    private AccountsAdapter accountsAdapter;

    @Mock
    private BacenAdapter bacenAdapter;

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private TransferServiceImpl transferService;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldPerformBankTransfer() {
        RLock rLockMock = mock(RLock.class);
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        String lock = BANK_TRANSFER_LOCK_PATTERN.formatted(senderAccountId, recipientAccountId, amount);

        AccountInfoResponse senderAccount = getAccountInfo(senderAccountId);
        CustomerInfoResponse senderInfo = getCustomerInfo(senderAccount.customerId());
        AccountInfoResponse recipientAccount = getAccountInfo(recipientAccountId);
        CustomerInfoResponse recipientInfo = getCustomerInfo(recipientAccount.customerId());
        Transfer transfer = Transfer.builder()
                .id(UUID.randomUUID())
                .senderId(senderAccount.customerId())
                .senderAccountId(senderAccountId)
                .recipientId(recipientAccount.customerId())
                .recipientAccountId(recipientAccountId)
                .bacenNotified(false)
                .amount(amount)
                .createdAt(Instant.now())
                .build();

        when(redissonClient.getFairLock(lock)).thenReturn(rLockMock);
        when(rLockMock.tryLock()).thenReturn(true);
        when(accountsAdapter.getAccountInfo(senderAccountId)).thenReturn(senderAccount);
        when(customersAdapter.getCustomerInfo(senderAccount.customerId())).thenReturn(senderInfo);
        when(accountsAdapter.getAccountInfo(recipientAccountId)).thenReturn(recipientAccount);
        when(customersAdapter.getCustomerInfo(recipientAccount.customerId())).thenReturn(recipientInfo);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        UUID bankTransferId = transferService.transfer(senderAccountId, recipientAccountId, amount);

        assertEquals(transfer.getId(), bankTransferId);
        verify(transferInterceptorExecutor, times(1)).execute(any(TransferInterceptorMessage.class));
        verify(accountsAdapter, times(1)).updateBalance(any(BankTransfer.class));
        verify(bacenAdapter, times(1)).notify(any(BankTransfer.class));
    }

    @Test
    public void shouldThrowDuplicateBankTransferExceptionWhenCouldNotAcquireLock() {
        RLock rLockMock = mock(RLock.class);
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        String lock = BANK_TRANSFER_LOCK_PATTERN.formatted(senderAccountId, recipientAccountId, amount);

        when(redissonClient.getFairLock(lock)).thenReturn(rLockMock);
        when(rLockMock.tryLock()).thenReturn(false);

        DuplicateBankTransferException e = assertThrows(DuplicateBankTransferException.class, () -> transferService.transfer(senderAccountId, recipientAccountId, amount));
        assertEquals("Possible duplicity on transfer, do you really want to continue?", e.getMessage());

        verify(transferInterceptorExecutor, never()).execute(any(TransferInterceptorMessage.class));
        verify(accountsAdapter, never()).updateBalance(any(BankTransfer.class));
        verify(bacenAdapter, never()).notify(any(BankTransfer.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    public void shouldThrowTransferExceptionWhenBankTransferIsNotValid() {
        RLock rLockMock = mock(RLock.class);
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        String lock = BANK_TRANSFER_LOCK_PATTERN.formatted(senderAccountId, recipientAccountId, amount);

        AccountInfoResponse senderAccount = getAccountInfo(senderAccountId);
        CustomerInfoResponse senderInfo = getCustomerInfo(senderAccount.customerId());
        AccountInfoResponse recipientAccount = getAccountInfo(recipientAccountId);
        CustomerInfoResponse recipientInfo = getCustomerInfo(recipientAccount.customerId());

        when(redissonClient.getFairLock(lock)).thenReturn(rLockMock);
        when(rLockMock.tryLock()).thenReturn(true);
        when(accountsAdapter.getAccountInfo(senderAccountId)).thenReturn(senderAccount);
        when(customersAdapter.getCustomerInfo(senderAccount.customerId())).thenReturn(senderInfo);
        when(accountsAdapter.getAccountInfo(recipientAccountId)).thenReturn(recipientAccount);
        when(customersAdapter.getCustomerInfo(recipientAccount.customerId())).thenReturn(recipientInfo);
        doThrow(new InsufficientBalanceException(50.0)).when(transferInterceptorExecutor).execute(any(TransferInterceptorMessage.class));

        InsufficientBalanceException e = assertThrows(InsufficientBalanceException.class, () -> transferService.transfer(senderAccountId, recipientAccountId, amount));
        assertEquals("Insufficient balance to perform transfer, current balance: %.2f".formatted(50.0), e.getMessage());

        verify(accountsAdapter, never()).updateBalance(any(BankTransfer.class));
        verify(bacenAdapter, never()).notify(any(BankTransfer.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    public void shouldThrowExceptionWhenCouldNotAcquireCustomerInfo() {
        RLock rLockMock = mock(RLock.class);
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        String lock = BANK_TRANSFER_LOCK_PATTERN.formatted(senderAccountId, recipientAccountId, amount);
        AccountInfoResponse senderAccount = getAccountInfo(senderAccountId);

        when(redissonClient.getFairLock(lock)).thenReturn(rLockMock);
        when(rLockMock.tryLock()).thenReturn(true);
        when(accountsAdapter.getAccountInfo(senderAccountId)).thenReturn(senderAccount);
        doThrow(new GetCustomerException()).when(customersAdapter).getCustomerInfo(senderAccount.customerId());

        GetCustomerException e = assertThrows(GetCustomerException.class, () -> transferService.transfer(senderAccountId, recipientAccountId, amount));
        assertEquals("Unable to retrieve customer data", e.getMessage());

        verify(transferInterceptorExecutor, never()).execute(any(TransferInterceptorMessage.class));
        verify(accountsAdapter, never()).updateBalance(any(BankTransfer.class));
        verify(bacenAdapter, never()).notify(any(BankTransfer.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    public void shouldThrowGetAccountExceptionWhenCouldNotAcquireAccountData() {
        RLock rLockMock = mock(RLock.class);
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        String lock = BANK_TRANSFER_LOCK_PATTERN.formatted(senderAccountId, recipientAccountId, amount);

        when(redissonClient.getFairLock(lock)).thenReturn(rLockMock);
        when(rLockMock.tryLock()).thenReturn(true);
        doThrow(new GetAccountException()).when(accountsAdapter).getAccountInfo(senderAccountId);

        GetAccountException e = assertThrows(GetAccountException.class, () -> transferService.transfer(senderAccountId, recipientAccountId, amount));
        assertEquals("Unable to retrieve account data", e.getMessage());

        verify(transferInterceptorExecutor, never()).execute(any(TransferInterceptorMessage.class));
        verify(accountsAdapter, never()).updateBalance(any(BankTransfer.class));
        verify(bacenAdapter, never()).notify(any(BankTransfer.class));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    public void shouldThrowExceptionWhenCouldNotUpdateBalance() {
        RLock rLockMock = mock(RLock.class);
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        String lock = BANK_TRANSFER_LOCK_PATTERN.formatted(senderAccountId, recipientAccountId, amount);

        AccountInfoResponse senderAccount = getAccountInfo(senderAccountId);
        CustomerInfoResponse senderInfo = getCustomerInfo(senderAccount.customerId());
        AccountInfoResponse recipientAccount = getAccountInfo(recipientAccountId);
        CustomerInfoResponse recipientInfo = getCustomerInfo(recipientAccount.customerId());
        Transfer transfer = Transfer.builder()
                .id(UUID.randomUUID())
                .senderId(senderAccount.customerId())
                .senderAccountId(senderAccountId)
                .recipientId(recipientAccount.customerId())
                .recipientAccountId(recipientAccountId)
                .bacenNotified(false)
                .amount(amount)
                .createdAt(Instant.now())
                .build();

        when(redissonClient.getFairLock(lock)).thenReturn(rLockMock);
        when(rLockMock.tryLock()).thenReturn(true);
        when(accountsAdapter.getAccountInfo(senderAccountId)).thenReturn(senderAccount);
        when(customersAdapter.getCustomerInfo(senderAccount.customerId())).thenReturn(senderInfo);
        when(accountsAdapter.getAccountInfo(recipientAccountId)).thenReturn(recipientAccount);
        when(customersAdapter.getCustomerInfo(recipientAccount.customerId())).thenReturn(recipientInfo);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        doThrow(new TransferException()).when(accountsAdapter).updateBalance(any(BankTransfer.class));

        TransferException e = assertThrows(TransferException.class, () -> transferService.transfer(senderAccountId, recipientAccountId, amount));
        assertEquals("Unable to perform transfer", e.getMessage());
    }

    private AccountInfoResponse getAccountInfo(UUID accountId) {
        return new AccountInfoResponse(accountId, UUID.randomUUID(), 100.0, true, 50.0);
    }

    private CustomerInfoResponse getCustomerInfo(UUID customerId) {
        return new CustomerInfoResponse(customerId, "Customer", "11999999999", "PF");
    }

}
