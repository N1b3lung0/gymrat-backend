-- V1: Create media table
-- Stores image and video assets referenced by exercises.
-- Deduplication is enforced via UNIQUE constraint on url.

CREATE TABLE media (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(255),
    description TEXT,
    url         VARCHAR(2048) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMPTZ,
    updated_by  VARCHAR(255),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  VARCHAR(255),
    CONSTRAINT uq_media_url UNIQUE (url)
);

CREATE INDEX idx_media_url_active ON media (url) WHERE deleted_at IS NULL;

