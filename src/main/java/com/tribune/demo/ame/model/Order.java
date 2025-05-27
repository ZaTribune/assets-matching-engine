package com.tribune.demo.ame.model;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
public class Order {
    private Long id;
    private String asset;
    private double price;
    @NotNull
    private double amount;

    private OrderDirection direction;
    private LocalDateTime time;
}
