package com.emanuelvictor.erp.infrastructure.multitenant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "tenant")
public class TTenant implements TenantDetails {

    @Id
    private String schema;
    @Column(nullable = false)
    private String database;
    @Column(nullable = false)
    private String address;
    @Transient
    private DataSource dataSource;

    public TTenant(String schema, String database, String address, DataSource dataSource) {
        this.schema = schema;
        this.database = database;
        this.address = address;
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = dataSourceFromTenant(this);
        }
        return dataSource;
    }
}
