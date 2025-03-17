package com.emanuelvictor.erp.infrastructure.multitenant;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public interface TenantDetails {

    String getSchema();

    String getDatabase();

    String getAddress();

    DataSource getDataSource();

    default DataSource dataSourceFromTenant(TenantDetails tenantDetails) {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver"); // TODO put in env
        dataSource.setUsername("central");  // TODO put in env
        dataSource.setPassword("central");  // TODO put in env
        dataSource.setPoolName(tenantDetails.getSchema());
        dataSource.setSchema(tenantDetails.getSchema());
        dataSource.setMaximumPoolSize(10); // TODO put in tenant
        dataSource.setJdbcUrl("jdbc:postgresql://" + tenantDetails.getAddress() + ":5433/" + tenantDetails.getDatabase()); // TODO put in tenant

        return dataSource;
    }
}
