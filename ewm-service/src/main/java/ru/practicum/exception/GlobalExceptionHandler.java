package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.dto.ApiError;

import java.time.LocalDateTime;

public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        ApiError apiError = new ApiError();
        apiError.setStatus(String.valueOf(HttpStatus.NOT_FOUND));
        apiError.setReason("The required object was not found.");
        apiError.setMessage(ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }
}
