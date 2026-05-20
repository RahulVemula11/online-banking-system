package com.rahul.banking.exception;

import com.rahul.banking.dto.Dtos.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Catches exceptions thrown anywhere in the app and turns them into tidy JSON,
 * so the frontend always gets {"message": "..."} instead of a stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<MessageResponse> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new MessageResponse(ex.getMessage()));
    }

    // Triggered when @Valid fails on an incoming request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Invalid request");
        return ResponseEntity.badRequest().body(new MessageResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Something went wrong: " + ex.getMessage()));
    }
}
