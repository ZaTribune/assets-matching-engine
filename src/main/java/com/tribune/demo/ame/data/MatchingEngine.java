package com.tribune.demo.ame.data;


import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventSubscriber;
import com.tribune.demo.ame.event.EventType;
import com.tribune.demo.ame.event.OrderEvent;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderResponse;
import com.tribune.demo.ame.model.Trade;
import com.tribune.demo.ame.model.UpdateCounterpart;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

///  This will hold all orderBooks for all assets
@Slf4j
@Component
public class MatchingEngine implements EventSubscriber {

    @Getter
    private final AtomicLong counter = new AtomicLong(0);

    private final Map<String, OrderBook> orderBooks = new HashMap<>();

    private final Map<Long, OrderResponse> archive = new ConcurrentHashMap<>();

    private final EventBus eventBus;

    @Autowired
    public MatchingEngine(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(EventType.SAVE_OR_UPDATE_ORDER, this);
        eventBus.subscribe(EventType.UPDATE_COUNTERPART, this);
        newOrderBook("BTC");
    }



    /**
     * Retrieves an OrderBook by its name.
     * If the OrderBook does not exist, it throws an IllegalArgumentException.
     *
     * @param name The name of the asset for which to retrieve the OrderBook.
     * @return The OrderBook associated with the specified asset name.
     */
    public OrderBook getOrderBook(String name) {
        OrderBook book = orderBooks.get(name);
        if (book == null) {
            throw new IllegalArgumentException("OrderBook not found: " + name);
        }
        return book;
    }
    /**
     * Creates a new OrderBook for the specified asset name.
     * If an OrderBook with the same name already exists, it returns the existing one.
     *
     * @param name The name of the asset for which to create an OrderBook.
     * @return The created or existing OrderBook.
     */
    OrderBook newOrderBook(String name) {
        if (orderBooks.containsKey(name)) {
            return orderBooks.get(name);
        }
        OrderBook orderBook = new OrderBook(name, eventBus, counter);
        orderBooks.put(name, orderBook);
        return orderBook;
    }

    /**
     * Deletes an OrderBook by its name.
     * If the OrderBook does not exist, it returns false.
     *
     * @param name The name of the asset for which to delete the OrderBook.
     * @return true if the OrderBook was successfully deleted, false otherwise.
     */
    public boolean deleteOrderBook(String name) {
        return orderBooks.remove(name) != null;
    }

    @Override
    public void onEvent(OrderEvent event) {
        log.debug("Received an event: {}", event.getMessage());
        if (event.getEventType().equals(EventType.SAVE_OR_UPDATE_ORDER)) {
            OrderResponse o = (OrderResponse) event.getSource();
            archive.put(o.getId(), o);
        } else if (event.getEventType().equals(EventType.UPDATE_COUNTERPART)) {
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

    /**
     * Finds an order by ID.
     *
     * @param id The ID of the order to find.
     * @return The Order related to the provided ID.
     */
    public OrderResponse findOrderById(long id) {
        return archive.get(id);
    }

    /**
     * Finds all live orders by a given asset.
     *
     * @param name The name of the asset for which to find live orders.
     * @return A list of all live orders for a specified asset name.
     */
    public List<Order> findAllLiveOrdersByAsset(String name) {
        OrderBook orderBook = getOrderBook(name);

        return Stream.concat(orderBook.getBuyQueue().stream(), orderBook.getSellQueue().stream())
                .toList();
    }
}
