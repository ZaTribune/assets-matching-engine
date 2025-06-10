package com.tribune.demo.ame.controller;


import com.tribune.demo.ame.domain.MatchingEngine;
import com.tribune.demo.ame.domain.OrderBook;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
        order.setId(matchingEngine.getNextOrderId());
        return orderBook.submit(order);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable int id) {
        log.info("Getting order - id: {}", id);

        return matchingEngine.findOrderById(id);
    }

    @GetMapping("/live")
    public List<Order> getLiveOrdersByAsset(@RequestParam(name = "asset") String name, @RequestParam(name = "direction", required = false) String direction) {
        log.info("Getting order by asset - name: {}", name);

        return matchingEngine.findAllLiveOrdersByAsset(name, direction);
    }

}
