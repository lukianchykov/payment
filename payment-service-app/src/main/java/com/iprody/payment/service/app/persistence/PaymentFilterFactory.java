package com.iprody.payment.service.app.persistence;

import com.iprody.payment.service.app.persistence.entity.Payment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PaymentFilterFactory {

    public static Specification<Payment> fromFilter(PaymentFilter filter) {
        Specification<Payment> spec = Specification.unrestricted();

        if (StringUtils.hasText(filter.currency())) {
            spec = spec.and(PaymentSpecifications.hasCurrency(filter.currency()));
        }

        if (filter.minAmount() != null && filter.maxAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountBetween(
                    filter.minAmount(), filter.maxAmount()));
        }

        if (filter.createdAfter() != null && filter.createdBefore() != null) {
            spec = spec.and(PaymentSpecifications.createdBetween(
                    filter.createdAfter(), filter.createdBefore()));
        }

        if (filter.status() != null) {
            spec = spec.and(PaymentSpecifications.hasStatus(filter.status()));
        }

        return spec;
    }
}