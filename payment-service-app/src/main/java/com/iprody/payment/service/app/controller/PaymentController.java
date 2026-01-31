package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;

    @GetMapping("/{guid}")
    public Payment getPaymentById(@PathVariable UUID guid) {
        return paymentRepository.findById(guid)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found with guid: " + guid
                ));
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
