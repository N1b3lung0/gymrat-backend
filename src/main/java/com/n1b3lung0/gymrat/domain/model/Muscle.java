package com.n1b3lung0.gymrat.domain.model;

/**
 * Represents a muscle group targeted by an exercise.
 * An exercise has one primary muscle and zero or more secondary muscles.
 */
public enum Muscle {

    /** Pectoral muscles. */
    CHEST,

    /** Latissimus dorsi and other back muscles. */
    BACK,

    /** Deltoid muscles. */
    SHOULDERS,

    /** Biceps brachii. */
    BICEPS,

    /** Triceps brachii. */
    TRICEPS,

    /** Quadriceps femoris. */
    QUADRICEPS,

    /** Hamstring muscles. */
    HAMSTRINGS,

    /** Gluteal muscles. */
    GLUTEAL,

    /** Core stabilisers (transverse abdominis, obliques, rectus abdominis). */
    CORE,

    /** Lumbar erectors and lower back muscles. */
    LUMBAR,

    /** Gastrocnemius and soleus (calves). */
    CALFS,

    /** Hip abductor muscles. */
    ABDUCTORS,

    /** Hip adductor muscles. */
    ADDUCTORS,

    /** Forearm flexors and extensors. */
    FOREARMS,

    /** Trapezius muscle. */
    TRAPEZE
}

