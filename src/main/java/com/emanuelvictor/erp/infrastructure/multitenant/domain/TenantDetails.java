package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import javax.sql.DataSource;

public interface TenantDetails {

    String getSchema();

    String getDatabase();

    String getAddress();

    boolean isCentral();

    DataSource getDataSource();
}
