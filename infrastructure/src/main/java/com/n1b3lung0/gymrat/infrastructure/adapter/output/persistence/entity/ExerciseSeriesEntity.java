package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the {@code exercise_series} table.
 *
 * <p>Acts as the join entity between {@link WorkoutEntity} and {@link ExerciseEntity},
 * while also owning a collection of {@link SeriesEntity} records.
 */
@Entity
@Table(name = "exercise_series")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE exercise_series SET deleted_at = NOW(), active = false WHERE id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseSeriesEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_exercise_series_workout"))
    private WorkoutEntity workout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_exercise_series_exercise"))
    private ExerciseEntity exercise;

    @OneToMany(mappedBy = "exerciseSeries", fetch = FetchType.LAZY)
    @Builder.Default
    private List<SeriesEntity> seriesEntities = new ArrayList<>();

    @Embedded
    private AuditEmbeddable audit;
}

