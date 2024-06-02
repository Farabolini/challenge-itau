package br.com.farabolini.challenge.adapters.accounts;

import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.domain.exceptions.GetAccountException;
import br.com.farabolini.challenge.domain.exceptions.TransferException;
import br.com.farabolini.challenge.infrastructure.configurations.WiremockConfig;
import br.com.farabolini.challenge.infrastructure.rest.RestCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class AccountsCommand extends RestCommand {

    private static final String GET_ACCOUNT = "contas/%s";
    private static final String UPDATE_BALANCE = "contas/saldos";

    protected AccountsCommand(RestTemplate restTemplate, WiremockConfig wiremockConfig) {
        super(restTemplate, wiremockConfig);
    }

    public void updateBalance(BankTransfer bankTransfer) {
        try {
            execute(buildUrl(UPDATE_BALANCE), HttpMethod.PUT, bankTransfer, null, Void.class);
        } catch (RestClientException e) {
            log.error("Update balance failed due to: ", e);
            throw new TransferException();
        }
    }

    public AccountInfoResponse getAccountInfo(UUID accountId) {
        try {
            return execute(buildUrl(GET_ACCOUNT.formatted(accountId)), HttpMethod.GET, null, null, AccountInfoResponse.class);
        } catch (RestClientException e) {
            log.error("Get account failed due to: ", e);
            throw new GetAccountException();
        }
    }

}
