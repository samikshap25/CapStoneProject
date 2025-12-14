package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryDto;
import com.ecommerce.inventory.dto.ReserveRequest;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    // GET: Check stock
    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable Long productId) {
        int stock = service.getStock(productId);
        return ResponseEntity.ok(stock);
    }

    // PUT: Update stock
    @PutMapping("/{productId}")
    public ResponseEntity<Inventory> updateStock(
            @PathVariable Long productId,
            @RequestBody InventoryDto dto
    ) {
        Inventory updated = service.updateStock(productId, dto.getNewStock());
        return ResponseEntity.ok(updated);
    }

    // POST: Reserve stock
  @PostMapping("/reserve")
    public ResponseEntity<Boolean> reserveStock(@RequestBody ReserveRequest request) {
        try {
            boolean success = service.reserveStock(
                request.getProductId(), 
                request.getQuantity()
            );
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
    }
}

    // POST: Release stock
    @PostMapping("/release")
    public ResponseEntity<String> releaseStock(@RequestBody InventoryDto dto) {
        service.releaseStock(dto.getProductId(), dto.getQuantity());
        return ResponseEntity.ok("Reserved stock released");
    }
}
