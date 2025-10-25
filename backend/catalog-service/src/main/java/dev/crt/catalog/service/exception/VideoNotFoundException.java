package dev.crt.catalog.service.exception;

public class VideoNotFoundException extends RuntimeException{
    public VideoNotFoundException(String message){
        super(message);
    }
}
