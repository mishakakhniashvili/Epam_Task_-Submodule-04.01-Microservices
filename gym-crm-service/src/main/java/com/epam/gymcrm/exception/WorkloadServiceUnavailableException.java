package com.epam.gymcrm.exception;

public class WorkloadServiceUnavailableException extends RuntimeException {
    public WorkloadServiceUnavailableException(String message,  Throwable cause) {
        super(message, cause);
    }
}
