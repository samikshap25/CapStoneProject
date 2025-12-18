package com.ecommerce.inventory.service;

import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    private Inventory getInventoryOrThrow(Long productId) {
        return repository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product ID: " + productId));
    }

    // ✅ FIXED: use getQuantity()
    @Transactional(readOnly = true)
    public int getStock(Long productId) {
        return getInventoryOrThrow(productId).getQuantity();
    }

    // ✅ FIXED: accept productName
    public Inventory createInventory(Long productId, String productName, int initialStock) {
        if (repository.existsByProductId(productId)) {
            throw new IllegalArgumentException("Inventory already exists for product: " + productId);
        }

        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setProductName(productName);
        inventory.setTotalStock(initialStock);
        inventory.setReservedStock(0);

        logger.info("Creating inventory for product {} ({})", productId, productName);
        return repository.save(inventory);
    }

    public Inventory updateStock(Long productId, int newStock) {
        Inventory inventory = getInventoryOrThrow(productId);

        if (newStock < inventory.getReservedStock()) {
            throw new IllegalArgumentException(
                    "Stock cannot be less than reserved stock");
        }

        inventory.setTotalStock(newStock);
        return repository.save(inventory);
    }

    @Transactional(readOnly = true)
    public boolean checkInventory(Long productId, int quantity) {
        return getInventoryOrThrow(productId).getQuantity() >= quantity;
    }

    public boolean reserveStock(Long productId, int quantity) {
        Inventory inventory = getInventoryOrThrow(productId);

        if (inventory.getQuantity() < quantity) {
            return false;
        }

        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        repository.save(inventory);
        return true;
    }

    public void releaseStock(Long productId, int quantity) {
        Inventory inventory = getInventoryOrThrow(productId);
        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        repository.save(inventory);
    }

    public void deductStock(Long productId, int quantity) {
        Inventory inventory = getInventoryOrThrow(productId);
        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        inventory.setTotalStock(inventory.getTotalStock() - quantity);
        repository.save(inventory);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        return repository.findAll();
    }
}
