package com.tribune.demo.ame.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
@ToString
public class CustomSpringEvent extends ApplicationEvent {

    private String message;
    private EventType eventType;
    private Object data;

    public CustomSpringEvent(Object source, String message, EventType eventType) {
        super(source);
        this.message = message;
        this.eventType = eventType;
    }
}
