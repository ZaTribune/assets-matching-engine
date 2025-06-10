package com.tribune.demo.ame.data;

import com.tribune.demo.ame.domain.MatchingEngine;
import com.tribune.demo.ame.domain.OrderPublisher;
import com.tribune.demo.ame.impl.SimpleOrderPublisher;
import com.tribune.demo.ame.impl.SimpleMatchingEngine;
import com.tribune.demo.ame.impl.SimpleOrderBook;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SimpleOrderBookTest {

    MatchingEngine engine;
    DevBootstrap bootstrap;
    SimpleOrderBook book;

    @BeforeEach
    void setUp() {
        OrderPublisher eventBus = new SimpleOrderPublisher();
        engine = new SimpleMatchingEngine(eventBus);
        bootstrap = new DevBootstrap(engine);
        bootstrap.init();
        book = (SimpleOrderBook) engine.getOrderBook("BTC");
    }

    @Test
    void testDataInitialized() {
        SimpleOrderBook book = (SimpleOrderBook) engine.getOrderBook("BTC");
        assertNotNull(book);

        assertEquals(3, book.getSellQueue().size());
        assertEquals(3, book.getBuyQueue().size());
    }

    @Test
    void addSellOrder_whenSuccessful() {
        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(10.0)
                .amount(55.0)
                .direction(OrderDirection.SELL)
                .timestamp(LocalDateTime.now())
                .build();

        Order response = book.submit(order);

        assertNotNull(response);
        assertNotNull(response.trades());
        //two trades should be created
        assertEquals(2, response.trades().size());
        //No pending amount left
        assertEquals(0, response.pendingAmount());

        //Sell Queue should have 3 orders
        assertEquals(3, book.getSellQueue().size());

        //Buy Queue should have 2 orders
        assertEquals(2, book.getBuyQueue().size());

        Order last = book.getBuyQueue().peek();

        assertNotNull(last);

        // We should be able to retrieve its pendingAmount from the archive
        Order lastResponse = engine.findOrderById(last.id());

        // only 5 should be left eventually
        assertEquals(5, lastResponse.pendingAmount());
    }

    @Test
    void addSellOrder_whenBuyQueueIsEmpty() {

        //clear the buy queue

        book.getBuyQueue().clear();

        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(10.0)
                .amount(55.0)
                .direction(OrderDirection.SELL)
                .timestamp(LocalDateTime.now())
                .build();

        Order response = book.submit(order);

        // same order returned on response
        assertEquals(order.amount(), response.amount());
        assertEquals(order.price(), response.price());
        assertEquals(4, book.getSellQueue().size());
    }

    @Test
    void addSellOrder_whenNoSuitablePrice() {

        //clear the buy queue
        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(15)
                .amount(55.0)
                .direction(OrderDirection.SELL)
                .timestamp(LocalDateTime.now())
                .build();

        Order response = book.submit(order);

        // same order returned on response
        assertEquals(order.amount(), response.amount());
        assertEquals(order.price(), response.price());
        assertEquals(4, book.getSellQueue().size());
    }

    @Test
    void addBuyOrder_whenSuccessful() {
        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(10.06)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .timestamp(LocalDateTime.now())
                .build();

        Order response = book.submit(order);

        assertNotNull(response);
        assertNotNull(response.trades());
        //3 trades should be created
        assertEquals(3, response.trades().size());
        //No pending amount left
        assertEquals(0, response.pendingAmount());

        //Sell Queue should have 1 orders
        assertEquals(1, book.getSellQueue().size());

        //Buy Queue should have 2 orders
        assertEquals(3, book.getBuyQueue().size());


        Order last = book.getSellQueue().peek();

        assertNotNull(last);

        // We should be able to retrieve its pendingAmount from the archive
        Order lastResponse = engine.findOrderById(last.id());

        // 25 should be left eventually
        assertEquals(25, lastResponse.pendingAmount());
    }

    @Test
    void addBuyOrder_whenSellQueueIsEmpty() {

        //clear the buy queue

        book.getSellQueue().clear();

        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(10.0)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .timestamp(LocalDateTime.now())
                .build();

        Order response = book.submit(order);

        // same order returned on response
        assertEquals(order.amount(), response.amount());
        assertEquals(order.price(), response.price());
        assertEquals(4, book.getBuyQueue().size());
    }

    @Test
    void addBuyOrder_whenNoSuitablePrice() {

        //clear the buy queue

        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(5)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .timestamp(LocalDateTime.now())
                .build();

        Order response = book.submit(order);

        // same order returned on response
        assertEquals(order.amount(), response.amount());
        assertEquals(order.price(), response.price());
        assertEquals(4, book.getBuyQueue().size());
    }


    @Test
    void submit_whenAssetIsInvalid(){
        Order order = Order.builder()
                .asset("fdfd")
                .price(10.06)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .build();
        SimpleOrderBook book = (SimpleOrderBook) engine.getOrderBook("BTC");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,()->book.submit(order)) ;
        assertEquals("This asset doesn't belong to this order book.", e.getMessage());
    }
}