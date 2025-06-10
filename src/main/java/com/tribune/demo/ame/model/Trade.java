package com.tribune.demo.ame.model;


import lombok.*;


@Builder
public record Trade(Long orderId, double amount, double price) {}
