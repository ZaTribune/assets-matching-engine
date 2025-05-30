package com.tribune.demo.ame.data;

import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventBusImpl;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import com.tribune.demo.ame.model.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class MatchingEngineTest {

    MatchingEngine matchingEngine;



    @BeforeEach
    public void setUp() {

        EventBus eventBus = new EventBusImpl();
        matchingEngine = new MatchingEngine(eventBus);
    }

    @Test
    void newOrderBook() {

        String bookName = "BTC";
        OrderBook result = matchingEngine.newOrderBook(bookName);
        assertNotNull(result);
        assertEquals(bookName, result.getAsset());
    }

    @Test
    void getOrderBook() {
        String bookName = "BTC";
        OrderBook orderBook = matchingEngine.newOrderBook(bookName);
        assertNotNull(orderBook);

        OrderBook result = matchingEngine.getOrderBook(bookName);
        assertNotNull(result);
        assertEquals(bookName, result.getAsset());
    }

    @Test
    void deleteOrderBook() {
        String bookName = "LTC";
        OrderBook orderBook = matchingEngine.newOrderBook(bookName);
        assertNotNull(orderBook);

        boolean deleted = matchingEngine.deleteOrderBook(bookName);
        assertTrue(deleted);

        // Verify that the book is no longer available
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> matchingEngine.getOrderBook(bookName));
        assertEquals("OrderBook not found: LTC", e.getMessage());
    }

    @Test
    void findOrderById() {
        long id = 1L;
        OrderResponse orderResponse = OrderResponse.builder()
                .id(id)
                .asset("BTC")
                .amount(10.0)
                .price(100.0)
                .build();

        Map<Long, OrderResponse> archive = (Map<Long, OrderResponse>) ReflectionTestUtils.getField(matchingEngine,"archive");
        archive.put(id, orderResponse);

        OrderResponse result = matchingEngine.findOrderById(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("BTC", result.getAsset());
        assertEquals(10.0, result.getAmount());
        assertEquals(100.0, result.getPrice());
    }

    @Test
    void findAllLiveOrdersByAsset() {


        OrderResponse order1 = OrderResponse.builder()
                .id(1L)
                .asset("BTC")
                .amount(5.0)
                .price(300.0)
                .direction(OrderDirection.SELL)
                .build();
        OrderResponse order2 = OrderResponse.builder()
                .id(2L)
                .asset("BTC")
                .amount(3.0)
                .price(250.0)
                .direction(OrderDirection.BUY)
                .build();

        OrderBook book = matchingEngine.getOrderBook("BTC");
        book.addOrder(order1);
        book.addOrder(order2);

        List<Order> orders = matchingEngine.findAllLiveOrdersByAsset("BTC");
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }
}