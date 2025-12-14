package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.CreatePaymentRequestDto;
import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.dto.VerifyPaymentRequestDto;
import com.ecommerce.payment.exception.PaymentNotFoundException;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final RazorpayClient razorpayClient;
    private final RestTemplate restTemplate;

    @Value("${razorpay.key.secret}")
    private String razorpaySecretKey;

    public PaymentServiceImpl(
            PaymentRepository repository,
            RazorpayClient razorpayClient,
            RestTemplate restTemplate) {
        this.repository = repository;
        this.razorpayClient = razorpayClient;
        this.restTemplate = restTemplate;
    }

    @Override
    public PaymentDto createPayment(CreatePaymentRequestDto request) {

        try {
            JSONObject options = new JSONObject();
            options.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)));
            options.put("currency", "INR");

            Order order = razorpayClient.orders.create(options);

            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setUserId(request.getUserId());
            payment.setAmount(request.getAmount());
            payment.setStatus("PENDING");
            payment.setTransactionId(order.get("id"));

            Payment saved = repository.save(payment);

            PaymentDto dto = new PaymentDto();
            BeanUtils.copyProperties(saved, dto);
            return dto;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentDto verifyPayment(VerifyPaymentRequestDto request) {

        Payment payment = repository.findByTransactionId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        try {
            String payload =
                    request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

            String signature =
                    Utils.getHash(payload, razorpaySecretKey);

            if (!signature.equals(request.getRazorpaySignature())) {
                payment.setStatus("FAILED");
                repository.save(payment);
                throw new RuntimeException("Invalid signature");
            }

            payment.setStatus("SUCCESS");
            payment.setTransactionId(request.getRazorpayPaymentId());
            Payment saved = repository.save(payment);

            restTemplate.put(
                    "http://ORDER-SERVICE/orders/" + payment.getOrderId() + "/paid",
                    null
            );

            restTemplate.delete(
                    "http://CART-SERVICE/cart/clear/" + payment.getUserId()
            );

            PaymentDto dto = new PaymentDto();
            BeanUtils.copyProperties(saved, dto);
            return dto;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaymentDto getPayment(Long id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        PaymentDto dto = new PaymentDto();
        BeanUtils.copyProperties(payment, dto);
        return dto;
    }

    @Override
    public List<PaymentDto> getAllPayments() {
        return repository.findAll()
                .stream()
                .map(p -> {
                    PaymentDto dto = new PaymentDto();
                    BeanUtils.copyProperties(p, dto);
                    return dto;
                }).toList();
    }
}
