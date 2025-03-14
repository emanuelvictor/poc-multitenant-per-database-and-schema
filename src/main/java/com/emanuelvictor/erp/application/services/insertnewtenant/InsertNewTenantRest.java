package com.emanuelvictor.erp.application.services.insertnewtenant;

import com.emanuelvictor.erp.infrastructure.migration.FlywaySchemaInitializer;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantTable;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InsertNewTenantRest {

    private final TenantTableRepository tenantTableRepository;
    private final FlywaySchemaInitializer flywaySchemaInitializer;

    @PostMapping("tenants")
    @Transactional
    public TenantDTO insertNewTenant(@RequestBody TenantDTO tenantDTO) {
        final var tenant = new TenantTable(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address());
        tenantTableRepository.save(tenant);
        flywaySchemaInitializer.migrate(tenant);
        return tenantDTO;
    }
}
