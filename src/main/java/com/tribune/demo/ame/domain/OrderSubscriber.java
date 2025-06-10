package com.tribune.demo.ame.domain;


public interface OrderSubscriber {

    void onEvent(OrderEvent event);
}
