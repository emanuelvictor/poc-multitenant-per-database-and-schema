package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TTenantRepository extends JpaRepository<TTenant, String> {
}
