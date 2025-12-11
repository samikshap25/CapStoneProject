package com.ecommerce.payment.service;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.model.Payment;

import repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;

    public PaymentServiceImpl(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentDto makePayment(PaymentDto dto) {
        Payment payment = new Payment();
        BeanUtils.copyProperties(dto, payment);

        payment.setStatus("SUCCESS");
        payment.setTransactionId("TXN-" + System.currentTimeMillis());

        Payment saved = repository.save(payment);

        PaymentDto result = new PaymentDto();
        BeanUtils.copyProperties(saved, result);

        return result;
    }

    @Override
    public PaymentDto getPayment(Long id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id " + id));

        PaymentDto dto = new PaymentDto();
        BeanUtils.copyProperties(payment, dto);
        return dto;
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return repository.findAll()
                .stream()
                .map(payment -> {
                    PaymentDto dto = new PaymentDto();
                    BeanUtils.copyProperties(payment, dto);
                    return dto;
                }).toList();
    }
}
