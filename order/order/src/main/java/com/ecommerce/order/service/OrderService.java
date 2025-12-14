package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.dto.ReserveRequest;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final RestTemplate restTemplate;

    // ✅ REMOVED CartService - we don't need it
    public OrderService(OrderRepository orderRepo, OrderItemRepository itemRepo, 
                       RestTemplate restTemplate) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.restTemplate = restTemplate;
    }

    // ----------------------- CREATE ORDER -----------------------
    public OrderDto createOrder(OrderDto dto) {

        // 1. Reserve stock by calling INVENTORY SERVICE
        for (OrderItemDto item : dto.getItems()) {
            String url = "http://INVENTORY-SERVICE/inventory/reserve";

            var reserveRequest = new ReserveRequest(item.getProductId(), item.getQuantity());

            try {
                Boolean success = restTemplate.postForObject(url, reserveRequest, Boolean.class);

                if (success == null || !success) {
                    throw new RuntimeException("Stock not available for product: " + item.getProductId());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to reserve stock for product: " + item.getProductId() + ". Error: " + e.getMessage());
            }
        }

        // 2. Create Order entity
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PLACED");

        // Calculate total amount
        BigDecimal total = dto.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        Order savedOrder = orderRepo.save(order);

        // 3. Convert OrderItems
        List<OrderItem> orderItems = dto.getItems().stream().map(item -> {
            OrderItem itemEntity = new OrderItem();
            itemEntity.setOrder(savedOrder);
            itemEntity.setProductId(item.getProductId());  // ✅ FIXED THIS LINE
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setPrice(item.getPrice());
            return itemEntity;
        }).collect(Collectors.toList());

        itemRepo.saveAll(orderItems);

        // 4. Convert back to DTO
        dto.setOrderId(savedOrder.getOrderId());
        dto.setCreatedAt(savedOrder.getCreatedAt());
        dto.setStatus(savedOrder.getStatus());
        dto.setTotalAmount(savedOrder.getTotalAmount());

        return dto;
    }

    // ----------------------- GET ORDER BY ID -----------------------
    public OrderDto getOrder(Long id) {

        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDto> items = order.getItems().stream().map(i -> {
            OrderItemDto d = new OrderItemDto();
            d.setProductId(i.getProductId());
            d.setQuantity(i.getQuantity());
            d.setPrice(i.getPrice());
            return d;
        }).collect(Collectors.toList());

        dto.setItems(items);

        return dto;
    }

    // ----------------------- GET ALL ORDERS -----------------------
    public List<OrderDto> getAllOrders() {

        return orderRepo.findAll().stream().map(order -> {

            OrderDto dto = new OrderDto();
            dto.setOrderId(order.getOrderId());
            dto.setUserId(order.getUserId());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setCreatedAt(order.getCreatedAt());

            List<OrderItemDto> items = order.getItems().stream().map(i -> {
                OrderItemDto itemDto = new OrderItemDto();
                itemDto.setProductId(i.getProductId());
                itemDto.setQuantity(i.getQuantity());
                itemDto.setPrice(i.getPrice());
                return itemDto;
            }).collect(Collectors.toList());

            dto.setItems(items);

            return dto;

        }).collect(Collectors.toList());
    }
}