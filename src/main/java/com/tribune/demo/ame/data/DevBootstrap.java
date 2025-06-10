package com.tribune.demo.ame.data;



import com.tribune.demo.ame.domain.MatchingEngine;
import com.tribune.demo.ame.impl.SimpleOrderBook;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import com.tribune.demo.ame.model.OrderResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Queue;


@Slf4j
@RequiredArgsConstructor
@Profile("local")
@Component
public class DevBootstrap {


    private final MatchingEngine matchingEngine;

    @PostConstruct
    public void init() {
        log.info("Running on local profile, initializing data...");
        SimpleOrderBook orderBook = (SimpleOrderBook) matchingEngine.getOrderBook("BTC");

        Queue<Order> sellQueue = orderBook.getSellQueue();
        Queue<Order> buyQueue = orderBook.getBuyQueue();


        LocalDateTime now = LocalDateTime.now();

        Order o1 = Order.builder()
                .asset("BTC")
                .id(matchingEngine.getNextOrderId())
                .direction(OrderDirection.SELL)
                .timestamp(now)
                .amount(20)
                .price(10.05)
                .build();
        sellQueue.add(o1);
        OrderResponse response = orderBook.createOrderResponse(o1,new ArrayList<>(),0);
        orderBook.saveOrUpdateOrder(response);
        Order o2 = Order.builder()
                .asset("BTC")
                .id(matchingEngine.getNextOrderId())
                .direction(OrderDirection.SELL)
                .timestamp(now.plusMinutes(2))
                .amount(20)
                .price(10.04)
                .build();
        sellQueue.add(o2);
        response = orderBook.createOrderResponse(o2,new ArrayList<>(),0);
        orderBook.saveOrUpdateOrder(response);
        Order o3 = Order.builder()
                .asset("BTC")
                .id(matchingEngine.getNextOrderId())
                .direction(OrderDirection.SELL)
                .timestamp(now.plusMinutes(3))
                .amount(40)
                .price(10.05)
                .build();
        sellQueue.add(o3);
        response = orderBook.createOrderResponse(o3,new ArrayList<>(),0);
        orderBook.saveOrUpdateOrder(response);
        Order o4 = Order.builder()
                .asset("BTC")
                .id(matchingEngine.getNextOrderId())
                .direction(OrderDirection.BUY)
                .timestamp(now.plusMinutes(5))
                .amount(20)
                .price(10.00)
                .build();
        buyQueue.add(o4);
        response = orderBook.createOrderResponse(o4,new ArrayList<>(),0);
        orderBook.saveOrUpdateOrder(response);
        Order o5 = Order.builder()
                .asset("BTC")
                .id(matchingEngine.getNextOrderId())
                .direction(OrderDirection.BUY)
                .timestamp(now.plusMinutes(6))
                .amount(40)
                .price(10.02)
                .build();
        buyQueue.add(o5);
        response = orderBook.createOrderResponse(o5,new ArrayList<>(),0);
        orderBook.saveOrUpdateOrder(response);
        Order o6 = Order.builder()
                .asset("BTC")
                .id(matchingEngine.getNextOrderId())
                .direction(OrderDirection.BUY)
                .timestamp(now.plusMinutes(10))
                .amount(40)
                .price(10.00)
                .build();
        buyQueue.add(o6);
        response = orderBook.createOrderResponse(o6,new ArrayList<>(),0);
        orderBook.saveOrUpdateOrder(response);
    }
}
