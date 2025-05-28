package com.tribune.demo.ame.event;


public interface EventSubscriber {

    void onEvent(OrderEvent event);
}
