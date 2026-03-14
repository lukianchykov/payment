package com.iprody.payment.service.app.controller.handler;

import com.iprody.payment.service.app.controller.exception.ErrorMessageDto;
import com.iprody.payment.service.app.controller.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorMessageDto handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ErrorMessageDto(ex.getMessage(), Instant.now());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorMessageDto handleNotFoundException(NotFoundException ex) {
        return new ErrorMessageDto(ex.getMessage(), Instant.now());
    }
}