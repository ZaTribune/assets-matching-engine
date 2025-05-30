package com.tribune.demo.ame.error;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;



@RestControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<GenericResponse<String>> handleException(IllegalArgumentException ex) {

        logger.error(ex.getMessage());
        GenericResponse<String> response = GenericResponse.<String>builder()
                .message(ex.getMessage())
                .code(6002)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        logger.error("Invalid input error! {}", ex);

        GenericResponse<String> response = GenericResponse.<String>builder()
                .message("Validation Error")
                .reason(ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError -> String.format("%s : %s", fieldError.getField(), fieldError.getDefaultMessage()))
                        .toArray())
                .code(6002)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

}

