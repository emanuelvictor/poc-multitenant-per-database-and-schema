package com.emanuelvictor.erp.application.adapters.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TProductRepository extends JpaRepository<TProduct, UUID> {
}
