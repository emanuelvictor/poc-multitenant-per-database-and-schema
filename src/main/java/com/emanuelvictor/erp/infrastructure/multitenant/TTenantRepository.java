package com.emanuelvictor.erp.infrastructure.multitenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface TTenantRepository extends JpaRepository<TTenant, String> {
}
