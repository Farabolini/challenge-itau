package br.com.farabolini.challenge.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class GetAccountException extends TransferException {

    private static final String MESSAGE = "Unable to retrieve account data";

    public GetAccountException() {
        super(MESSAGE);
    }

}
