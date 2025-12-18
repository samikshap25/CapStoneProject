package com.ecommerce.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock;

    // ===== DERIVED FIELD FOR UI =====
    @Transient
    private int quantity;

    public Inventory() {}

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public void setReservedStock(int reservedStock) {
        this.reservedStock = reservedStock;
    }

    // 🔥 IMPORTANT: UI reads this
    public int getQuantity() {
        return totalStock - reservedStock;
    }
}
