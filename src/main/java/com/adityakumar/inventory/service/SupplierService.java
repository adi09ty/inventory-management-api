package com.adityakumar.inventory.service;

import com.adityakumar.inventory.domain.Supplier;
import com.adityakumar.inventory.dto.Dtos.SupplierRequest;
import com.adityakumar.inventory.dto.Dtos.SupplierResponse;
import com.adityakumar.inventory.exception.DuplicateResourceException;
import com.adityakumar.inventory.exception.ResourceNotFoundException;
import com.adityakumar.inventory.repository.ProductRepository;
import com.adityakumar.inventory.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository suppliers;
    private final ProductRepository products;

    public SupplierService(SupplierRepository suppliers, ProductRepository products) {
        this.suppliers = suppliers;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> findAll() {
        return suppliers.findAll().stream().map(SupplierResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse findById(Long id) {
        return SupplierResponse.from(get(id));
    }

    public SupplierResponse create(SupplierRequest req) {
        return SupplierResponse.from(suppliers.save(new Supplier(req.name(), req.email(), req.phone())));
    }

    public SupplierResponse update(Long id, SupplierRequest req) {
        Supplier s = get(id);
        s.update(req.name(), req.email(), req.phone());
        return SupplierResponse.from(s);
    }

    public void delete(Long id) {
        Supplier s = get(id);
        if (products.existsBySupplierId(id)) {
            throw new DuplicateResourceException("Supplier still has products and cannot be deleted");
        }
        suppliers.delete(s);
    }

    private Supplier get(Long id) {
        return suppliers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }
}
