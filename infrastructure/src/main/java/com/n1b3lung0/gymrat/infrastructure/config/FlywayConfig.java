package com.n1b3lung0.gymrat.infrastructure.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Manual Flyway configuration for Spring Boot 4.
 *
 * <p>Spring Boot 4 removed built-in Flyway autoconfiguration, so we wire it explicitly here.
 * Flyway runs on application startup before any JPA operations.
 *
 * <p>Properties are read from {@code application.yaml} under {@code spring.flyway.*}.
 */
@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean(initMethod = "migrate")
    public Flyway flyway(
            DataSource dataSource,
            @Value("${spring.flyway.locations:classpath:db/migration}") String locations,
            @Value("${spring.flyway.default-schema:public}") String defaultSchema,
            @Value("${spring.flyway.baseline-on-migrate:false}") boolean baselineOnMigrate,
            @Value("${spring.flyway.validate-on-migrate:true}") boolean validateOnMigrate,
            @Value("${spring.flyway.out-of-order:false}") boolean outOfOrder) {

        log.info("Configuring Flyway — locations: {}, schema: {}", locations, defaultSchema);

        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                .defaultSchema(defaultSchema)
                .schemas(defaultSchema)
                .baselineOnMigrate(baselineOnMigrate)
                .validateOnMigrate(validateOnMigrate)
                .outOfOrder(outOfOrder)
                .load();
    }
}

