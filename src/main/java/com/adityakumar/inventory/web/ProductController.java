package com.adityakumar.inventory.web;

import com.adityakumar.inventory.dto.Dtos.*;
import com.adityakumar.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /** Pagination and sorting: ?page=0&size=10&sort=price,desc&search=keyboard */
    @GetMapping
    public PageResponse<ProductResponse> list(@RequestParam(required = false) String search,
                                              @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return service.search(search, pageable);
    }

    @GetMapping("/low-stock")
    public List<ProductResponse> lowStock() {
        return service.lowStock();
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/stock")
    public ProductResponse adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustRequest req,
                                       Authentication auth) {
        return service.adjustStock(id, req, auth.getName());
    }

    @GetMapping("/{id}/movements")
    public PageResponse<StockMovementResponse> movements(@PathVariable Long id,
                                                         @PageableDefault(size = 20) Pageable pageable) {
        return service.movements(id, pageable);
    }
}
