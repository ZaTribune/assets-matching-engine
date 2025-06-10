package com.tribune.demo.ame.domain;


/**
 * An interface for publishing and subscribing to events in the matching engine.
 * This interface allows components to communicate through events, enabling a decoupled architecture.
 * Can be implemented using various event-driven frameworks or custom implementations.
 */
public interface OrderPublisher {
    void publish(OrderEvent event);

    void subscribe(OrderEventType orderEventType, OrderSubscriber subscriber);

    void unsubscribe(OrderEventType orderEventType, OrderSubscriber subscriber);
}
