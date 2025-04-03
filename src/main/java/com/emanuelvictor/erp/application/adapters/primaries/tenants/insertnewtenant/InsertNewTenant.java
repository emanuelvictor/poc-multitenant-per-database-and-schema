package com.emanuelvictor.erp.application.adapters.primaries.tenants.insertnewtenant;

import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TTenant;
import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO;
import com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDetails;
import com.emanuelvictor.erp.domain.tenants.Tenant;
import com.emanuelvictor.erp.infrastructure.multitenant.database.RoutingDataSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.emanuelvictor.erp.application.adapters.secundaries.tenant.TenantDAO.CENTRAL_TENANT;
import static com.emanuelvictor.erp.infrastructure.migration.MigrationService.migrate;

@Service
@RequiredArgsConstructor
public class InsertNewTenant {

    private final RoutingDataSourceService routingDataSourceService;

    public TenantDTO insertNewTenant(TenantDTO tenantDTO) {
        if (tenantDTO.database().equals(CENTRAL_TENANT.getDatabase()))
            return insertNewTenantToCentralDatabase(tenantDTO);
        return insertNewTenantWithNewDatabase(tenantDTO);
    }

    private TenantDTO insertNewTenantToCentralDatabase(TenantDTO tenantDTO) {
        Tenant tenant = createTenantInstance(tenantDTO);
        final TenantDetails tenantDetails = new TTenant(tenant.getSchema(), tenant.getDatabase(), tenant.getAddress(), CENTRAL_TENANT.getDataSource());
        TenantDAO.addNewTenant(tenantDetails);
        migrate(tenantDetails);
        routingDataSourceService.configureRoutingDataSources();
        return tenantDTO;
    }

    private TenantDTO insertNewTenantWithNewDatabase(TenantDTO tenantDTO) {
        Tenant tenant = createTenantInstance(tenantDTO);
        final TenantDetails tenantDetails = new TTenant(tenant.getSchema(), tenant.getDatabase(), tenant.getAddress(), null);
        TenantDAO.addNewTenant(tenantDetails);
        migrate(tenantDetails);
        routingDataSourceService.configureRoutingDataSources();
        return tenantDTO;
    }

    static private Tenant createTenantInstance(TenantDTO tenantDTO) {
        return Tenant.create(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address());
    }
}
