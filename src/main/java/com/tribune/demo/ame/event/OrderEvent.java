package com.tribune.demo.ame.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
@ToString
public class OrderEvent extends ApplicationEvent {

    private String message;
    private EventType eventType;
    private Object data;

    public OrderEvent(Object source, String message, EventType eventType) {
        super(source);
        this.message = message;
        this.eventType = eventType;
    }
}
