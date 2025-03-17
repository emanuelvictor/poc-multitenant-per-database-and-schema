package com.emanuelvictor.erp.application.services.tenants.insertnewtenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantService.CENTRAL_DATA_SOURCE;


@RestController
@RequiredArgsConstructor
public class InsertNewTenantRest {

    private final TenantMigrationService tenantMigrationService;

    // TODO criar regra no domain: quando o schema for diferente de 'central', o schema e a database devem ser iguais
    // TODO colocar o schema central como central aou invés de de public
    @Transactional
    @PostMapping("tenants")
    public TenantDTO insertNewTenant(@RequestBody TenantDTO tenantDTO) {
        if (tenantDTO.database().equals(CENTRAL_DATA_SOURCE.getDatabase()))
            return insertNewTenantToCentralDatabase(tenantDTO);
        return insertNewTenantWithNewDatabase(tenantDTO);
    }

    private TenantDTO insertNewTenantToCentralDatabase(TenantDTO tenantDTO) {
        final TenantDetails tenant = new TTenant(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address(), CENTRAL_DATA_SOURCE.getDataSource());
        TenantService.addNewTenant(tenant);
        tenantMigrationService.configureRoutingDataSources(); // TODO não precisa
        tenantMigrationService.migrate(tenant);
        return tenantDTO;
    }

    private TenantDTO insertNewTenantWithNewDatabase(TenantDTO tenantDTO) {
        final TenantDetails tenant = new TTenant(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address(), null);
        TenantService.addNewTenant(tenant);
        tenantMigrationService.configureRoutingDataSources();
        tenantMigrationService.migrate(tenant);
        return tenantDTO;
    }
}
