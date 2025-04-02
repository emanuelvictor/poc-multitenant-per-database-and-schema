package com.emanuelvictor.erp.infrastructure.multitenant;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class HibernateConfig {

    /**
     *
     */
    private final DataSource dataSource;

    /**
     *
     */
    private final org.springframework.core.env.Environment env;

    /**
     * @return {@link JpaVendorAdapter}
     */
    @Bean
    JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    /**
     * @return {@link LocalContainerEntityManagerFactoryBean}
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.emanuelvictor.erp.infrastructure.multitenant", "com.emanuelvictor.erp.application.adapters.stock");

        em.setJpaVendorAdapter(jpaVendorAdapter());

        final Map<String, Object> properties = new HashMap<>();

        properties.put(Environment.SHOW_SQL, true); // TODO env
        properties.put(Environment.HBM2DDL_AUTO, "validate");  // TODO env

        em.setJpaPropertyMap(properties);

        return em;
    }

    /**
     *
     */
    @Configuration
    @RequiredArgsConstructor
    public static class TransactionManager {

        /**
         *
         */
        private final LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;

        /**
         * @return {@link PlatformTransactionManager }
         */
        @Primary
        @Bean
        public PlatformTransactionManager transactionManager() {
            final JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(localContainerEntityManagerFactoryBean.getObject());
            return transactionManager;
        }
    }
}