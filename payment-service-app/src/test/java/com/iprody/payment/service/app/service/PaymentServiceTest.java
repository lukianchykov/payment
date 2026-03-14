package com.iprody.payment.service.app.service;


import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private UUID guid;

    @BeforeEach
    void setUp() {
        guid = UUID.randomUUID();

        payment = new Payment();
        payment.setGuid(guid);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setCreatedAt(Instant.now().atOffset(ZoneOffset.UTC));
        payment.setUpdatedAt(Instant.now().atOffset(ZoneOffset.UTC));

        paymentDto = new PaymentDto();
        paymentDto.setGuid(guid);
        paymentDto.setCurrency("USD");
        paymentDto.setStatus(PaymentStatus.APPROVED);
    }

    @Test
    void shouldReturnPaymentById() {
        when(paymentRepository.findById(guid))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment))
                .thenReturn(paymentDto);

        Optional<PaymentDto> result = paymentService.get(guid);

        assertEquals(guid, result.get().getGuid());
        assertEquals(PaymentStatus.APPROVED, result.get().getStatus());

        verify(paymentRepository).findById(guid);
        verify(paymentMapper).toDto(payment);
    }

    @ParameterizedTest
    @MethodSource("statusProvider")
    void shouldSearchByStatus(PaymentStatus status) {

        PaymentFilter filter = new PaymentFilter(
                null,
                null,
                null,
                null,
                null,
                status
        );

        Pageable pageable = Pageable.unpaged();

        payment.setStatus(status);
        paymentDto.setStatus(status);

        Page<Payment> page = new PageImpl<>(List.of(payment));

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(paymentMapper.toDto(payment))
                .thenReturn(paymentDto);

        Page<PaymentDto> result = paymentService.search(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(status, result.getContent().getFirst().getStatus());

        verify(paymentRepository)
                .findAll(any(Specification.class), eq(pageable));
    }


    static Stream<PaymentStatus> statusProvider() {
        return Stream.of(
                PaymentStatus.RECEIVED,
                PaymentStatus.PENDING,
                PaymentStatus.APPROVED,
                PaymentStatus.DECLINED,
                PaymentStatus.NOT_SENT
        );
    }

    @Test
    void shouldSearchWithSorting() {
        PaymentFilter filter = new PaymentFilter(
                null,
                null,
                null,
                null,
                null,
                null
        );

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "amount")
        );

        Page<Payment> page = new PageImpl<>(List.of(payment));

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(paymentMapper.toDto(payment))
                .thenReturn(paymentDto);

        Page<PaymentDto> result = paymentService.search(filter, pageable);

        assertEquals(1, result.getContent().size());
        verify(paymentRepository)
                .findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldSearchWithPaginationDefaults() {
        PaymentFilter filter = new PaymentFilter(
                null,
                null,
                null,
                null,
                null,
                null
        );

        Pageable pageable = PageRequest.of(0, 25);

        Page<Payment> page = new PageImpl<>(
                List.of(payment),
                pageable,
                1
        );

        when(paymentRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(paymentMapper.toDto(payment))
                .thenReturn(paymentDto);

        Page<PaymentDto> result = paymentService.search(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getNumber());
        assertEquals(25, result.getSize());
    }

    @Test
    void shouldUpdateStatus() {
        PaymentStatus newStatus = PaymentStatus.DECLINED;

        when(paymentRepository.updateStatus(guid, newStatus))
                .thenReturn(1);
        when(paymentRepository.findById(guid))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment))
                .thenReturn(paymentDto);

        PaymentDto result = paymentService.updateStatus(guid, newStatus);

        assertEquals(paymentDto, result);

        verify(paymentRepository).updateStatus(guid, newStatus);
        verify(paymentRepository).findById(guid);
        verify(paymentMapper).toDto(payment);
    }

    @Test
    void shouldUpdateNote() {
        String note = "Test note";

        when(paymentRepository.updateNote(guid, note))
                .thenReturn(1);
        when(paymentRepository.findById(guid))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment))
                .thenReturn(paymentDto);

        PaymentDto result = paymentService.updateNote(guid, note);

        assertEquals(paymentDto, result);

        verify(paymentRepository).updateNote(guid, note);
        verify(paymentRepository).findById(guid);
        verify(paymentMapper).toDto(payment);
    }



    @Test
    void shouldThrowExceptionWhenUpdateStatusAndPaymentNotFound() {
        PaymentStatus newStatus = PaymentStatus.DECLINED;

        when(paymentRepository.updateStatus(guid, newStatus))
                .thenReturn(0);

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updateStatus(guid, newStatus)
        );

        assertEquals("Платеж не найден: " + guid, ex.getMessage());

        verify(paymentRepository).updateStatus(guid, newStatus);
        verify(paymentRepository, org.mockito.Mockito.never()).findById(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNoteAndPaymentNotFound() {
        String note = "Test note";

        when(paymentRepository.updateNote(guid, note))
                .thenReturn(0);

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.updateNote(guid, note)
        );

        assertEquals("Платеж не найден: " + guid, ex.getMessage());

        verify(paymentRepository).updateNote(guid, note);
        verify(paymentRepository, org.mockito.Mockito.never()).findById(any());
    }



    @Test
    void shouldReturnAllPayments() {
        when(paymentRepository.findAll())
                .thenReturn(List.of(payment));
        when(paymentMapper.toDto(payment))
                .thenReturn(paymentDto);

        List<PaymentDto> result = paymentService.findAll();

        assertEquals(1, result.size());
        verify(paymentRepository).findAll();
    }
}
