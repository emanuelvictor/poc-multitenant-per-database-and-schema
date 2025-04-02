package com.emanuelvictor.erp.infrastructure.multitenant;

import com.emanuelvictor.Main;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;

public interface TenantDetails {

    String getSchema();

    String getDatabase();

    String getAddress();

    DataSource getDataSource();

    default DataSource dataSourceFromTenant(TenantDetails tenantDetails) {
        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        return dataSourceFactory.createDataSourceFromTenantDetails(tenantDetails);
    }
}
