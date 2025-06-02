package com.tribune.demo.ame.data;

import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventBusImpl;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import com.tribune.demo.ame.model.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
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

        Map<Long, OrderResponse> archive = new HashMap<>();
        archive.put(id, orderResponse);

        ReflectionTestUtils.setField(matchingEngine, "archive", archive);


        OrderResponse result = matchingEngine.findOrderById(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("BTC", result.getAsset());
        assertEquals(10.0, result.getAmount());
        assertEquals(100.0, result.getPrice());
    }

    @ParameterizedTest
    @CsvSource({
            "BUY, 1, false",
            "sell, 1, false",
            "null, 2, false",
            "test, 0, true"
    })
    void findLiveOrdersByAsset(String dir, int size, boolean error) {


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

        String direction = "null".equals(dir) ? null : dir;
        if (error) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                    matchingEngine.findAllLiveOrdersByAsset("BTC", direction));
            assertEquals("Invalid order direction: test", e.getMessage());
        } else {
            List<Order> orders = matchingEngine.findAllLiveOrdersByAsset("BTC", direction);
            assertNotNull(orders);
            assertEquals(size, orders.size());
        }
    }
}