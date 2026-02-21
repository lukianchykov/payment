package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.dto.PaymentNoteUpdateDto;
import com.iprody.payment.service.app.dto.PaymentStatusUpdateDto;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import com.iprody.payment.service.app.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PaymentDto create(@RequestBody PaymentDto dto) {
        return paymentService.create(dto);
    }

    @GetMapping("/{id}")
    public PaymentDto get(@PathVariable UUID id) {
        return paymentService.get(id);
    }

    @GetMapping
    public List<PaymentDto> findAll() {
        return paymentService.findAll();
    }

    @GetMapping
    public Page<PaymentDto> search(
            PaymentFilter filter,
            Pageable pageable
    ) {
        return paymentService.search(filter, pageable);
    }

    @GetMapping("/search")
    public Page<PaymentDto> search(
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso =
                    DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            Pageable pageable
    ) {
        final PaymentFilter filter = new PaymentFilter(
                currency,
                minAmount,
                maxAmount,
                createdAfter,
                createdBefore,
                status
        );

        return paymentService.search(filter, pageable);
    }

    @PutMapping("/{id}")
    public PaymentDto update(
            @PathVariable UUID id,
            @RequestBody PaymentDto dto
    ) {
        return paymentService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public PaymentDto updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid PaymentStatusUpdateDto dto
    ) {
        return paymentService.updateStatus(id, dto.getStatus());
    }

    @PatchMapping("/{id}/note")
    public PaymentDto updateNote(
            @PathVariable UUID id,
            @RequestBody @Valid PaymentNoteUpdateDto dto
    ) {
        return paymentService.updateNote(id, dto.getNote());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        paymentService.delete(id);
    }
}