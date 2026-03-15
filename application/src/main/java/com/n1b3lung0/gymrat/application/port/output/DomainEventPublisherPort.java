package com.n1b3lung0.gymrat.application.port.output;

/**
 * Output port for publishing domain events.
 *
 * <p>Implemented in the infrastructure layer (e.g. Spring Application Events,
 * Kafka producer, or Outbox pattern). The application layer never depends on
 * the concrete mechanism — only on this contract.
 *
 * <p>Must always be called <strong>after</strong> the aggregate has been
 * persisted within the same transaction boundary, following the pattern:
 * <pre>
 *   repository.save(aggregate);
 *   aggregate.pullDomainEvents().forEach(eventPublisher::publish);
 * </pre>
 */
public interface DomainEventPublisherPort {

    /**
     * Publishes a single domain event.
     *
     * @param event the domain event to publish; never {@code null}
     */
    void publish(Object event);
}

