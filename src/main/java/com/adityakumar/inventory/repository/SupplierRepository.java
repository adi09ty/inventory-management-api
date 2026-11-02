package com.adityakumar.inventory.repository;

import com.adityakumar.inventory.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
