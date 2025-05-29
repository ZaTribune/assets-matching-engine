package com.tribune.demo.ame.data;

import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventBusImpl;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import com.tribune.demo.ame.model.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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
    void addSellOrder_whenSuccessful() {
        Order order = Order.builder()
                .asset("BTC")
                .price(10.0)
                .amount(55.0)
                .direction(OrderDirection.SELL)
                .timestamp(LocalDateTime.now())
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

        Order last = book.getBuyQueue().peek();

        assertNotNull(last);

        // We should be able to retrieve its pendingAmount from the archive
        OrderResponse lastResponse = engine.findById(last.getId());

        // only 5 should be left eventually
        assertEquals(5, lastResponse.getPendingAmount());
    }

    @Test
    void addSellOrder_whenBuyQueueIsEmpty() {

        //clear the buy queue
        book = engine.getOrderBook("BTC");
        book.getBuyQueue().clear();

        Order order = Order.builder()
                .asset("BTC")
                .price(10.0)
                .amount(55.0)
                .direction(OrderDirection.SELL)
                .timestamp(LocalDateTime.now())
                .build();

        OrderResponse response = book.addOrder(order);

        // same order returned on response
        assertEquals(order.getAmount(), response.getAmount());
        assertEquals(order.getPrice(), response.getPrice());
        assertEquals(4, book.getSellQueue().size());
    }

    @Test
    void addSellOrder_whenNoSuitablePrice() {

        //clear the buy queue
        book = engine.getOrderBook("BTC");
        Order order = Order.builder()
                .asset("BTC")
                .price(15)
                .amount(55.0)
                .direction(OrderDirection.SELL)
                .timestamp(LocalDateTime.now())
                .build();

        OrderResponse response = book.addOrder(order);

        // same order returned on response
        assertEquals(order.getAmount(), response.getAmount());
        assertEquals(order.getPrice(), response.getPrice());
        assertEquals(4, book.getSellQueue().size());
    }

    @Test
    void addBuyOrder_whenSuccessful() {
        Order order = Order.builder()
                .asset("BTC")
                .price(10.06)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .timestamp(LocalDateTime.now())
                .build();

        OrderResponse response = book.addOrder(order);

        assertNotNull(response);
        assertNotNull(response.getTrades());
        //3 trades should be created
        assertEquals(3, response.getTrades().size());
        //No pending amount left
        assertEquals(0, response.getPendingAmount());

        //Sell Queue should have 1 orders
        assertEquals(1, book.getSellQueue().size());

        //Buy Queue should have 2 orders
        assertEquals(3, book.getBuyQueue().size());


        Order last = book.getSellQueue().peek();

        assertNotNull(last);

        // We should be able to retrieve its pendingAmount from the archive
        OrderResponse lastResponse = engine.findById(last.getId());

        // 25 should be left eventually
        assertEquals(25, lastResponse.getPendingAmount());
    }

    @Test
    void addBuyOrder_whenSellQueueIsEmpty() {

        //clear the buy queue
        book = engine.getOrderBook("BTC");
        book.getSellQueue().clear();

        Order order = Order.builder()
                .asset("BTC")
                .price(10.0)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .timestamp(LocalDateTime.now())
                .build();

        OrderResponse response = book.addOrder(order);

        // same order returned on response
        assertEquals(order.getAmount(), response.getAmount());
        assertEquals(order.getPrice(), response.getPrice());
        assertEquals(4, book.getBuyQueue().size());
    }

    @Test
    void addBuyOrder_whenNoSuitablePrice() {

        //clear the buy queue
        book = engine.getOrderBook("BTC");
        Order order = Order.builder()
                .asset("BTC")
                .price(5)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .timestamp(LocalDateTime.now())
                .build();

        OrderResponse response = book.addOrder(order);

        // same order returned on response
        assertEquals(order.getAmount(), response.getAmount());
        assertEquals(order.getPrice(), response.getPrice());
        assertEquals(4, book.getBuyQueue().size());
    }


    @Test
    void addOrder_whenAssetIsInvalid(){
        Order order = Order.builder()
                .asset("fdfd")
                .price(10.06)
                .amount(55.0)
                .direction(OrderDirection.BUY)
                .build();
        OrderBook book = engine.getOrderBook("BTC");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,()->book.addOrder(order)) ;
        assertEquals("This asset doesn't belong to this order book.", e.getMessage());
    }
}