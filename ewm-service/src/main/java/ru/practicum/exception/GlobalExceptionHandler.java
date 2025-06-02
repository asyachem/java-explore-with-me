package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.dto.ApiError;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "The required object was not found.", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Incorrectly made request.", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(ConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, "For the requested operation the conditions are not met.", ex.getMessage());
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String reason, String message) {
        ApiError apiError = new ApiError();
        apiError.setStatus(status.name());
        apiError.setReason(reason);
        apiError.setMessage(message);
        apiError.setTimestamp(LocalDateTime.now());

        return new ResponseEntity<>(apiError, status);
    }
}
