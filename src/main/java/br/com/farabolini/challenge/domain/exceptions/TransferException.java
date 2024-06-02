package br.com.farabolini.challenge.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class TransferException extends RuntimeException {

    private static final String MESSAGE = "Unable to perform transfer";

    public TransferException(String message) {
        super(message);
    }

    public TransferException() {
        super(MESSAGE);
    }

}
