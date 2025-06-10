package com.tribune.demo.ame.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
@ToString
public class OrderEvent extends ApplicationEvent {

    private String message;
    private OrderEventType type;
    private Object data;

    public OrderEvent(Object source, String message, OrderEventType type) {
        super(source);
        this.message = message;
        this.type = type;
    }
}
