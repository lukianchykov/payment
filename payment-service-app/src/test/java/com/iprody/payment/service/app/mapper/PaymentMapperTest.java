package com.iprody.payment.service.app.mapper;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {
    private final PaymentMapper mapper =
            Mappers.getMapper(PaymentMapper.class);

    @Test
    void shouldMapToDto() {
        UUID id = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setGuid(id);
        payment.setAmount(new BigDecimal("123.45"));
        payment.setCurrency("USD");
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setCreatedAt(OffsetDateTime.now());
        payment.setUpdatedAt(OffsetDateTime.now());
        PaymentDto dto = mapper.toDto(payment);
        assertThat(dto).isNotNull();
        assertThat(dto.getGuid()).isEqualTo(payment.getGuid());
        assertThat(dto.getAmount()).isEqualTo(payment.getAmount());
        assertThat(dto.getCurrency()).isEqualTo(payment.getCurrency());
        assertThat(dto.getInquiryRefId()).isEqualTo(payment.getInquiryRefId());

        assertThat(dto.getStatus()).isEqualTo(payment.getStatus());
        assertThat(dto.getCreatedAt()).isEqualTo(payment.getCreatedAt().toInstant());
        assertThat(dto.getUpdatedAt()).isEqualTo(payment.getUpdatedAt().toInstant());
    }

    @Test
    void shouldMapToEntity() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        PaymentDto dto = PaymentDto.builder()
                .guid(id)
                .inquiryRefId(id)
                .amount(new BigDecimal("999.99"))
                .currency("EUR")
                .transactionRefId(id)
                .status(PaymentStatus.PENDING)
                .createdAt(now.toInstant())
                .updatedAt(now.toInstant())
                .build();

        Payment entity = mapper.toEntity(dto);
        assertThat(entity).isNotNull();
        assertThat(entity.getGuid()).isEqualTo(dto.getGuid());
        assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
        assertThat(entity.getCurrency()).isEqualTo(dto.getCurrency());
        assertThat(entity.getInquiryRefId()).isEqualTo(dto.getInquiryRefId());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
        assertThat(entity.getTransactionRefId()).isEqualTo(dto.getTransactionRefId());
        assertThat(entity.getCreatedAt().toInstant()).isEqualTo(dto.getCreatedAt());
        assertThat(entity.getUpdatedAt().toInstant()).isEqualTo(dto.getUpdatedAt());
    }
}