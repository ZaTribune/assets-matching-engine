package com.tribune.demo.ame.data;


import com.tribune.demo.ame.event.EventBus;
import com.tribune.demo.ame.event.EventType;
import com.tribune.demo.ame.event.OrderEvent;
import com.tribune.demo.ame.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
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

    /***
     * Adds an order to the order book.
     * If the order is a SELL order, it will be processed against the BUY queue.
     * If the order is a BUY order, it will be processed against the SELL queue.
     * The method will create an OrderResponse containing the order details, trades made, and any pending amount.
     * @param order The order to be added.
     * */
    public OrderResponse addOrder(Order order) {
        order.setId(counter.getAndIncrement());

        OrderResponse response = OrderDirection.SELL.equals(order.getDirection())?
                processSell(order) :
                processBuy(order);

        updateOrder(response);
        return response;
    }


    private OrderResponse processSell(Order order) {
        log.info("Adding order to SELL queue");
        double pendingAmount = order.getAmount();
        List<Trade> trades = new ArrayList<>();
        if (!buyQueue.isEmpty()) {
            Order nextBuy = buyQueue.peek();
            if (nextBuy.getPrice() < order.getPrice()) {
                log.info("No suitable BUY orders found, adding to SELL queue");
                sellQueue.add(order);
            } else {
                pendingAmount = getPendingAmount(order, trades, nextBuy.getPrice() >= order.getPrice(), buyQueue);
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
                log.info("No suitable SELL orders found, adding to BUY queue");
                buyQueue.add(order);
            } else {
                pendingAmount = getPendingAmount(order, trades, nextSell.getPrice() <= order.getPrice(), sellQueue);
            }
        } else {
            buyQueue.add(order);
        }
        return createOrderResponse(order, trades, pendingAmount);
    }

    private double getPendingAmount(Order order, List<Trade> trades, boolean condition, Queue<Order> otherQueue) {
        double pendingAmount = order.getAmount();
        while (condition && pendingAmount > 0) {

            Order nextSell = otherQueue.poll();
            assert nextSell != null;//already peaked - false positive
            double tradeAmount = nextSell.getAmount();

            if (pendingAmount < tradeAmount) {
                tradeAmount = pendingAmount;
                nextSell.setAmount(nextSell.getAmount() - tradeAmount);
                otherQueue.add(nextSell);// keep it case it's larger
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
        return pendingAmount;
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



    private void updateOrder(OrderResponse response) {
        OrderEvent event = new OrderEvent(response,
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
        OrderEvent event = new OrderEvent(o,
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
        OrderEvent event = new OrderEvent(updateCounterpart,
                "Update the counterpart",
                EventType.UPDATE_COUNTERPART);
        event.setData(triggerId);

        eventBus.publish(event);
    }

}
