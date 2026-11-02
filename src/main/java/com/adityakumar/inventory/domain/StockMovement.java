package com.adityakumar.inventory.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MovementType type;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 255)
    private String note;

    @Column(nullable = false, length = 50)
    private String performedBy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected StockMovement() {}

    public StockMovement(Product product, MovementType type, int quantity, String note, String performedBy) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.note = note;
        this.performedBy = performedBy;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public MovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public String getNote() { return note; }
    public String getPerformedBy() { return performedBy; }
    public Instant getCreatedAt() { return createdAt; }
}
