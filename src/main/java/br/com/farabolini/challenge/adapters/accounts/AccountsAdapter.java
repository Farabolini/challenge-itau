package br.com.farabolini.challenge.adapters.accounts;

import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;

import java.util.UUID;

public interface AccountsAdapter {

    void updateBalance(BankTransfer bankTransfer);

    AccountInfoResponse getAccountInfo(UUID accountId);

}
