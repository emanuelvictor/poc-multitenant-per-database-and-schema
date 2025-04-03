package com.emanuelvictor.erp.application.adapters.secundaries.tenant;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public interface TenantDetails {

    Logger LOGGER = LoggerFactory.getLogger(TenantDetails.class);

    String getSchema();

    String getDatabase();

    String getAddress();

    DataSource getDataSource();

    default DataSource dataSourceFromTenant(TenantDetails tenantDetails) {
        return createDataSourceFromTenantDetails(tenantDetails);
    }

    private static DataSource createDataSourceFromTenantDetails(final TenantDetails tenantDetails) {

        final Properties configuration = loadProperties();
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(configuration.getProperty("driver-class-name"));
        dataSource.setMaximumPoolSize(Integer.parseInt(Objects.requireNonNull(configuration.getProperty("hikari.maximum-pool-size"))));
        dataSource.setPoolName(tenantDetails.getSchema());
        dataSource.setJdbcUrl("jdbc:postgresql://" + tenantDetails.getAddress() + ":" + configuration.getProperty("port-number") + "/" + tenantDetails.getDatabase());
        dataSource.setUsername(configuration.getProperty("username"));
        dataSource.setPassword(configuration.getProperty("password"));

        return dataSource;
    }

    static private Properties loadProperties() {
        final Properties configuration = new Properties();
        final InputStream inputStream = TenantDetails.class.getClassLoader()
                .getResourceAsStream("config/application.yml");
        try {
            configuration.load(inputStream);
            assert inputStream != null;
            inputStream.close();
        } catch (Exception e) {
            LOGGER.error("Error to get config properties", e);
            throw new RuntimeException(e);
        }
        return configuration;
    }
}
