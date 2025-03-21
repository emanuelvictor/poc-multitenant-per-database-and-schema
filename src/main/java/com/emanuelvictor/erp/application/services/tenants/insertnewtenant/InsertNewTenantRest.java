package com.emanuelvictor.erp.application.services.tenants.insertnewtenant;

import com.emanuelvictor.erp.domain.tenants.Tenant;
import com.emanuelvictor.erp.infrastructure.multitenant.RoutingDataSourceService;
import com.emanuelvictor.erp.infrastructure.multitenant.TTenant;
import com.emanuelvictor.erp.infrastructure.multitenant.TenantDetails;
import com.emanuelvictor.erp.infrastructure.multitenant.TenantDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.emanuelvictor.erp.infrastructure.migration.MigrationService.migrate;
import static com.emanuelvictor.erp.infrastructure.multitenant.TenantDAO.CENTRAL_TENANT;

@RestController
@RequiredArgsConstructor
public class InsertNewTenantRest {

    private final RoutingDataSourceService routingDataSourceService;

    @Transactional
    @PostMapping("tenants")
    public TenantDTO insertNewTenant(@RequestBody TenantDTO tenantDTO) {
        if (tenantDTO.database().equals(CENTRAL_TENANT.getDatabase()))
            return insertNewTenantToCentralDatabase(tenantDTO);
        return insertNewTenantWithNewDatabase(tenantDTO);
    }

    // TODO colocar em um application service
    private TenantDTO insertNewTenantToCentralDatabase(TenantDTO tenantDTO) {
        Tenant tenant = Tenant.create(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address());
        final TenantDetails tenantDetails = new TTenant(tenant.getSchema(), tenant.getDatabase(), tenant.getAddress(), CENTRAL_TENANT.getDataSource());
        TenantDAO.addNewTenant(tenantDetails);
        migrate(tenantDetails);
        routingDataSourceService.configureRoutingDataSources();
        return tenantDTO;
    }

    // TODO colocar em um application service
    private TenantDTO insertNewTenantWithNewDatabase(TenantDTO tenantDTO) {
        Tenant tenant = Tenant.create(tenantDTO.schema(), tenantDTO.database(), tenantDTO.address());
        final TenantDetails tenantDetails = new TTenant(tenant.getSchema(), tenant.getDatabase(), tenant.getAddress(), null);
        TenantDAO.addNewTenant(tenantDetails);
        migrate(tenantDetails);
        routingDataSourceService.configureRoutingDataSources();
        return tenantDTO;
    }
}
