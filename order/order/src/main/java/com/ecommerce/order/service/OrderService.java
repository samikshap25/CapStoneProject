package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.dto.OrderItemDto;
import com.ecommerce.order.dto.ReserveRequest;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    

    public OrderService(OrderRepository orderRepo, OrderItemRepository itemRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
    }

    // ----------------------- MARK ORDER AS PAID -----------------------
    @Transactional
    public void markOrderPaid(Long orderId) {
        log.info("Marking order {} as PAID", orderId);
        
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        order.setStatus("PAID");
        orderRepo.save(order);

        log.info("Order {} marked as PAID successfully", orderId);
    }

    // ----------------------- CREATE ORDER -----------------------
    @Transactional
    public OrderDto createOrder(OrderDto dto) {
        log.info("Creating order for userId: {}", dto.getUserId());
        log.info("Order items: {}", dto.getItems());

        try {
            // 1. Validate input
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one item");
            }

            // 2. Reserve stock from INVENTORY SERVICE (optional in dev mode)
            // 3. Create Order entity
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
            log.info("✅ Order created with ID: {}", savedOrder.getOrderId());

            // 4. Create OrderItems
            List<OrderItem> orderItems = dto.getItems().stream().map(item -> {
                OrderItem itemEntity = new OrderItem();
                itemEntity.setOrder(savedOrder);
                itemEntity.setProductId(item.getProductId());
                itemEntity.setQuantity(item.getQuantity());
                itemEntity.setPrice(item.getPrice());
                return itemEntity;
            }).collect(Collectors.toList());

            itemRepo.saveAll(orderItems);
            log.info("✅ Saved {} order items", orderItems.size());

            // 5. Build response DTO
            dto.setOrderId(savedOrder.getOrderId());
            dto.setCreatedAt(savedOrder.getCreatedAt());
            dto.setStatus(savedOrder.getStatus());
            dto.setTotalAmount(savedOrder.getTotalAmount());

            log.info("✅ Order creation completed successfully for orderId: {}", savedOrder.getOrderId());
            return dto;
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Order creation failed", e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    // ----------------------- RESERVE INVENTORY -----------------------
    private void reserveInventory(List<OrderItemDto> items) {
        for (OrderItemDto item : items) {
            try {
                String url = "http://INVENTORY-SERVICE/inventory/reserve";
                var reserveRequest = new ReserveRequest(item.getProductId(), item.getQuantity());

                log.info("📤 Reserving stock for productId: {}, quantity: {}", 
                        item.getProductId(), item.getQuantity());
                
                
                log.info("✅ Stock reserved successfully for productId: {}", item.getProductId());
            } catch (Exception e) {
                log.error("❌ Failed to reserve stock for product: {}", item.getProductId(), e);
                throw new RuntimeException("Failed to reserve stock for product: " + item.getProductId(), e);
            }
        }
    }

    // ----------------------- GET ORDER BY ID -----------------------
    public OrderDto getOrder(Long id) {
        log.info("Fetching order with ID: {}", id);

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

        log.info("Order {} fetched successfully with {} items", id, items.size());
        return dto;
    }

    // ----------------------- GET ALL ORDERS -----------------------
    public List<OrderDto> getAllOrders() {
        log.info("Fetching all orders");

        List<OrderDto> orders = orderRepo.findAll().stream().map(order -> {

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

        log.info("Fetched {} orders", orders.size());
        return orders;
    }

    // ----------------------- GET ORDERS BY USER ID -----------------------
    public List<OrderDto> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for userId: {}", userId);

        List<Order> userOrders = orderRepo.findAll().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());

        List<OrderDto> orders = userOrders.stream().map(order -> {
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

        log.info("Found {} orders for userId: {}", orders.size(), userId);
        return orders;
    }
}