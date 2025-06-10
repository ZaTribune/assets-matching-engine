package com.tribune.demo.ame.model;


import lombok.*;

@Builder
public record UpdateCounterpart(Long triggerId,
                                Long counterPartId,
                                double counterpartAmount,
                                double counterpartPrice) {
}
