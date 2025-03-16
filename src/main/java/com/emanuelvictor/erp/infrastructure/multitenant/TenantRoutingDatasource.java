//package com.emanuelvictor.erp.infrastructure.multitenant;
//
//import com.emanuelvictor.erp.infrastructure.multitenant.domain.TTenant;
//import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantDetails;
//import com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.List;
//
//import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService.dataSourceFromTenant;
//import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService.getCentralTenant;
//import static java.util.Optional.ofNullable;
//
//@Component
//public class TenantRoutingDatasource extends AbstractRoutingDataSource {
//
//    private final TenantMigrationService tenantMigrationService;
//    private final TenantIdentifierResolver tenantIdentifierResolver;
//
//    TenantRoutingDatasource(final TenantMigrationService tenantMigrationService, TenantIdentifierResolver tenantIdentifierResolver) {
//        this.tenantMigrationService = tenantMigrationService;
//        this.tenantIdentifierResolver = tenantIdentifierResolver;
//        this.config(tenantMigrationService.getCostumerTenants());
//    }
//
//    public void config(List<TTenant> tenants) {
//        setDefaultTargetDataSource(dataSourceFromTenant(getCentralTenant()));
//
//        final HashMap<Object, Object> targetDataSources = new HashMap<>();
//        tenants.forEach(tenant -> targetDataSources.put(tenant.getSchema(), dataSourceFromTenant(tenant)));
//        setTargetDataSources(targetDataSources);
//    }
//
//    @Override
//    protected String determineCurrentLookupKey() {
//        return ofNullable(tenantIdentifierResolver.resolveCurrentTenantIdentifier()).orElse(getCentralTenant()).getDatabase();
//    }
//}
