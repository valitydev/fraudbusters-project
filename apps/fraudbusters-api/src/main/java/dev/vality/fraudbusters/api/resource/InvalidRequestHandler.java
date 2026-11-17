package dev.vality.fraudbusters.api.resource;

import dev.vality.fraudbusters.api.exceptions.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Answers invalid requests with the BadResponse body of the contract: {@code {code: invalidRequest, message}}.
 */
@Slf4j
@RestControllerAdvice
public class InvalidRequestHandler {

    public static final String INVALID_REQUEST = "invalidRequest";

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidRequest(InvalidRequestException e) {
        log.warn("Invalid request: {}", e.getMessage());
        return Map.of("code", INVALID_REQUEST, "message", e.getMessage());
    }
}
