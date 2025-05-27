package com.tribune.demo.ame.controller;


import com.tribune.demo.ame.data.MatchingEngine;
import com.tribune.demo.ame.data.OrderBook;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/orders")
@RestController
public class OrderController {

    private final MatchingEngine matchingEngine;

    @PostMapping
    public OrderResponse addOrder(@RequestBody Order order) {
        log.info("Adding order - asset: {}", order.getAsset());
        OrderBook orderBook = matchingEngine.getOrderBook(order.getAsset());
        return orderBook.addOrder(order);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable int id) {
        log.info("Getting order - id: {}", id);

        return matchingEngine.findById(id);
    }



}
