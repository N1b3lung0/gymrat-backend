package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

/**
 * JPA entity for the {@code media} table.
 *
 * <p>Soft-delete is handled by {@link SQLDelete} — setting {@code deleted_at}
 * and {@code active = false} instead of physically removing the row.
 * {@link SQLRestriction} ensures Hibernate automatically filters out deleted rows.
 */
@Entity
@Table(name = "media")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE media SET deleted_at = NOW(), active = false WHERE id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Embedded
    private AuditEmbeddable audit;
}

