package com.tribune.demo.ame.event;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class EventBusImpl implements EventBus {

    private final Map<EventType, List<EventSubscriber>> subscribersMap = new EnumMap<>(EventType.class);


    public EventBusImpl() {
        for (EventType event : EventType.values()) {
            subscribersMap.put(event, new ArrayList<>());
        }
    }

    @Override
    public void publish(OrderEvent event) {
        log.debug("publish - {}", event.getEventType());
        subscribersMap.get(event.getEventType())
                .forEach(subscriber -> subscriber.onEvent(event));
    }

    @Override
    public void subscribe(EventType eventType, EventSubscriber newSubscriber) {
        log.debug("subscribe - eventType: {}", eventType);
        subscribersMap.get(eventType).add(newSubscriber);
    }

    @Override
    public void unsubscribe(EventType eventType, EventSubscriber subscriber) {
        log.debug("unsubscribe - eventType: {}", eventType);
        subscribersMap.get(eventType)
                .remove(subscriber);
    }

}
