package com.tribune.demo.ame.model;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCounterpart {
    private Long triggerId;
    private Long counterPartId;
    private double counterpartAmount;
    private double counterpartPrice;
}
