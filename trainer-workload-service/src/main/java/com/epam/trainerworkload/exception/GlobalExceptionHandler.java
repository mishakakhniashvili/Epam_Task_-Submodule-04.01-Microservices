package com.epam.trainerworkload.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            TrainerWorkloadNotFoundException.class,
            MonthlyWorkloadNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(RuntimeException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleMalformedRequest(Exception ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed request or invalid field format"
        );
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message
    ) {
        ApiError error = new ApiError(
                status.value(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(error);
    }
}