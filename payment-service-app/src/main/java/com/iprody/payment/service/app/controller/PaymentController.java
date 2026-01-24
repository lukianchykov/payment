package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.model.Payment;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final Map<Long, Payment> payments = new HashMap<>();

    public PaymentController() {
        payments.put(1L, new Payment(1L, 99.99));
        payments.put(2L, new Payment(2L, 150.00));
        payments.put(3L, new Payment(3L, 49.50));
        payments.put(4L, new Payment(4L, 299.99));
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return payments.get(id);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments.values());
    }
}
