package com.emanuelvictor.erp.infrastructure.multitenant.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "tenant")
public class TenantTable implements TenantDetails {

    @Id
    private String schema;
    @Column(nullable = false)
    private String database;
    @Column(nullable = false)
    private String address;

    public TenantTable(String schema, String database, String address) {
        this.schema = schema;
        this.database = database;
        this.address = address;
    }

    public boolean isCentral() {
        return schema.equals("public");
    }
}
