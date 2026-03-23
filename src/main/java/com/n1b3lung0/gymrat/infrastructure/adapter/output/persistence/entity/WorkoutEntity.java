package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the {@code workouts} table.
 */
@Entity
@Table(name = "workouts")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE workouts SET deleted_at = NOW(), active = false WHERE id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "start_workout", nullable = false)
    private Instant startWorkout;

    @Column(name = "end_workout")
    private Instant endWorkout;

    @OneToMany(mappedBy = "workout", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExerciseSeriesEntity> exerciseSeriesEntities = new ArrayList<>();

    @Embedded
    private AuditEmbeddable audit;
}

