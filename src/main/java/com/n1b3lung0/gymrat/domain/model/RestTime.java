package com.n1b3lung0.gymrat.domain.model;

/**
 * Represents the rest time between series, expressed in seconds.
 */
public enum RestTime {

    THIRTY(30),
    SIXTY(60),
    NINETY(90),
    ONE_TWENTY(120),
    ONE_EIGHTY(180),
    TWO_FORTY(240),
    THREE_HUNDRED(300);

    private final int seconds;

    RestTime(int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() {
        return seconds;
    }

    /**
     * Returns the {@code RestTime} matching the given seconds value.
     *
     * @param seconds the rest duration in seconds
     * @return the matching {@code RestTime}
     * @throws IllegalArgumentException if no match is found
     */
    public static RestTime fromSeconds(int seconds) {
        for (RestTime restTime : values()) {
            if (restTime.seconds == seconds) {
                return restTime;
            }
        }
        throw new IllegalArgumentException("No RestTime found for seconds: " + seconds);
    }
}

