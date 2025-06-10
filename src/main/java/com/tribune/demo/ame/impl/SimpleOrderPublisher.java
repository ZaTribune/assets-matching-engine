package com.tribune.demo.ame.impl;


import com.tribune.demo.ame.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class SimpleOrderPublisher implements OrderPublisher {

    private final Map<OrderEventType, List<OrderSubscriber>> subscribersMap = new EnumMap<>(OrderEventType.class);


    public SimpleOrderPublisher() {
        for (OrderEventType event : OrderEventType.values()) {
            subscribersMap.put(event, new ArrayList<>());
        }
    }

    @Override
    public void publish(OrderEvent event) {
        log.debug("publish - {}", event.getType());
        subscribersMap.get(event.getType())
                .forEach(subscriber -> subscriber.onEvent(event));
    }

    @Override
    public void subscribe(OrderEventType orderEventType, OrderSubscriber newSubscriber) {
        log.debug("subscribe - eventType: {}", orderEventType);
        subscribersMap.get(orderEventType).add(newSubscriber);
    }

    @Override
    public void unsubscribe(OrderEventType orderEventType, OrderSubscriber subscriber) {
        log.debug("unsubscribe - eventType: {}", orderEventType);
        subscribersMap.get(orderEventType)
                .remove(subscriber);
    }

}
