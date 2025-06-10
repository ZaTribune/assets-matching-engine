package com.tribune.demo.ame.domain;


public interface OrderEvent {

    String getMessage();

    OrderEventType getType();

    Object getSource();
}
