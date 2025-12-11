package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentDto;
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

    @PostMapping
    public PaymentDto makePayment(@RequestBody PaymentDto dto) {
        return service.makePayment(dto);
    }

    @GetMapping("/{id}")
    public PaymentDto getPayment(@PathVariable Long id) {
        return service.getPayment(id);
    }

    @GetMapping
    public List<PaymentDto> getAllPayments() {
        return service.getAllPayments();
    }
}
