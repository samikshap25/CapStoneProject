package com.ecommerce.inventory.service;

import com.ecommerce.inventory.exception.ResourceNotFoundException;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository repo;

    public InventoryService(InventoryRepository repo) {
        this.repo = repo;
    }

    // Helper function to avoid repeated code
    private Inventory getInventoryOrThrow(Long productId) {
        Inventory inv = repo.findByProductId(productId);
        if (inv == null) {
            throw new ResourceNotFoundException("Inventory not found for product ID: " + productId);
        }
        return inv;
    }

    // Get available stock
    public int getStock(Long productId) {
        Inventory inventory = getInventoryOrThrow(productId);
        return inventory.getTotalStock() - inventory.getReservedStock();
    }

    // Update Stock
    public Inventory updateStock(Long productId, int newStock) {
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        Inventory inv = getInventoryOrThrow(productId);
        inv.setTotalStock(newStock);
        return repo.save(inv);
    }

    // Reserve stock
    public boolean reserveStock(Long productId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Inventory inv = getInventoryOrThrow(productId);

        int available = inv.getTotalStock() - inv.getReservedStock();
        if (available < qty) {
            return false;  // Order service will handle this condition
        }

        inv.setReservedStock(inv.getReservedStock() + qty);
        repo.save(inv);
        return true;
    }

    // Release reserved stock
    public void releaseStock(Long productId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Inventory inv = getInventoryOrThrow(productId);

        if (inv.getReservedStock() < qty) {
            throw new IllegalArgumentException("Cannot release more stock than reserved");
        }

        inv.setReservedStock(inv.getReservedStock() - qty);
        repo.save(inv);
    }

    // Deduct final stock after payment success
    public void deductStock(Long productId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Inventory inv = getInventoryOrThrow(productId);

        int available = inv.getTotalStock() - inv.getReservedStock();
        if (available < qty) {
            throw new IllegalArgumentException("Not enough stock to deduct");
        }

        inv.setReservedStock(inv.getReservedStock() - qty);
        inv.setTotalStock(inv.getTotalStock() - qty);
        repo.save(inv);
    }
}
