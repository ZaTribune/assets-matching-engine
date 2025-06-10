package com.tribune.demo.ame.model;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Builder
public record Order(Long id,
                    @NotBlank
                    String asset,
                    double price,
                    @NotNull
                    double amount,
                    @NotNull
                    OrderDirection direction,
                    LocalDateTime timestamp,
                    List<Trade> trades,
                    double pendingAmount) {

    public Order {
        trades = trades != null ? trades: new ArrayList<>(); // Immutable safe copy
    }

    public void addTrade(Trade trade) {
        trades.add(trade);
    }

    public Order withAmount(double amount) {
        return new Order(id, asset, price, amount, direction, timestamp, trades, pendingAmount);
    }

    public Order withPendingAmount(double pendingAmount) {
        return new Order(id, asset, price, amount, direction, timestamp, trades, pendingAmount);
    }
    public Order withTrades(List<Trade> trades) {
        return new Order(id, asset, price, amount, direction, timestamp, trades, pendingAmount);
    }
}
