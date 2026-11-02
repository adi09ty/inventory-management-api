package com.adityakumar.inventory;

import com.adityakumar.inventory.domain.MovementType;
import com.adityakumar.inventory.domain.Product;
import com.adityakumar.inventory.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductStockTest {

    private Product product(int quantity) {
        return new Product("SKU-1", "Keyboard", null, new BigDecimal("49.99"), quantity, 5, null);
    }

    @Test
    void stockInIncreasesQuantity() {
        Product p = product(10);
        p.adjustStock(MovementType.IN, 5);
        assertEquals(15, p.getQuantity());
    }

    @Test
    void stockOutDecreasesQuantity() {
        Product p = product(10);
        p.adjustStock(MovementType.OUT, 4);
        assertEquals(6, p.getQuantity());
    }

    @Test
    void stockOutBeyondAvailableIsRejectedAndLeavesQuantityUntouched() {
        Product p = product(3);
        assertThrows(InsufficientStockException.class, () -> p.adjustStock(MovementType.OUT, 4));
        assertEquals(3, p.getQuantity());
    }
}
