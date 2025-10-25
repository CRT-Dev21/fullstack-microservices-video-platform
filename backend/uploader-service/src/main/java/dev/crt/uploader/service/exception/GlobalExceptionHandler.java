package dev.crt.uploader.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(ServerWebInputException ex) {
        String message = "Missing form part or invalid input format. Check required fields.";

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message, "VALIDATION_001")));
    }

    @ExceptionHandler({StorageException.class, UploadFailedException.class, Throwable.class})
    public Mono<ResponseEntity<ErrorResponse>> handleInternalErrors(Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        String errorCode = (ex instanceof StorageException) ? "SYSTEM_002_STORAGE" : "SYSTEM_003_PIPELINE";

        return Mono.just(ResponseEntity
                .status(status)
                .body(new ErrorResponse("Internal processing or storage error.", errorCode)));
    }

}
