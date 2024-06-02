package br.com.farabolini.challenge.adapters.accounts;

import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class AccountsAdapterImpl implements AccountsAdapter {

    private final AccountsCommand accountsCommand;

    @Override
    public void updateBalance(BankTransfer bankTransfer) {
        accountsCommand.updateBalance(bankTransfer);
    }

    @Override
    public AccountInfoResponse getAccountInfo(UUID accountId) {
        return accountsCommand.getAccountInfo(accountId);
    }

}
