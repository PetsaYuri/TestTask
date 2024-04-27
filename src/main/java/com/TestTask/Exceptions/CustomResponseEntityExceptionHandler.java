package com.TestTask.Exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.Timestamp;
import java.util.Calendar;

@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<ErrorDTO> handleBadRequestException(RuntimeException ex, WebRequest webRequest) {
        Timestamp timestamp = Timestamp.from(Calendar.getInstance().toInstant());
        String[] messageSegments = ex.getMessage().split(" ", 3);
        int status = Integer.parseInt(messageSegments[0]);
        String error = messageSegments[1];
        String message = messageSegments[2].replaceAll("\"", "");
        String path = webRequest.getDescription(false).replace("uri=", "");

        return ResponseEntity.status(status).body(
                new ErrorDTO(timestamp, status, error, message, path));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<ErrorDTO> handleEntityNotFoundException(RuntimeException ex, WebRequest webRequest) {
        Timestamp timestamp = Timestamp.from(Calendar.getInstance().toInstant());
        int status = HttpStatus.NOT_FOUND.value();
        String error = HttpStatus.NOT_FOUND.name();
        String message = ex.getMessage().replace("com.TestTask.Users.", "");
        String path = webRequest.getDescription(false).replace("uri=", "");

        return ResponseEntity.status(status).body(
                new ErrorDTO(timestamp, status, error, message, path));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest webRequest) {
        Timestamp timestamp = Timestamp.from(Calendar.getInstance().toInstant());
        int responseStatus = status.value();
        String error = HttpStatus.valueOf(status.value()).name();

        String[] messageSegments = ex.getFieldError().toString().split("default message \\[", 3);
        String fieldName = messageSegments[1].split("]")[0];
        String description = messageSegments[2].split("]")[0];
        String message = "The " + fieldName + " field " + description;

        String path = webRequest.getDescription(false).replace("uri=", "");

        return ResponseEntity.status(responseStatus).body(
                new ErrorDTO(timestamp, responseStatus, error, message, path));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorDTO> handleConstraintViolationException(ConstraintViolationException ex, WebRequest webRequest) {
        Timestamp timestamp = Timestamp.from(Calendar.getInstance().toInstant());
        int status = HttpStatus.BAD_REQUEST.value();
        String error = HttpStatus.BAD_REQUEST.name();

        String fieldName = ex.getConstraintViolations().toString().split("propertyPath=", 2)[1].split(",", 2)[0];
        String description = ex.getConstraintViolations().toString().split("interpolatedMessage='", 2)[1].split("'", 2)[0];
        String message = "The " + fieldName + " field " + description;

        String path = webRequest.getDescription(false).replace("uri=", "");
        return ResponseEntity.status(status).body(
                new ErrorDTO(timestamp, status, error, message, path));
    }

    @ExceptionHandler(InvalidFormatException.class)
    protected ResponseEntity<ErrorDTO> handleInvalidFormatException(InvalidFormatException ex, WebRequest webRequest) {
        Timestamp timestamp = Timestamp.from(Calendar.getInstance().toInstant());
        int status = HttpStatus.BAD_REQUEST.value();
        String error = HttpStatus.BAD_REQUEST.name();
        String message = ex.getMessage().split("\\(error: ", 2)[1].split(" at \\[")[0];
        String path = webRequest.getDescription(false).replace("uri=", "");

        return ResponseEntity.status(status).body(
                new ErrorDTO(timestamp, status, error, message, path));
    }
}

