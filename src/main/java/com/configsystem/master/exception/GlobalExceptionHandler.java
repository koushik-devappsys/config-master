package com.configsystem.master.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // This tells Spring to catch exceptions from all Controllers
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleBusinessErrors(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("type", "BUSINESS_ERROR");

        // We return 400 (Bad Request) instead of 500 (Server Error)
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}