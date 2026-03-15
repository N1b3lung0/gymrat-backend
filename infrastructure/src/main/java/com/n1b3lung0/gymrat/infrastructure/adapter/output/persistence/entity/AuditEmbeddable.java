package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * JPA embeddable carrying audit metadata for every entity.
 *
 * <p>Maps directly to the audit columns present in all tables:
 * {@code created_at}, {@code created_by}, {@code updated_at}, {@code updated_by},
 * {@code active}, {@code deleted_at}, {@code deleted_by}.
 *
 * <p>The {@link CreatedDate}, {@link CreatedBy}, {@link LastModifiedDate} and
 * {@link LastModifiedBy} annotations are picked up by Spring Data JPA Auditing
 * (enabled via {@code @EnableJpaAuditing} in {@code PersistenceConfig}).
 *
 * <p>Use {@link #fromDomain(com.n1b3lung0.gymrat.domain.model.AuditFields)} and
 * {@link #toDomain()} to convert between the domain value object and this embeddable.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEmbeddable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 255)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", insertable = false, length = 255)
    private String updatedBy;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 255)
    private String deletedBy;

    // -------------------------------------------------------------------------
    // Conversion helpers
    // -------------------------------------------------------------------------

    /**
     * Creates an {@code AuditEmbeddable} from the domain {@link com.n1b3lung0.gymrat.domain.model.AuditFields}.
     *
     * @param audit the domain value object; must not be {@code null}
     * @return a populated embeddable instance
     */
    public static AuditEmbeddable fromDomain(com.n1b3lung0.gymrat.domain.model.AuditFields audit) {
        return AuditEmbeddable.builder()
                .createdAt(audit.createdAt())
                .createdBy(audit.createdBy())
                .updatedAt(audit.updatedAt())
                .updatedBy(audit.updatedBy())
                .active(audit.active())
                .deletedAt(audit.deletedAt())
                .deletedBy(audit.deletedBy())
                .build();
    }

    /**
     * Converts this embeddable back to the domain {@link com.n1b3lung0.gymrat.domain.model.AuditFields}.
     *
     * @return an immutable domain value object
     */
    public com.n1b3lung0.gymrat.domain.model.AuditFields toDomain() {
        return new com.n1b3lung0.gymrat.domain.model.AuditFields(
                createdAt,
                createdBy,
                updatedAt,
                updatedBy,
                active,
                deletedAt,
                deletedBy
        );
    }
}

