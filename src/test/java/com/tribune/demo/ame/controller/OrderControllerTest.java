package com.tribune.demo.ame.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tribune.demo.ame.data.MatchingEngine;
import com.tribune.demo.ame.data.OrderBook;
import com.tribune.demo.ame.event.EventBusImpl;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import com.tribune.demo.ame.model.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchingEngine matchingEngine;

    @MockitoBean
    EventBusImpl eventBus;



    ObjectMapper objectMapper = new ObjectMapper();

    OrderBook orderBook;

    @BeforeEach
    void init(){
         orderBook = new OrderBook("BTC", eventBus, new AtomicLong());
    }


    @Test
    void addOrder_whenSuccessful() throws Exception {
        Order order = new Order();
        order.setAsset("BTC");
        order.setPrice(10.0);
        order.setAmount(5.0);
        order.setDirection(OrderDirection.SELL);

        when(matchingEngine.getOrderBook(anyString())).thenReturn(orderBook);


        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk());
    }

    @Test
    void addOrder_whenOrderBookNotFound() throws Exception {
        Order order = new Order();
        order.setAsset("BTC");
        order.setPrice(10.0);
        order.setAmount(5.0);
        order.setDirection(OrderDirection.SELL);

        when(matchingEngine.getOrderBook(any())).thenThrow(new IllegalArgumentException("whatever"));


        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("whatever"));;
    }

    @Test
    void addOrder_whenInvalidInput() throws Exception {
        Order order = new Order();
        order.setPrice(10.0);
        order.setAmount(5.0);
        order.setDirection(OrderDirection.SELL);


        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.reason[0]").value("asset : must not be blank"));
    }

    @Test
    void getOrder_whenSuccessful() throws Exception {
        OrderResponse order = new OrderResponse();
        order.setId(1L);
        order.setPrice(10.0);
        order.setAmount(5.0);
        order.setDirection(OrderDirection.SELL);

        when(matchingEngine.findOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getOrder_whenOrderNotFound() throws Exception {
        OrderResponse order = new OrderResponse();
        order.setId(1L);
        order.setPrice(10.0);
        order.setAmount(5.0);
        order.setDirection(OrderDirection.SELL);

        when(matchingEngine.findOrderById(1L)).thenThrow(new IllegalArgumentException("whatever"));

        mockMvc.perform(get("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("whatever"));;
    }

    @Test
    void getLiveOrdersByAssetName_whenSuccessful() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setPrice(10.0);
        order.setAmount(5.0);
        order.setDirection(OrderDirection.SELL);

        when(matchingEngine.findAllLiveOrdersByAsset("BTC")).thenReturn(List.of(order));

        mockMvc.perform(get("/orders/live/asset/BTC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}