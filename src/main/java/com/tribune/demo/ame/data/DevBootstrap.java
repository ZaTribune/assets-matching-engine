package com.tribune.demo.ame.data;


import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@RequiredArgsConstructor
@Profile("local")
@Component
public class DevBootstrap {


    private final MatchingEngine matchingEngine;

    @PostConstruct
    public void init() {
        OrderBook orderBook = matchingEngine.newOrderBook("BTC");

        AtomicLong counter = matchingEngine.getCounter();

        PriorityBlockingQueue<Order> sellQueue = orderBook.getSellQueue();
        PriorityBlockingQueue<Order> buyQueue = orderBook.getBuyQueue();


        LocalDateTime now = LocalDateTime.now();

        Order o1 = Order.builder()
                .asset("BTC")
                .id(counter.getAndIncrement())
                .direction(OrderDirection.SELL)
                .time(now)
                .amount(20)
                .price(10.05)
                .build();
        sellQueue.add(o1);
        orderBook.insertToArchive(o1);
        Order o2 = Order.builder()
                .asset("BTC")
                .id(counter.getAndIncrement())
                .direction(OrderDirection.SELL)
                .time(now.plusMinutes(2))
                .amount(20)
                .price(10.04)
                .build();
        sellQueue.add(o2);
        orderBook.insertToArchive(o2);
        Order o3 = Order.builder()
                .asset("BTC")
                .id(counter.getAndIncrement())
                .direction(OrderDirection.SELL)
                .time(now.plusMinutes(3))
                .amount(40)
                .price(10.05)
                .build();
        sellQueue.add(o3);
        orderBook.insertToArchive(o3);
        Order o4 = Order.builder()
                .asset("BTC")
                .id(counter.getAndIncrement())
                .direction(OrderDirection.BUY)
                .time(now.plusMinutes(5))
                .amount(20)
                .price(10.00)
                .build();
        buyQueue.add(o4);
        orderBook.insertToArchive(o4);
        Order o5 = Order.builder()
                .asset("BTC")
                .id(counter.getAndIncrement())
                .direction(OrderDirection.BUY)
                .time(now.plusMinutes(6))
                .amount(40)
                .price(10.02)
                .build();
        buyQueue.add(o5);
        orderBook.insertToArchive(o5);
        Order o6 = Order.builder()
                .asset("BTC")
                .id(counter.getAndIncrement())
                .direction(OrderDirection.BUY)
                .time(now.plusMinutes(10))
                .amount(40)
                .price(10.00)
                .build();
        buyQueue.add(o6);
        orderBook.insertToArchive(o6);

//        while (sellQueue.peek() != null) {
//            Order order = sellQueue.poll();
//            System.out.println(order);
//        }
//        System.out.println("==================");
//        while (buyQueue.peek() != null) {
//            Order order = buyQueue.poll();
//            System.out.println(order);
//        }

    }
}
