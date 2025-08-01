package com.tribune.demo.ame.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tribune.demo.ame.impl.SimpleMatchingEngine;
import com.tribune.demo.ame.impl.SimpleOrderBook;
import com.tribune.demo.ame.impl.SimpleOrderPublisher;
import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderDirection;
import com.tribune.demo.ame.model.OrderRequest;
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
    SimpleMatchingEngine matchingEngine;

    @MockitoBean
    SimpleOrderPublisher eventBus;



    ObjectMapper objectMapper = new ObjectMapper();

    SimpleOrderBook orderBook;

    @BeforeEach
    void init(){
         orderBook = new SimpleOrderBook("BTC", eventBus);
    }


    @Test
    void addOrder_whenSuccessful() throws Exception {
        Order order = Order.builder()
                .asset("BTC")
                .price(10.0)
                .amount(5.0)
                .direction(OrderDirection.SELL)
                .build();

        when(matchingEngine.getOrderBook(anyString())).thenReturn(orderBook);


        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk());
    }

    @Test
    void addOrder_whenOrderBookNotFound() throws Exception {
        Order order = Order.builder()
                .asset("BTC")
                .price(10.0)
                .amount(5.0)
                .direction(OrderDirection.SELL)
                .build();

        when(matchingEngine.getOrderBook(any())).thenThrow(new IllegalArgumentException("whatever"));


        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("whatever"));
    }

    @Test
    void addOrder_whenInvalidInput() throws Exception {
        OrderRequest order = OrderRequest.builder()
                .price(10.0)
                .amount(5.0)
                .direction(OrderDirection.SELL)
                .build();


        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.reason[0]").value("asset : must not be blank"));
    }

    @Test
    void getOrder_whenSuccessful() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(10.0)
                .amount(5.0)
                .direction(OrderDirection.SELL)
                .build();

        when(matchingEngine.findOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getOrder_whenOrderNotFound() throws Exception {

        when(matchingEngine.findOrderById(1L)).thenThrow(new IllegalArgumentException("whatever"));

        mockMvc.perform(get("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("whatever"));
    }

    @Test
    void getLiveOrdersByAssetName_whenSuccessful() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(10.0)
                .amount(5.0)
                .direction(OrderDirection.SELL)
                .build();

        when(matchingEngine.findAllLiveOrdersByAsset("BTC", "SELL")).thenReturn(List.of(order));

        mockMvc.perform(get("/orders/live?asset=BTC&direction=SELL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getLiveOrdersByAssetName_whenDirectionNotSpecified() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .asset("BTC")
                .price(10.0)
                .amount(5.0)
                .direction(OrderDirection.SELL)
                .build();

        when(matchingEngine.findAllLiveOrdersByAsset("BTC", null)).thenReturn(List.of(order));

        mockMvc.perform(get("/orders/live?asset=BTC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}