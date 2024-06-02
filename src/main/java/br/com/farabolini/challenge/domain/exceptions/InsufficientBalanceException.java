package br.com.farabolini.challenge.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends TransferException {

    private static final String MESSAGE = "Insufficient balance to perform transfer, current balance: %.2f";

    public InsufficientBalanceException(Double currentBalance) {
        super(MESSAGE.formatted(currentBalance));
    }

}
