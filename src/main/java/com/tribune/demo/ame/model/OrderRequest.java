package com.tribune.demo.ame.model;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String asset;
    private double price;
    private double amount;
    private OrderDirection direction;
}
