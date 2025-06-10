package com.tribune.demo.ame.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank
    private String asset;

    private double price;

    @NotNull
    private Double amount;

    @NotNull
    private OrderDirection direction;
}
