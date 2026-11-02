package com.adityakumar.inventory.dto;

import com.adityakumar.inventory.domain.MovementType;
import com.adityakumar.inventory.domain.Product;
import com.adityakumar.inventory.domain.StockMovement;
import com.adityakumar.inventory.domain.Supplier;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

/** Request/response shapes for the API, grouped in one file for readability. */
public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 8, max = 100) String password) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record AuthResponse(String token, String tokenType, String username, String role, long expiresInSeconds) {}

    public record SupplierRequest(
            @NotBlank @Size(max = 120) String name,
            @Email @Size(max = 120) String email,
            @Size(max = 30) String phone) {}

    public record SupplierResponse(Long id, String name, String email, String phone) {
        public static SupplierResponse from(Supplier s) {
            return new SupplierResponse(s.getId(), s.getName(), s.getEmail(), s.getPhone());
        }
    }

    public record ProductRequest(
            @NotBlank @Size(max = 40) String sku,
            @NotBlank @Size(max = 150) String name,
            @Size(max = 1000) String description,
            @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @Min(0) int quantity,
            @Min(0) int reorderLevel,
            Long supplierId) {}

    public record ProductUpdateRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 1000) String description,
            @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
            @Min(0) int reorderLevel,
            Long supplierId) {}

    public record ProductResponse(Long id, String sku, String name, String description, BigDecimal price,
                                  int quantity, int reorderLevel, boolean lowStock,
                                  Long supplierId, String supplierName, Instant createdAt) {
        public static ProductResponse from(Product p) {
            Supplier s = p.getSupplier();
            return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(), p.getPrice(),
                    p.getQuantity(), p.getReorderLevel(), p.getQuantity() <= p.getReorderLevel(),
                    s == null ? null : s.getId(), s == null ? null : s.getName(), p.getCreatedAt());
        }
    }

    public record StockAdjustRequest(
            @NotNull MovementType type,
            @Min(1) int quantity,
            @Size(max = 255) String note) {}

    public record StockMovementResponse(Long id, MovementType type, int quantity, String note,
                                        String performedBy, Instant createdAt) {
        public static StockMovementResponse from(StockMovement m) {
            return new StockMovementResponse(m.getId(), m.getType(), m.getQuantity(), m.getNote(),
                    m.getPerformedBy(), m.getCreatedAt());
        }
    }

    public record PageResponse<T>(java.util.List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        }
    }
}
