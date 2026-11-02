package com.adityakumar.inventory.domain;

import com.adityakumar.inventory.exception.InsufficientStockException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reorderLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /** Optimistic locking: concurrent stock updates fail instead of silently overwriting. */
    @Version
    private Long version;

    protected Product() {}

    public Product(String sku, String name, String description, BigDecimal price,
                   int quantity, int reorderLevel, Supplier supplier) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.supplier = supplier;
    }

    public void update(String name, String description, BigDecimal price, int reorderLevel, Supplier supplier) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.reorderLevel = reorderLevel;
        this.supplier = supplier;
    }

    public void adjustStock(MovementType type, int amount) {
        if (type == MovementType.OUT) {
            if (amount > quantity) {
                throw new InsufficientStockException(sku, quantity, amount);
            }
            quantity -= amount;
        } else {
            quantity += amount;
        }
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public Supplier getSupplier() { return supplier; }
    public Instant getCreatedAt() { return createdAt; }
}
