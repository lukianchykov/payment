package com.iprody.payment.service.app.persistence;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByCurrencyAndStatus(String currency, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.currency = :currency AND p.amount >= :minAmount")
    List<Payment> filter(String currency, BigDecimal minAmount);

    @NativeQuery("""
                    SELECT p
                    FROM payments p
                    WHERE (p.currency = :currency OR :currency is null)
                      AND (p.amount >= :minAmount OR :minAmount is null)
                    """)
    List<Payment> filterNative(String currency, BigDecimal minAmount);
}