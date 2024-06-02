package br.com.farabolini.challenge.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class DuplicateBankTransferException extends TransferException {

    private static final String MESSAGE = "Possible duplicity on transfer, do you really want to continue?";

    public DuplicateBankTransferException() {
        super(MESSAGE);
    }

}
