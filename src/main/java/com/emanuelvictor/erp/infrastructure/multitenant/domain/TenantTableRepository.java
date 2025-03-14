package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantTableRepository extends JpaRepository<TenantTable, String> {
}
