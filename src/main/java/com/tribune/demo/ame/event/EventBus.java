package com.tribune.demo.ame.event;


public interface EventBus {
    void publish(OrderEvent event);

    void subscribe(EventType eventType, EventSubscriber subscriber);

    void unsubscribe(EventType eventType, EventSubscriber subscriber);
}
