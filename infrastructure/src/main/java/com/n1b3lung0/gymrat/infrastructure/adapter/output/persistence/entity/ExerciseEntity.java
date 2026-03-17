package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity;

import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for the {@code exercises} table.
 *
 * <p>{@code routines} and {@code secondaryMuscles} are stored as element collections
 * in their own join tables. {@code image} and {@code video} are optional
 * many-to-one references to {@link MediaEntity}.
 */
@Entity
@Table(name = "exercises")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE exercises SET deleted_at = NOW(), active = false WHERE id = ?")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 50)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_muscle", nullable = false, length = 50)
    private Muscle primaryMuscle;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "exercise_routines",
            joinColumns = @JoinColumn(name = "exercise_id",
                    foreignKey = @ForeignKey(name = "fk_exercise_routines_exercise"))
    )
    @Column(name = "routine", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Routine> routines = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "exercise_secondary_muscles",
            joinColumns = @JoinColumn(name = "exercise_id",
                    foreignKey = @ForeignKey(name = "fk_exercise_secondary_muscles_exercise"))
    )
    @Column(name = "secondary_muscle", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Muscle> secondaryMuscles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", foreignKey = @ForeignKey(name = "fk_exercises_image"))
    private MediaEntity image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", foreignKey = @ForeignKey(name = "fk_exercises_video"))
    private MediaEntity video;

    @OneToMany(mappedBy = "exercise", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExerciseSeriesEntity> exerciseSeriesEntities = new ArrayList<>();

    @Embedded
    private AuditEmbeddable audit;
}

