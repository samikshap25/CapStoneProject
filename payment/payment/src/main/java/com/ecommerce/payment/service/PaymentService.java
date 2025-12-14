package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.CreatePaymentRequestDto;
import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.dto.VerifyPaymentRequestDto;

import java.util.List;

public interface PaymentService {

    PaymentDto createPayment(CreatePaymentRequestDto request);

    PaymentDto verifyPayment(VerifyPaymentRequestDto request);

    PaymentDto getPayment(Long id);

    List<PaymentDto> getAllPayments();
}
