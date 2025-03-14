package com.emanuelvictor.erp.infrastructure.multitenant.domain;

public interface TenantDetails {

    String getSchema();

    String getDatabase();

    String getAddress();
}
