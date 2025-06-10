package com.tribune.demo.ame.impl;

import com.tribune.demo.ame.domain.OrderEvent;
import com.tribune.demo.ame.domain.OrderEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
@ToString
public class SimpleOrderEvent extends ApplicationEvent implements OrderEvent {

    private String message;
    private OrderEventType type;
    private Object source;

    public SimpleOrderEvent(Object source, String message, OrderEventType type) {
        super(source);
        this.message = message;
        this.type = type;
        this.source = source;
    }
}
