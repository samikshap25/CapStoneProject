package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryDto;
import com.ecommerce.inventory.dto.ReserveRequest;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.service.InventoryService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    // ✅ Used by Order / Product service
    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok(service.getStock(productId));
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> checkInventory(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(service.checkInventory(productId, quantity));
    }

    // ✅ FIXED: include productName
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody InventoryDto dto) {
        Inventory created = service.createInventory(
                dto.getProductId(),
                dto.getProductName(),
                dto.getQuantity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ✅ Used by Admin UI
    @PutMapping("/{productId}")
    public ResponseEntity<Inventory> updateStock(
            @PathVariable Long productId,
            @RequestBody InventoryDto dto
    ) {
        return ResponseEntity.ok(
                service.updateStock(productId, dto.getQuantity())
        );
    }

    @PostMapping("/reserve")
    public ResponseEntity<Boolean> reserveStock(@RequestBody ReserveRequest request) {
        return ResponseEntity.ok(
                service.reserveStock(request.getProductId(), request.getQuantity())
        );
    }

    @PostMapping("/release")
    public ResponseEntity<String> releaseStock(@RequestBody InventoryDto dto) {
        service.releaseStock(dto.getProductId(), dto.getQuantity());
        return ResponseEntity.ok("Reserved stock released");
    }

    @PostMapping("/deduct")
    public ResponseEntity<String> deductStock(@RequestBody InventoryDto dto) {
        service.deductStock(dto.getProductId(), dto.getQuantity());
        return ResponseEntity.ok("Stock deducted successfully");
    }

    // ✅ Used by Admin Inventory page
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(service.getAllInventory());
    }
}
