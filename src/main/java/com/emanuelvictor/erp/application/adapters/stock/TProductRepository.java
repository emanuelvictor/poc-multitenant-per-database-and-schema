package com.emanuelvictor.erp.application.adapters.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TProductRepository extends JpaRepository<TProduct, UUID> {
}
