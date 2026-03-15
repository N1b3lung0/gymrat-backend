package com.n1b3lung0.gymrat.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

/**
 * JPA persistence configuration.
 *
 * <p>Enables Spring Data JPA Auditing so that {@code @CreatedDate},
 * {@code @LastModifiedDate}, {@code @CreatedBy} and {@code @LastModifiedBy}
 * annotations in {@link com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.AuditEmbeddable}
 * are populated automatically on save.
 *
 * <p>The {@code auditorProvider} bean returns {@code "system"} as a placeholder
 * until a proper security context is wired in a later step.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableJpaRepositories(
        basePackages = "com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository"
)
public class PersistenceConfig {

    /**
     * Provides the current auditor identity for {@code @CreatedBy} / {@code @LastModifiedBy}.
     * Returns {@code "system"} until a Spring Security context is available.
     *
     * @return an {@link AuditorAware} that always yields {@code "system"}
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}

