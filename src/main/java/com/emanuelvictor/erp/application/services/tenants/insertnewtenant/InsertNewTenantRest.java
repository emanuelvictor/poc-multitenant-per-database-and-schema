package com.emanuelvictor.erp.application.services.tenants.insertnewtenant;

import com.emanuelvictor.erp.infrastructure.multitenant.domain.TTenant;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TTenantRepository;
import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService.CENTRAL_DATA_SOURCE;

@RestController
@RequiredArgsConstructor
public class InsertNewTenantRest {

    private final TTenantRepository tTenantRepository;
    private final TenantMigrationService tenantMigrationService;

    @Transactional
    @PostMapping("tenants")
    public TenantDTO insertNewTenant(@RequestBody TenantDTO tenantDTO) {
        final TTenant tenant;
        if (tenantDTO.database().equals(CENTRAL_DATA_SOURCE.getDatabase())) {
            tenant = new TTenant(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address(), CENTRAL_DATA_SOURCE.getDataSource());
            tTenantRepository.save(tenant);
            tenantMigrationService.migrate(tenant);
            return tenantDTO;
        }

        tenant = new TTenant(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address(), null);
        tTenantRepository.save(tenant);
        tenantMigrationService.migrate(tenant);
        return tenantDTO;
    }
}
