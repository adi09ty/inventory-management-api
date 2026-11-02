package com.adityakumar.inventory.service;

import com.adityakumar.inventory.domain.Product;
import com.adityakumar.inventory.domain.StockMovement;
import com.adityakumar.inventory.domain.Supplier;
import com.adityakumar.inventory.dto.Dtos.*;
import com.adityakumar.inventory.exception.DuplicateResourceException;
import com.adityakumar.inventory.exception.ResourceNotFoundException;
import com.adityakumar.inventory.repository.ProductRepository;
import com.adityakumar.inventory.repository.StockMovementRepository;
import com.adityakumar.inventory.repository.SupplierRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository products;
    private final SupplierRepository suppliers;
    private final StockMovementRepository movements;

    public ProductService(ProductRepository products, SupplierRepository suppliers, StockMovementRepository movements) {
        this.products = products;
        this.suppliers = suppliers;
        this.movements = movements;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String query, Pageable pageable) {
        var page = (query == null || query.isBlank())
                ? products.findAll(pageable)
                : products.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query, pageable);
        return PageResponse.from(page.map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(get(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> lowStock() {
        return products.findLowStock().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse create(ProductRequest req) {
        if (products.existsBySku(req.sku())) {
            throw new DuplicateResourceException("SKU already exists: " + req.sku());
        }
        Product p = new Product(req.sku(), req.name(), req.description(), req.price(),
                req.quantity(), req.reorderLevel(), supplierOrNull(req.supplierId()));
        return ProductResponse.from(products.save(p));
    }

    public ProductResponse update(Long id, ProductUpdateRequest req) {
        Product p = get(id);
        p.update(req.name(), req.description(), req.price(), req.reorderLevel(), supplierOrNull(req.supplierId()));
        return ProductResponse.from(p);
    }

    public void delete(Long id) {
        products.delete(get(id));
    }

    public ProductResponse adjustStock(Long id, StockAdjustRequest req, String username) {
        Product p = get(id);
        p.adjustStock(req.type(), req.quantity());
        movements.save(new StockMovement(p, req.type(), req.quantity(), req.note(), username));
        return ProductResponse.from(p);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> movements(Long productId, Pageable pageable) {
        get(productId);
        return PageResponse.from(
                movements.findByProductIdOrderByCreatedAtDesc(productId, pageable).map(StockMovementResponse::from));
    }

    private Product get(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Supplier supplierOrNull(Long supplierId) {
        if (supplierId == null) {
            return null;
        }
        return suppliers.findById(supplierId).orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }
}
