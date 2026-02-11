package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.PaymentFilterFactory;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public List<Payment> findAll() {
        return this.paymentRepository.findAll();
    }

    public Page<Payment> search(PaymentFilter filter, Pageable pageable) {
        return this.paymentRepository.findAll(PaymentFilterFactory.fromFilter(filter), pageable);
    }

    public Page<Payment> searchPaged(PaymentFilter filter, Pageable
            pageable) {
        Specification<Payment> spec =
                PaymentFilterFactory.fromFilter(filter);
        return paymentRepository.findAll(spec, pageable);
    }
}
