package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.CreatePaymentRequestDto;
import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.dto.VerifyPaymentRequestDto;
import com.ecommerce.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping("/create")
public PaymentDto createPayment(@RequestBody CreatePaymentRequestDto request) {
    return service.createPayment(request);
}

    @GetMapping("/{id}")
    public PaymentDto getPayment(@PathVariable Long id) {
        return service.getPayment(id);
    }

    @PostMapping("/verify")
public PaymentDto verifyPayment(@RequestBody VerifyPaymentRequestDto request) {
    return service.verifyPayment(request);
}

    @GetMapping
    public List<PaymentDto> getAllPayments() {
        return service.getAllPayments();
    }
}
