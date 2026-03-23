-- V6: Seed sample data for development and manual testing
-- ─────────────────────────────────────────────────────────────────────────────
-- 3  media rows   (2 images + 1 video)
-- 5  exercises    (varied level / muscle / routine combinations)
-- 2  workouts     (1 finished, 1 open)
-- 3  exercise_series rows
-- 6  series rows  (3 sets per exercise_series pair)
-- ─────────────────────────────────────────────────────────────────────────────

-- ─────────────────────────────────────────────────────────────────────────────
-- MEDIA
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO media (id, name, description, url, created_at, created_by, active)
VALUES
    ('a1000000-0000-0000-0000-000000000001',
     'Bench Press Form',
     'Correct grip and arc for the flat barbell bench press',
     'https://cdn.gymrat.app/media/bench-press-form.jpg',
     '2026-01-01T08:00:00Z', 'seed', TRUE),

    ('a1000000-0000-0000-0000-000000000002',
     'Pull-Up Grip',
     'Pronated vs supinated grip comparison for pull-ups',
     'https://cdn.gymrat.app/media/pullup-grip.jpg',
     '2026-01-01T08:00:00Z', 'seed', TRUE),

    ('a1000000-0000-0000-0000-000000000003',
     'Squat Tutorial',
     'Full squat mechanics: depth, knee tracking and bracing',
     'https://cdn.gymrat.app/media/squat-tutorial.mp4',
     '2026-01-01T08:00:00Z', 'seed', TRUE);

-- ─────────────────────────────────────────────────────────────────────────────
-- EXERCISES
-- ─────────────────────────────────────────────────────────────────────────────
-- Levels  : BEGINNER | INTERMEDIATE | ADVANCED
-- Muscles : CHEST | BACK | SHOULDERS | BICEPS | TRICEPS | QUADRICEPS |
--           HAMSTRINGS | GLUTEAL | CORE | LUMBAR | CALFS |
--           ABDUCTORS | ADDUCTORS | FOREARMS | TRAPEZE
-- Routines: PUSH | PULL | LEG | FULLBODY | UPPERBODY | ARMS | SHOULDERS

INSERT INTO exercises
    (id, name, description, level, primary_muscle, image_id, video_id,
     created_at, created_by, active)
VALUES
    -- 1. Bench Press — BEGINNER, CHEST, PUSH
    ('e1000000-0000-0000-0000-000000000001',
     'Bench Press',
     'Classic compound push movement targeting the chest with barbell on a flat bench.',
     'BEGINNER', 'CHEST',
     'a1000000-0000-0000-0000-000000000001', NULL,
     '2026-01-01T08:00:00Z', 'seed', TRUE),

    -- 2. Pull-Up — INTERMEDIATE, BACK, PULL
    ('e1000000-0000-0000-0000-000000000002',
     'Pull-Up',
     'Bodyweight vertical pull targeting the lats and biceps.',
     'INTERMEDIATE', 'BACK',
     'a1000000-0000-0000-0000-000000000002', NULL,
     '2026-01-01T08:00:00Z', 'seed', TRUE),

    -- 3. Back Squat — INTERMEDIATE, QUADRICEPS, LEG
    ('e1000000-0000-0000-0000-000000000003',
     'Back Squat',
     'Barbell back squat — the king of lower body compound movements.',
     'INTERMEDIATE', 'QUADRICEPS',
     NULL, 'a1000000-0000-0000-0000-000000000003',
     '2026-01-01T08:00:00Z', 'seed', TRUE),

    -- 4. Overhead Press — ADVANCED, SHOULDERS, PUSH + SHOULDERS
    ('e1000000-0000-0000-0000-000000000004',
     'Overhead Press',
     'Standing barbell press targeting deltoids and triceps.',
     'ADVANCED', 'SHOULDERS',
     NULL, NULL,
     '2026-01-01T08:00:00Z', 'seed', TRUE),

    -- 5. Deadlift — ADVANCED, LUMBAR, FULLBODY
    ('e1000000-0000-0000-0000-000000000005',
     'Deadlift',
     'Full-body hinge movement — primary driver of posterior chain strength.',
     'ADVANCED', 'LUMBAR',
     NULL, NULL,
     '2026-01-01T08:00:00Z', 'seed', TRUE);

-- Routines (element collection)
INSERT INTO exercise_routines (exercise_id, routine) VALUES
    ('e1000000-0000-0000-0000-000000000001', 'PUSH'),
    ('e1000000-0000-0000-0000-000000000001', 'UPPERBODY'),
    ('e1000000-0000-0000-0000-000000000002', 'PULL'),
    ('e1000000-0000-0000-0000-000000000002', 'UPPERBODY'),
    ('e1000000-0000-0000-0000-000000000003', 'LEG'),
    ('e1000000-0000-0000-0000-000000000004', 'PUSH'),
    ('e1000000-0000-0000-0000-000000000004', 'SHOULDERS'),
    ('e1000000-0000-0000-0000-000000000005', 'FULLBODY'),
    ('e1000000-0000-0000-0000-000000000005', 'PULL');

-- Secondary muscles (element collection)
INSERT INTO exercise_secondary_muscles (exercise_id, secondary_muscle) VALUES
    ('e1000000-0000-0000-0000-000000000001', 'TRICEPS'),
    ('e1000000-0000-0000-0000-000000000001', 'SHOULDERS'),
    ('e1000000-0000-0000-0000-000000000002', 'BICEPS'),
    ('e1000000-0000-0000-0000-000000000002', 'CORE'),
    ('e1000000-0000-0000-0000-000000000003', 'GLUTEAL'),
    ('e1000000-0000-0000-0000-000000000003', 'HAMSTRINGS'),
    ('e1000000-0000-0000-0000-000000000003', 'CORE'),
    ('e1000000-0000-0000-0000-000000000004', 'TRICEPS'),
    ('e1000000-0000-0000-0000-000000000004', 'CORE'),
    ('e1000000-0000-0000-0000-000000000005', 'HAMSTRINGS'),
    ('e1000000-0000-0000-0000-000000000005', 'GLUTEAL'),
    ('e1000000-0000-0000-0000-000000000005', 'FOREARMS');

-- ─────────────────────────────────────────────────────────────────────────────
-- WORKOUTS
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO workouts (id, start_workout, end_workout, created_at, created_by, active)
VALUES
    -- Workout 1: finished push session
    ('b1000000-0000-0000-0000-000000000001',
     '2026-03-10T09:00:00Z', '2026-03-10T10:15:00Z',
     '2026-03-10T09:00:00Z', 'seed', TRUE),

    -- Workout 2: open / in-progress leg session
    ('b1000000-0000-0000-0000-000000000002',
     '2026-03-17T09:00:00Z', NULL,
     '2026-03-17T09:00:00Z', 'seed', TRUE);

-- ─────────────────────────────────────────────────────────────────────────────
-- EXERCISE SERIES
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO exercise_series (id, workout_id, exercise_id, created_at, created_by, active)
VALUES
    -- Workout 1 → Bench Press
    ('c1000000-0000-0000-0000-000000000001',
     'b1000000-0000-0000-0000-000000000001',
     'e1000000-0000-0000-0000-000000000001',
     '2026-03-10T09:00:00Z', 'seed', TRUE),

    -- Workout 1 → Overhead Press
    ('c1000000-0000-0000-0000-000000000002',
     'b1000000-0000-0000-0000-000000000001',
     'e1000000-0000-0000-0000-000000000004',
     '2026-03-10T09:45:00Z', 'seed', TRUE),

    -- Workout 2 → Back Squat
    ('c1000000-0000-0000-0000-000000000003',
     'b1000000-0000-0000-0000-000000000002',
     'e1000000-0000-0000-0000-000000000003',
     '2026-03-17T09:00:00Z', 'seed', TRUE);

-- ─────────────────────────────────────────────────────────────────────────────
-- SERIES
-- rest_time stored as seconds integer: 60=SIXTY, 90=NINETY, 120=ONE_TWENTY
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO series
    (id, serial_number, repetitions_to_do, repetitions_done,
     intensity, weight, start_series, end_series, rest_time,
     exercise_series_id, created_at, created_by, active)
VALUES
    -- Bench Press — 3 sets (all completed)
    ('d1000000-0000-0000-0000-000000000001',
     1, 8, 8, 7, 80.00,
     '2026-03-10T09:02:00Z', '2026-03-10T09:03:30Z', 90,
     'c1000000-0000-0000-0000-000000000001',
     '2026-03-10T09:02:00Z', 'seed', TRUE),

    ('d1000000-0000-0000-0000-000000000002',
     2, 8, 7, 8, 80.00,
     '2026-03-10T09:05:00Z', '2026-03-10T09:06:20Z', 90,
     'c1000000-0000-0000-0000-000000000001',
     '2026-03-10T09:05:00Z', 'seed', TRUE),

    ('d1000000-0000-0000-0000-000000000003',
     3, 8, 6, 9, 80.00,
     '2026-03-10T09:08:00Z', '2026-03-10T09:09:10Z', 120,
     'c1000000-0000-0000-0000-000000000001',
     '2026-03-10T09:08:00Z', 'seed', TRUE),

    -- Overhead Press — 3 sets (all completed)
    ('d1000000-0000-0000-0000-000000000004',
     1, 6, 6, 7, 50.00,
     '2026-03-10T09:47:00Z', '2026-03-10T09:48:10Z', 90,
     'c1000000-0000-0000-0000-000000000002',
     '2026-03-10T09:47:00Z', 'seed', TRUE),

    ('d1000000-0000-0000-0000-000000000005',
     2, 6, 6, 8, 50.00,
     '2026-03-10T09:50:00Z', '2026-03-10T09:51:05Z', 90,
     'c1000000-0000-0000-0000-000000000002',
     '2026-03-10T09:50:00Z', 'seed', TRUE),

    -- Back Squat — 3 sets (open workout, sets recorded but not finished)
    ('d1000000-0000-0000-0000-000000000006',
     1, 5, NULL, 8, 100.00,
     '2026-03-17T09:05:00Z', NULL, 120,
     'c1000000-0000-0000-0000-000000000003',
     '2026-03-17T09:05:00Z', 'seed', TRUE);

