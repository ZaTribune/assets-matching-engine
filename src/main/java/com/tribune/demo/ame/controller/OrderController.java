package com.tribune.demo.ame.controller;


import com.tribune.demo.ame.data.MatchingEngine;
import com.tribune.demo.ame.data.OrderBook;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Queue;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/orders")
@RestController
public class OrderController {

    private final MatchingEngine matchingEngine;

    @PostMapping
    public OrderResponse addOrder(@Valid @RequestBody Order order) {
        log.info("Adding order - asset: {}", order.getAsset());
        OrderBook orderBook = matchingEngine.getOrderBook(order.getAsset());
        return orderBook.addOrder(order);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable int id) {
        log.info("Getting order - id: {}", id);

        return matchingEngine.findById(id);
    }

    @GetMapping("/{direction}/{asset}")
    public Queue<Order> getBook(@PathVariable String direction, @PathVariable String asset) {
        log.info("Getting book for {}ing asset - id: {}", direction, asset);

        OrderBook book = matchingEngine.getOrderBook(asset);


        if ("buy".equals(direction)) {
            return book.getBuyQueue();
        } else if ("sell".equals(direction)) {
            return book.getSellQueue();
        } else {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }


}
