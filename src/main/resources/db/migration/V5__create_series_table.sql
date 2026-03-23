-- V5: Create series table
-- Stores individual sets within an exercise_series.
-- serial_number is the 1-based order of the set; unique per exercise_series.
-- rest_time stores the enum ordinal value (seconds): 30,60,90,120,180,240,300.

CREATE TABLE series (
    id                 UUID          PRIMARY KEY,
    serial_number      INT           NOT NULL,
    repetitions_to_do  INT           NOT NULL,
    repetitions_done   INT,
    intensity          INT           NOT NULL CHECK (intensity BETWEEN 1 AND 10),
    weight             NUMERIC(6, 2),
    start_series       TIMESTAMPTZ,
    end_series         TIMESTAMPTZ,
    rest_time          INT           NOT NULL,
    exercise_series_id UUID          NOT NULL REFERENCES exercise_series(id),
    created_at         TIMESTAMPTZ   NOT NULL,
    created_by         VARCHAR(255)  NOT NULL,
    updated_at         TIMESTAMPTZ,
    updated_by         VARCHAR(255),
    active             BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted_at         TIMESTAMPTZ,
    deleted_by         VARCHAR(255),
    CONSTRAINT uq_series_exercise_series_serial UNIQUE (exercise_series_id, serial_number)
);

CREATE INDEX idx_series_exercise_series ON series (exercise_series_id, serial_number)
    WHERE deleted_at IS NULL;

