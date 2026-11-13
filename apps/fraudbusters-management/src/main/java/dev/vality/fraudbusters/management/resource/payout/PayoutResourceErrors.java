package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.fraudbusters.management.controller.ErrorController;
import dev.vality.fraudbusters.management.domain.response.ErrorResponse;
import dev.vality.fraudbusters.management.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Invalid payout scopes and missing payout configuration are the caller's mistakes, not server errors. */
@Slf4j
@RestControllerAdvice(basePackageClasses = PayoutResourceErrors.class)
public class PayoutResourceErrors {

    public static final String NOT_FOUND = "notFound";

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequest(IllegalArgumentException e) {
        log.warn("Invalid payout configuration request: {}", e.getMessage());
        return ErrorResponse.builder()
                .code(ErrorController.INVALID_PARAMETERS)
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        log.warn("Payout configuration not found: {}", e.getMessage());
        return ErrorResponse.builder()
                .code(NOT_FOUND)
                .message(e.getMessage())
                .build();
    }
}
