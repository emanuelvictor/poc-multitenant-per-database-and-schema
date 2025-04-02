package com.emanuelvictor.erp.infrastructure.multitenant;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class DataSourceFactory implements IDataSourceFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

    public DataSource createDataSourceFromTenantDetails(final TenantDetails tenantDetails) {

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
        final InputStream inputStream = DataSourceFactory.class
                .getClassLoader()
                .getResourceAsStream("config/application.yml");
        try {
            configuration.load(inputStream);
            inputStream.close();
        } catch (Exception e) {
            LOGGER.error("Error to get config properties", e);
            throw new RuntimeException(e);
        }
        return configuration;
    }
}
