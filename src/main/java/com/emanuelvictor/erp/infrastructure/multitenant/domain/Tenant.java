package com.emanuelvictor.erp.infrastructure.multitenant.domain;


public record Tenant(String database, String schema, String ipAddress) {

    public boolean isCentral() {
        return schema.equals("central");
    }
}
