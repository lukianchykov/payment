package com.iprody.payment.service.app.service;

import com.iprody.payment.service.app.dto.PaymentDto;
import com.iprody.payment.service.app.mapper.PaymentMapper;
import com.iprody.payment.service.app.persistence.PaymentFilter;
import com.iprody.payment.service.app.persistence.PaymentFilterFactory;
import com.iprody.payment.service.app.persistence.PaymentRepository;
import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private PaymentRepository paymentRepository;
    private PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    public PaymentDto create(PaymentDto dto) {
        final Payment entity = paymentMapper.toEntity(dto);
        entity.setGuid(null);
        final Payment saved = paymentRepository.save(entity);
        return paymentMapper.toDto(saved);
    }

    @Override
    public PaymentDto get(UUID id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toDto)
                .orElseThrow (() -> new IllegalArgumentException("Платеж не найден:" + id));
    }

    @Override
    public Page<PaymentDto> search(PaymentFilter filter, Pageable
            pageable) {
        final Specification<Payment> spec =
                PaymentFilterFactory.fromFilter(filter);
        final Page<Payment> page = paymentRepository.findAll(spec, pageable);
        return page.map(paymentMapper::toDto);
    }

    @Override
    public List<PaymentDto> findAll() {
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    public PaymentDto update(UUID id, PaymentDto dto) {
        if (!paymentRepository.existsById(id)) {
            throw new IllegalArgumentException("Платеж не найден: " + id);
        }
        final Payment updated = paymentMapper.toEntity(dto);
        updated.setGuid(id);
        final Payment saved = paymentRepository.save(updated);
        return paymentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PaymentDto updateStatus(UUID id, PaymentStatus status) {
        final int updatedRows = paymentRepository.updateStatus(id, status);

        if (updatedRows == 0) {
            throw new IllegalArgumentException("Платеж не найден: " + id);
        }

        return paymentRepository.findById(id)
                .map(paymentMapper::toDto)
                .orElseThrow();
    }

    @Override
    @Transactional
    public PaymentDto updateNote(UUID id, String note) {
        final int updatedRows = paymentRepository.updateNote(id, note);

        if (updatedRows == 0) {
            throw new IllegalArgumentException("Платеж не найден: " + id);
        }

        return paymentRepository.findById(id)
                .map(paymentMapper::toDto)
                .orElseThrow();
    }

    public void delete(UUID id) {
        if (!paymentRepository.existsById(id)) {
            throw new IllegalArgumentException("Платеж не найден: " + id);
        }
        paymentRepository.deleteById(id);
    }
}
