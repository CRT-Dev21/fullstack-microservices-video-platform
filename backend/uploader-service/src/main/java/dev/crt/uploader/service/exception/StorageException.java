package dev.crt.uploader.service.exception;

public class StorageException extends RuntimeException{
    public StorageException(String message, Throwable cause){
        super(message, cause);
    }
}
