package com.n1b3lung0.gymrat.application.port.output;

/**
 * Output port for recording application metrics.
 *
 * <p>Implemented in the infrastructure layer with Micrometer. The application
 * layer never depends on Micrometer directly — only on this contract.
 */
public interface MetricsPort {

    /**
     * Increments a named counter by one.
     *
     * @param name the metric name (e.g. {@code "exercises.created.total"})
     * @param tags alternating key-value pairs for metric tags; must be even-length
     */
    void increment(String name, String... tags);
}

