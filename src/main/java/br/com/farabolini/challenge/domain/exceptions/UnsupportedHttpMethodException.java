package br.com.farabolini.challenge.domain.exceptions;

public class UnsupportedHttpMethodException extends RuntimeException {

    private static final String MESSAGE = "Supported HTTP Methods: GET; POST; PUT";

    public UnsupportedHttpMethodException() {
        super(MESSAGE);
    }

}
