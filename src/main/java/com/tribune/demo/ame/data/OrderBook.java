package com.tribune.demo.ame.data;


import com.tribune.demo.ame.event.CustomSpringEvent;
import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventType;
import com.tribune.demo.ame.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class OrderBook {

    private final EventBus eventBus;
    private final AtomicLong counter;

    @Getter
    @Setter
    public String asset;

    public OrderBook(String name, EventBus eventBus, AtomicLong counter) {
        this.asset = name;
        this.eventBus = eventBus;
        this.counter = counter;
    }


    // I need the least selling prices to come first
    private final Comparator<Order> comparator1 = Comparator.comparingDouble(Order::getPrice)//asc
            .thenComparing(Order::getTime);//asc

    // I need the highest buying prices to come first
    private final Comparator<Order> comparator2 = Comparator.comparingDouble(Order::getPrice).reversed()//desc
            .thenComparing(Order::getTime).reversed();//desc

    // I used PriorityQueue with synchronized methods but then searched the web and found this one to be a better alternative
    @Getter
    private final PriorityBlockingQueue<Order> sellQueue = new PriorityBlockingQueue<>(1, comparator1);

    @Getter
    private final PriorityBlockingQueue<Order> buyQueue = new PriorityBlockingQueue<>(1, comparator2);


    private OrderResponse processSell(Order order) {
        log.info("Adding order to SELL queue");
        double pendingAmount = order.getAmount();
        List<Trade> trades = new ArrayList<>();
        if (!buyQueue.isEmpty()) {
            Order nextBuy = buyQueue.peek();
            if (nextBuy.getPrice() < order.getPrice()) {
                sellQueue.add(order);
            } else {
                while (nextBuy.getPrice() >= order.getPrice() && pendingAmount > 0) {

                    nextBuy = buyQueue.poll();
                    assert nextBuy != null;//already peaked - false positive
                    double tradeAmount = nextBuy.getAmount();

                    if (pendingAmount < tradeAmount) {
                        tradeAmount = pendingAmount;
                        nextBuy.setAmount(nextBuy.getAmount() - tradeAmount);
                        buyQueue.add(order);// keep it case it's larger
                    }
                    Trade currentTrade = Trade.builder()
                            .orderId(nextBuy.getId())
                            .price(nextBuy.getPrice())
                            .amount(tradeAmount)
                            .build();
                    updateCounterpart(order.getId(), nextBuy.getId(), tradeAmount, order.getPrice());

                    trades.add(currentTrade);

                    pendingAmount -= tradeAmount;
                }
            }
        } else {
            buyQueue.add(order);
        }
        return createOrderResponse(order, trades, pendingAmount);
    }

    private OrderResponse processBuy(Order order) {
        log.info("Adding order to BUY queue");
        double pendingAmount = order.getAmount();
        List<Trade> trades = new ArrayList<>();
        if (!sellQueue.isEmpty()) {
            Order nextSell = sellQueue.peek();
            if (nextSell.getPrice() > order.getPrice()) {
                buyQueue.add(order);
            } else {
                while (nextSell.getPrice() <= order.getPrice() && pendingAmount > 0) {

                    nextSell = sellQueue.poll();
                    assert nextSell != null;//already peaked - false positive
                    double tradeAmount = nextSell.getAmount();

                    if (pendingAmount < tradeAmount) {
                        tradeAmount = pendingAmount;
                        nextSell.setAmount(nextSell.getAmount() - tradeAmount);
                        sellQueue.add(order);// keep it case it's larger
                    }
                    Trade currentTrade = Trade.builder()
                            .orderId(nextSell.getId())
                            .price(nextSell.getPrice())
                            .amount(tradeAmount)
                            .build();
                    updateCounterpart(order.getId(), nextSell.getId(), tradeAmount, order.getPrice());

                    trades.add(currentTrade);

                    pendingAmount -= tradeAmount;
                }
            }
        } else {
            buyQueue.add(order);
        }
        return createOrderResponse(order, trades, pendingAmount);
    }

    private OrderResponse createOrderResponse(Order order, List<Trade> trades, double pendingAmount) {
        return OrderResponse.builder()
                .id(order.getId())
                .price(order.getPrice())
                .asset(order.getAsset())
                .direction(order.getDirection())
                .time(LocalDateTime.now())
                .amount(order.getAmount())
                .pendingAmount(pendingAmount)
                .trades(trades)
                .build();
    }

    public OrderResponse addOrder(Order order) {
        order.setId(counter.getAndIncrement());
        OrderResponse response;
        if (order.getDirection() == OrderDirection.SELL) {
            response = processSell(order);
        } else {
            response = processBuy(order);
        }
        updateOrder(response);
        System.out.println(sellQueue);
        return response;
    }

    private void updateOrder(OrderResponse response) {
        CustomSpringEvent event = new CustomSpringEvent(response,
                "Update the order",
                EventType.UPDATE_ORDER);
        event.setData(response.getId());

        eventBus.publish(event);
    }

    public void insertToArchive(Order order) {

        OrderResponse o = OrderResponse.builder()
                .id(order.getId())
                .price(order.getPrice())
                .asset(order.getAsset())
                .direction(order.getDirection())
                .time(LocalDateTime.now())
                .amount(order.getAmount())
                .trades(new ArrayList<>())
                .pendingAmount(order.getAmount())
                .build();
        CustomSpringEvent event = new CustomSpringEvent(o,
                "Insert the order",
                EventType.INSERT_ORDER);
        event.setData(o.getId());

        eventBus.publish(event);
    }


    public void updateCounterpart(Long triggerId, Long counterPartId, double counterpartAmount, double counterpartPrice) {
        log.info("Updating archive for order {}", counterPartId);
        UpdateCounterpart updateCounterpart = UpdateCounterpart.builder()
                .triggerId(triggerId)
                .counterPartId(counterPartId)
                .counterpartAmount(counterpartAmount)
                .counterpartPrice(counterpartPrice)
                .build();
        CustomSpringEvent event = new CustomSpringEvent(updateCounterpart,
                "Update the counterpart",
                EventType.UPDATE_COUNTERPART);
        event.setData(triggerId);

        eventBus.publish(event);
    }

}
