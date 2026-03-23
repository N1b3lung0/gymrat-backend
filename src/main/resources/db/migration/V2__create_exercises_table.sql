-- V2: Create exercises table and related collection tables
-- exercise_routines  : many routines per exercise (element collection)
-- exercise_secondary_muscles : many secondary muscles per exercise (element collection)

CREATE TABLE exercises (
    id             UUID         PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    level          VARCHAR(50)  NOT NULL,
    primary_muscle VARCHAR(50)  NOT NULL,
    image_id       UUID         REFERENCES media(id),
    video_id       UUID         REFERENCES media(id),
    created_at     TIMESTAMPTZ  NOT NULL,
    created_by     VARCHAR(255) NOT NULL,
    updated_at     TIMESTAMPTZ,
    updated_by     VARCHAR(255),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at     TIMESTAMPTZ,
    deleted_by     VARCHAR(255),
    CONSTRAINT uq_exercises_name UNIQUE (name)
);

CREATE INDEX idx_exercises_name_active ON exercises (name) WHERE deleted_at IS NULL;
CREATE INDEX idx_exercises_level       ON exercises (level);
CREATE INDEX idx_exercises_muscle      ON exercises (primary_muscle);

CREATE TABLE exercise_routines (
    exercise_id UUID        NOT NULL REFERENCES exercises(id),
    routine     VARCHAR(50) NOT NULL,
    PRIMARY KEY (exercise_id, routine)
);

CREATE TABLE exercise_secondary_muscles (
    exercise_id      UUID        NOT NULL REFERENCES exercises(id),
    secondary_muscle VARCHAR(50) NOT NULL,
    PRIMARY KEY (exercise_id, secondary_muscle)
);

