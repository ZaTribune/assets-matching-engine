package com.tribune.demo.ame.error;


import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
        ex.printStackTrace(); // Log the full stack trace
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<GenericResponse<String>> handleException(ResponseStatusException ex) {

        logger.error(ex.getReason());
        GenericResponse<String> response = GenericResponse.<String>builder()
                .message(ex.getReason())
                .code(ex.getStatusCode().value())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        logger.error("error");
        List<Object> list = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> String.format("%s : %s", fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        logger.error(list);

        GenericResponse<String> response = GenericResponse.<String>builder()
                .message("Validation Error")
                .reason(list.toArray())
                .code(6002)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

}

