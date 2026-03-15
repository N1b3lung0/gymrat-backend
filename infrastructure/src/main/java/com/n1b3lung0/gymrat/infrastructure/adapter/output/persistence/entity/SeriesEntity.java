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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the {@code series} table.
 *
 * <p>{@code restTime} stores the integer value in seconds (e.g. 30, 60, 90…)
 * matching the {@code RestTime} enum ordinal value defined in the domain.
 */
@Entity
@Table(name = "series")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE series SET deleted_at = NOW(), active = false WHERE id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeriesEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "serial_number", nullable = false)
    private int serialNumber;

    @Column(name = "repetitions_to_do", nullable = false)
    private int repetitionsToDo;

    @Column(name = "repetitions_done")
    private Integer repetitionsDone;

    @Column(name = "intensity", nullable = false)
    private int intensity;

    @Column(name = "weight", precision = 6, scale = 2)
    private BigDecimal weight;

    @Column(name = "start_series")
    private Instant startSeries;

    @Column(name = "end_series")
    private Instant endSeries;

    @Column(name = "rest_time", nullable = false)
    private int restTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_series_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_series_exercise_series"))
    private ExerciseSeriesEntity exerciseSeries;

    @Embedded
    private AuditEmbeddable audit;
}

