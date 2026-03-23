package com.n1b3lung0.gymrat.infrastructure.adapter.output.metrics;

import com.n1b3lung0.gymrat.application.port.output.MetricsPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adapter that implements {@link MetricsPort} using Micrometer's {@link MeterRegistry}.
 *
 * <p>The application layer never imports Micrometer directly — it only depends
 * on {@link MetricsPort}, keeping the domain and application free of framework coupling.
 */
@Component
public class MicrometerMetricsAdapter implements MetricsPort {

    private final MeterRegistry meterRegistry;

    public MicrometerMetricsAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry);
    }

    @Override
    public void increment(String name, String... tags) {
        Objects.requireNonNull(name, "Metric name must not be null");
        meterRegistry.counter(name, tags).increment();
    }
}

