package br.com.farabolini.challenge.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class AccountInactiveException extends TransferException {

    private static final String MESSAGE = "%s account is inactive";

    public AccountInactiveException(String account) {
        super(MESSAGE.formatted(account));
    }

}
