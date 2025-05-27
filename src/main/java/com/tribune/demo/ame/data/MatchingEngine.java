package com.tribune.demo.ame.data;


import com.tribune.demo.ame.event.CustomSpringEvent;
import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventSubscriber;
import com.tribune.demo.ame.event.EventType;
import com.tribune.demo.ame.model.OrderResponse;
import com.tribune.demo.ame.model.Trade;
import com.tribune.demo.ame.model.UpdateCounterpart;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

///  This will hold all orderBooks for all assets

@Getter
@Slf4j
@Component
public class MatchingEngine implements  EventSubscriber {

    // on archive, initial amount is constant
    private final Map<Long, OrderResponse> archive = new ConcurrentHashMap<>();
    private final EventBus eventBus;
    @Getter
    private final AtomicLong counter = new AtomicLong(1);

    @Autowired
    public MatchingEngine(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(EventType.INSERT_ORDER,this);
        eventBus.subscribe(EventType.UPDATE_ORDER,this);
        eventBus.subscribe(EventType.UPDATE_COUNTERPART,this);

    }

    private final Map<String, OrderBook> orderBooks = new HashMap<>();

    public OrderBook getOrderBook(String name) {
        OrderBook book = orderBooks.get(name);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "OrderBook not found: " + name);
        }
        return book;
    }

    public OrderBook newOrderBook(String name) {
        OrderBook orderBook = new OrderBook(name,eventBus, counter);
        orderBooks.put(name, orderBook);
        return orderBook;
    }

    public void addOrderBook(String name, OrderBook orderBook) {
        orderBooks.put(name, orderBook);
    }

    public boolean deleteOrderBook(String name) {
        return orderBooks.remove(name) != null;
    }

    @Override
    public void onEvent(CustomSpringEvent event) {
        log.info("Received an event: {}", event.getMessage());
        if (event.getEventType().equals(EventType.INSERT_ORDER)) {
            OrderResponse o = (OrderResponse) event.getSource();
            archive.put(o.getId(), o);
        }else if (event.getEventType().equals(EventType.UPDATE_ORDER)) {
            OrderResponse o = (OrderResponse) event.getSource();
            archive.put(o.getId(), o);
        }else if (event.getEventType().equals(EventType.UPDATE_COUNTERPART)) {
            UpdateCounterpart uc = (UpdateCounterpart) event.getSource();

            if (archive.containsKey(uc.getCounterPartId())) {
                OrderResponse o = archive.get(uc.getCounterPartId());
                Trade trade = Trade.builder()
                        .orderId(uc.getTriggerId())
                        .price(uc.getCounterpartPrice())
                        .amount(uc.getCounterpartAmount())
                        .build();
                o.setPendingAmount(o.getAmount() - uc.getCounterpartAmount());
                o.addTrade(trade);
            }
        }
    }

    public OrderResponse findById(long id) {
        return archive.get(id);
    }
}
