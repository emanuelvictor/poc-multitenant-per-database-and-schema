package com.emanuelvictor.erp.infrastructure.multitenant;

import javax.sql.DataSource;

public interface TenantDetails {

    String getSchema();

    String getDatabase();

    String getAddress();

    DataSource getDataSource();

    default DataSource dataSourceFromTenant(TenantDetails tenantDetails) {
        return DataSourceFactory.createDataSourceFromTenantDetails(tenantDetails);
    }
}
