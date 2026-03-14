package com.n1b3lung0.gymrat.domain.model;

/**
 * Represents the training routine type an exercise belongs to.
 * An exercise can be associated with multiple routines.
 */
public enum Routine {

    /** Pushing movements: chest, shoulders, triceps. */
    PUSH,

    /** Pulling movements: back, biceps. */
    PULL,

    /** Lower body: quadriceps, hamstrings, glutes, calves. */
    LEG,

    /** All major muscle groups in a single session. */
    FULLBODY,

    /** Upper body only: chest, back, shoulders, arms. */
    UPPERBODY,

    /** Biceps and triceps focused session. */
    ARMS,

    /** Deltoids focused session. */
    SHOULDERS
}

