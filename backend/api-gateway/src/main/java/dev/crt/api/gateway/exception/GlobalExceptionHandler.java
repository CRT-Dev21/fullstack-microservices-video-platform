package dev.crt.api.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex, ServerWebExchange exchange) {
        System.err.println("Handling ServiceUnavailableException: " + ex.getMessage());

        ErrorResponse errorBody = new ErrorResponse(
                "Failed to communicate with " + ex.getServiceName() + ". Details: " + ex.getMessage(),
                "SERVICE_UNAVAILABLE"
        );
        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
