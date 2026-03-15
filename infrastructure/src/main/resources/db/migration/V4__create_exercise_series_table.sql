-- V4: Create exercise_series table
-- Join table between workouts and exercises.
-- An exercise may only appear once per workout (UNIQUE constraint).

CREATE TABLE exercise_series (
    id          UUID        PRIMARY KEY,
    workout_id  UUID        NOT NULL REFERENCES workouts(id),
    exercise_id UUID        NOT NULL REFERENCES exercises(id),
    created_at  TIMESTAMPTZ NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMPTZ,
    updated_by  VARCHAR(255),
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  VARCHAR(255),
    CONSTRAINT uq_exercise_series_workout_exercise UNIQUE (workout_id, exercise_id)
);

CREATE INDEX idx_exercise_series_workout  ON exercise_series (workout_id)  WHERE deleted_at IS NULL;
CREATE INDEX idx_exercise_series_exercise ON exercise_series (exercise_id) WHERE deleted_at IS NULL;

