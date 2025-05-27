package com.tribune.demo.ame.controller;


import com.tribune.demo.ame.data.MatchingEngine;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderRequest;
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
        OrderResponse response = matchingEngine.addOrder(order);

        return response;
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable int id) {
        log.info("Getting order - id: {}", id);

        return matchingEngine.findById(id);
    }



}
