package com.emanuelvictor.erp.infrastructure.multitenant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TTenantRepository extends JpaRepository<TTenant, String> {
}
