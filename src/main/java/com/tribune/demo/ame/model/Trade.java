package com.tribune.demo.ame.model;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {
    private Long orderId;
    private double amount;
    private double price;
}
