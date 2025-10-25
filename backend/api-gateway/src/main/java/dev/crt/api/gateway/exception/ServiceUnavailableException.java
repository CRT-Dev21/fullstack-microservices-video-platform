package dev.crt.api.gateway.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends RuntimeException {
    private final String serviceName;
    private final HttpStatus status;

    public ServiceUnavailableException(String serviceName, HttpStatus status, String message) {
        super(String.format("Service %s is unavailable or returned an error: %s", serviceName, message));
        this.serviceName = serviceName;
        this.status = status;
    }

    public String getServiceName() { return serviceName; }
    public HttpStatus getStatus() { return status; }
}
