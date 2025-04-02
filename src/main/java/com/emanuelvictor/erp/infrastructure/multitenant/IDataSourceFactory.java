package com.emanuelvictor.erp.infrastructure.multitenant;

import javax.sql.DataSource;

@FunctionalInterface
public interface IDataSourceFactory {

    DataSource createDataSourceFromTenantDetails(final TenantDetails tenantDetails);
}
