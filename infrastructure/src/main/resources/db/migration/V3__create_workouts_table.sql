-- V3: Create workouts table

CREATE TABLE workouts (
    id            UUID        PRIMARY KEY,
    start_workout TIMESTAMPTZ NOT NULL,
    end_workout   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL,
    created_by    VARCHAR(255) NOT NULL,
    updated_at    TIMESTAMPTZ,
    updated_by    VARCHAR(255),
    active        BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at    TIMESTAMPTZ,
    deleted_by    VARCHAR(255)
);

CREATE INDEX idx_workouts_start ON workouts (start_workout DESC) WHERE deleted_at IS NULL;

