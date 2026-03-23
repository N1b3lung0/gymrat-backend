package com.n1b3lung0.gymrat.domain.model;

import java.time.Instant;

/**
 * Value Object that carries audit metadata for every aggregate root.
 *
 * <p>Lives in the domain because the business needs to reason about it:
 * soft-delete checks ({@code active}, {@code deletedAt}) and ownership
 * traceability ({@code createdBy}, {@code updatedBy}) are domain concerns.
 *
 * <p>The JPA entity embeds these fields via {@code @Embedded} +
 * {@code @AttributeOverrides}, keeping the mapping details out of the domain.
 *
 * @param createdAt  timestamp when the aggregate was created; never {@code null}
 * @param createdBy  identity of the actor who created it; never {@code null}
 * @param updatedAt  timestamp of the last update; {@code null} until first update
 * @param updatedBy  identity of the actor who last updated it; {@code null} until first update
 * @param active     {@code false} once the aggregate is soft-deleted
 * @param deletedAt  timestamp of soft-deletion; {@code null} while active
 * @param deletedBy  identity of the actor who soft-deleted it; {@code null} while active
 */
public record AuditFields(
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy,
        boolean active,
        Instant deletedAt,
        String deletedBy
) {

    /**
     * Factory method for newly created aggregates.
     *
     * @param createdBy identity of the actor performing the creation
     * @return an {@code AuditFields} instance with {@code active = true} and no deletion data
     */
    public static AuditFields create(String createdBy) {
        return new AuditFields(
                Instant.now(),
                createdBy,
                null,
                null,
                true,
                null,
                null
        );
    }

    /**
     * Returns a new {@code AuditFields} reflecting an update operation.
     *
     * @param updatedBy identity of the actor performing the update
     * @return updated instance; original is unchanged (record immutability)
     */
    public AuditFields update(String updatedBy) {
        return new AuditFields(
                this.createdAt,
                this.createdBy,
                Instant.now(),
                updatedBy,
                this.active,
                this.deletedAt,
                this.deletedBy
        );
    }

    /**
     * Returns a new {@code AuditFields} marking the aggregate as soft-deleted.
     *
     * @param deletedBy identity of the actor performing the deletion
     * @return deleted instance with {@code active = false}; original is unchanged
     */
    public AuditFields delete(String deletedBy) {
        return new AuditFields(
                this.createdAt,
                this.createdBy,
                this.updatedAt,
                this.updatedBy,
                false,
                Instant.now(),
                deletedBy
        );
    }

    /** Returns {@code true} if the aggregate has been soft-deleted. */
    public boolean isDeleted() {
        return !active;
    }
}

