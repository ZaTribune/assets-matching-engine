package com.tribune.demo.ame.controller;


import com.tribune.demo.ame.domain.MatchingEngine;
import com.tribune.demo.ame.domain.OrderBook;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RequestMapping(path = "/orders")
@RestController
public record OrderController(MatchingEngine matchingEngine){


    @PostMapping
    public Order addOrder(@Valid @RequestBody OrderRequest dto) {
        log.info("Adding order - asset: {}", dto.getAsset());
        OrderBook orderBook = matchingEngine.getOrderBook(dto.getAsset());

        Order order = Order.builder()
                .id(matchingEngine.getNextOrderId())
                .asset(dto.getAsset())
                .price(dto.getPrice())
                .amount(dto.getAmount())
                .direction(dto.getDirection())
                .timestamp(LocalDateTime.now())
                .build();
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
