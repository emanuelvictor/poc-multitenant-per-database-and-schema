//package com.emanuelvictor.erp.infrastructure.multitenant;
//
//import com.emanuelvictor.erp.infrastructure.multitenant.domain.TTenant;
//import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//
//import static com.emanuelvictor.erp.infrastructure.multitenant.domain.TenantMigrationService.*;
//import static java.util.Optional.ofNullable;
//
//@Component
//public class TenantRoutingDatasource extends AbstractRoutingDataSource {
//
//    private final TenantIdentifierResolver tenantIdentifierResolver;
//
//    TenantRoutingDatasource(TenantIdentifierResolver tenantIdentifierResolver) {
//        this.tenantIdentifierResolver = tenantIdentifierResolver;
//        configureRoutingDataSource();
//    }
//
//
//    void configureRoutingDataSource() {
//        setDefaultTargetDataSource(CENTRAL_DATA_SOURCE.getDataSource());
//
//        final List<TTenant> costumerTenants = getCostumerTenantsWithDatabaseDifferentOfCentral();
//        final HashMap<Object, Object> targetDataSources = new HashMap<>();
//        costumerTenants.forEach(tenant -> {
//            CLIENT_DATA_SOURCES.put(tenant.getSchema(), tenant); // TODO ver se aqui entra primeiro ou lá
//            targetDataSources.put(tenant.getSchema(), tenant.getDataSource());
//        });
//        setTargetDataSources(targetDataSources);
//    }
//
//    @Override
//    protected String determineCurrentLookupKey() {
//        return ofNullable(CLIENT_DATA_SOURCES.get(tenantIdentifierResolver.resolveCurrentTenantIdentifier())).orElse(CENTRAL_DATA_SOURCE).getDatabase();
//    }
//}
