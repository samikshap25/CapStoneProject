package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentDto;
import java.util.List;

public interface PaymentService {

    PaymentDto makePayment(PaymentDto dto);

    PaymentDto getPayment(Long id);

    List<PaymentDto> getAllPayments();
}
