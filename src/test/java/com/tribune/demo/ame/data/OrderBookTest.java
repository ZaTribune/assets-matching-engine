package com.tribune.demo.ame.data;

import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventBusImpl;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import com.tribune.demo.ame.model.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderBookTest {

    MatchingEngine engine;
    DevBootstrap bootstrap;
    OrderBook book;

    @BeforeEach
    void setUp() {
        EventBus eventBus = new EventBusImpl();
        engine = new MatchingEngine(eventBus);
        bootstrap = new DevBootstrap(engine);
        bootstrap.init();
        book = engine.getOrderBook("BTC");
    }

    @Test
    void testDataInitialized() {
        OrderBook book = engine.getOrderBook("BTC");
        assertNotNull(book);

        assertEquals(3, book.getSellQueue().size());
        assertEquals(3, book.getBuyQueue().size());
    }

    @Test
    void addOrderToSell_whenSuccessful() {
        Order order = Order.builder()
                .asset("BTC")
                .price(10.0)
                .amount(55.0)
                .direction(OrderDirection.SELL)
                .time(LocalDateTime.now())
                .build();

        OrderResponse response = book.addOrder(order);

        assertNotNull(response);
        assertNotNull(response.getTrades());
        //two trades should be created
        assertEquals(2, response.getTrades().size());
        //No pending amount left
        assertEquals(0, response.getPendingAmount());

        //Sell Queue should have 3 orders
        assertEquals(3, book.getSellQueue().size());

        //Buy Queue should have 2 orders
        assertEquals(2, book.getBuyQueue().size());

        // one item should remain in the SELL queue
        Order last = book.getSellQueue().peek();

        assertNotNull(last);

        // We should be able to retrieve its pendingAmount from the archive
        OrderResponse lastResponse = engine.findById(last.getId());

        assertEquals(20, lastResponse.getPendingAmount());
    }

    @Test
    void addOrderToBuy_whenSuccessful() {
        Order order = Order.builder()
                .asset("BTC")
                .price(10.06)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .build();
        OrderBook book = engine.getOrderBook("BTC");

        OrderResponse response = book.addOrder(order);

        assertNotNull(response);
        assertNotNull(response.getTrades());
        assertEquals(3, response.getTrades().size());
        assertEquals(0, response.getPendingAmount());
    }

    @Test
    void insertToArchive() {
    }

    @Test
    void updateCounterpart() {
    }

    @Test
    void getAsset() {
    }

    @Test
    void getSellQueue() {
    }

    @Test
    void getBuyQueue() {
    }

    @Test
    void setAsset() {
    }
}