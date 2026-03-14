package com.iprody.payment.service.app.controller.exception;

import java.time.Instant;

public record ErrorMessageDto(
        String message,
        Instant timestamp
) {
}
