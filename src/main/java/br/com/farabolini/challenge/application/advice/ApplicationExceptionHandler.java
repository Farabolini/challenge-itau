package br.com.farabolini.challenge.application.advice;

import br.com.farabolini.challenge.application.dtos.ApplicationErrorResponse;
import br.com.farabolini.challenge.domain.exceptions.TransferException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ApplicationExceptionHandler extends ResponseStatusExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ApplicationErrorResponse> methodArgumentNotValidHandler(MethodArgumentNotValidException e) {
        List<String> errorMessages = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMessages.add(fieldError.getDefaultMessage());
        }

        ApplicationErrorResponse errResponse = new ApplicationErrorResponse(HttpStatus.BAD_REQUEST.value(),
                String.join(";", errorMessages));
        return new ResponseEntity<>(errResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({TransferException.class})
    public ResponseEntity<ApplicationErrorResponse> transferExceptionHandler(TransferException e) {
        HttpStatus httpStatus = getStatus(e.getClass());
        ApplicationErrorResponse errResponse = new ApplicationErrorResponse(httpStatus.value(), e.getMessage());

        return new ResponseEntity<>(errResponse, httpStatus);
    }

    private HttpStatus getStatus(Class<?> exception) {
        if (exception.isAnnotationPresent(ResponseStatus.class)) {
            ResponseStatus responseStatus = exception.getAnnotation(ResponseStatus.class);
            return responseStatus.code();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
