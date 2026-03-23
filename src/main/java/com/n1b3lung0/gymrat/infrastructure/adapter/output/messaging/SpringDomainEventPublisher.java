package com.n1b3lung0.gymrat.infrastructure.adapter.output.messaging;

import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adapter that implements {@link DomainEventPublisherPort} by delegating to
 * Spring's {@link ApplicationEventPublisher}.
 *
 * <p>Domain events are published synchronously within the same transaction.
 * Listeners can be registered with {@code @EventListener} or
 * {@code @TransactionalEventListener}.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    }

    @Override
    public void publish(Object event) {
        Objects.requireNonNull(event, "Domain event must not be null");
        applicationEventPublisher.publishEvent(event);
    }
}

