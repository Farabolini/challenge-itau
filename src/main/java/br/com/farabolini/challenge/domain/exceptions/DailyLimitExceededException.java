package br.com.farabolini.challenge.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class DailyLimitExceededException extends TransferException {

    private static final String MESSAGE = "Unable to perform transfer, daily limit exceeded. Current daily limit: %.2f";

    public DailyLimitExceededException(Double currentDailyLimit) {
        super(MESSAGE.formatted(currentDailyLimit));
    }

}
