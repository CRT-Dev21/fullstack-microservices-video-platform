package dev.crt.uploader.service.exception;

public class UploadFailedException extends RuntimeException{
    public UploadFailedException(String message, Throwable cause){
        super(message, cause);
    }
}
