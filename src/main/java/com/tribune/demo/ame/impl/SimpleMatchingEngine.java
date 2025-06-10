package com.tribune.demo.ame.impl;


import com.tribune.demo.ame.domain.*;
import com.tribune.demo.ame.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * This class is an implementation of {@link MatchingEngine} that uses the following:
 * <ol>
 * <li>A map of {@link SimpleOrderBook} instances, each identified by a unique asset name.</li>
 * <li>An {@link AtomicLong} counter to generate unique IDs for orders.</li>
 * <li>An archive of {@link Order} objects to keep track of processed orders.</li>
 * <li>An {@link OrderPublisher} to handle events related to order processing.</li>
 * </ol>
 */
@Slf4j
@Component
public class SimpleMatchingEngine implements MatchingEngine, OrderSubscriber {

    @Getter
    private final AtomicLong counter = new AtomicLong(0);

    private final Map<String, SimpleOrderBook> orderBooks = new HashMap<>();

    private final Map<Long, Order> archive = new ConcurrentHashMap<>();

    private final OrderPublisher orderPublisher;


    @Autowired
    public SimpleMatchingEngine(OrderPublisher orderPublisher) {
        this.orderPublisher = orderPublisher;
        orderPublisher.subscribe(OrderEventType.SAVE_OR_UPDATE_ORDER, this);
        orderPublisher.subscribe(OrderEventType.UPDATE_COUNTERPART, this);
        newOrderBook("BTC");
    }


    @Override
    public Long getNextOrderId() {
        return counter.getAndIncrement();
    }

    @Override
    public SimpleOrderBook getOrderBook(String name) {
        SimpleOrderBook book = orderBooks.get(name);
        if (book == null) {
            throw new IllegalArgumentException("OrderBook not found: " + name);
        }
        return book;
    }


    @Override
    public SimpleOrderBook newOrderBook(String name) {
        if (orderBooks.containsKey(name)) {
            return orderBooks.get(name);
        }
        SimpleOrderBook orderBook = new SimpleOrderBook(name, orderPublisher);
        orderBooks.put(name, orderBook);
        return orderBook;
    }


    @Override
    public boolean deleteOrderBook(String name) {
        return orderBooks.remove(name) != null;
    }

    @Override
    public Order findOrderById(long id) {
        return archive.get(id);
    }

    @Override
    public List<Order> findAllLiveOrdersByAsset(String name, String direction) {
        log.info("Finding live orders for asset: {}, direction: {}", name, direction);
        SimpleOrderBook orderBook = getOrderBook(name);

        if (direction == null) {
            return Stream.concat(orderBook.getBuyQueue().stream(), orderBook.getSellQueue().stream())
                    .toList();
        } else {
            OrderDirection dir;
            try {
                dir = OrderDirection.valueOf(direction.toUpperCase());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid order direction: " + direction);
            }

            return dir == OrderDirection.BUY ?
                    orderBook.getBuyQueue().stream().toList() :
                    orderBook.getSellQueue().stream().toList();
        }

    }


    @Override
    public void onEvent(OrderEvent event) {
        log.debug("Received an event: {}", event.getMessage());
        if (event.getType().equals(OrderEventType.SAVE_OR_UPDATE_ORDER)) {
            Order o = (Order) event.getSource();
            archive.put(o.id(), o);
        } else if (event.getType().equals(OrderEventType.UPDATE_COUNTERPART)) {
            UpdateCounterpart uc = (UpdateCounterpart) event.getSource();

            if (archive.containsKey(uc.counterPartId())) {
                Order o = archive.get(uc.counterPartId());
                Trade trade = Trade.builder()
                        .orderId(uc.triggerId())
                        .price(uc.counterpartPrice())
                        .amount(uc.counterpartAmount())
                        .build();
                o = o.withPendingAmount(o.amount() - uc.counterpartAmount());
                o.addTrade(trade);
                archive.put(o.id(), o);
            }
        }
    }
}
