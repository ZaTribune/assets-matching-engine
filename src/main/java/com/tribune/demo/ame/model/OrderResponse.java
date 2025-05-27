package com.tribune.demo.ame.model;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderResponse extends Order{

    private List<Trade> trades = new ArrayList<>();
    private double pendingAmount;


    public void addTrade(Trade trade) {
        trades.add(trade);
    }
}
