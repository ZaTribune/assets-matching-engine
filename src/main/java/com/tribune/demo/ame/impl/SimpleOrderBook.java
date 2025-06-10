package com.tribune.demo.ame.impl;


import com.tribune.demo.ame.domain.OrderBook;
import com.tribune.demo.ame.domain.OrderPublisher;
import com.tribune.demo.ame.domain.OrderEventType;
import com.tribune.demo.ame.domain.OrderEvent;
import com.tribune.demo.ame.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This class is a simple implementation of {@link OrderBook} for managing buy and sell orders.
 * It uses the following:
 * <ol>
 * <li>A {@link PriorityBlockingQueue} for `sell` orders, sorted by price (ascending) and timestamp (ascending).</li>
 * <li>A {@link PriorityBlockingQueue} for `buy` orders, sorted by price (descending) and timestamp (descending).</li>
 * <li>An {@link OrderPublisher} to handle events related to order processing.</li>
 * </ol>
 *
 */
@Slf4j
public class SimpleOrderBook implements OrderBook{

    private final OrderPublisher orderPublisher;

    @Getter
    @Setter
    private String asset;

    public SimpleOrderBook(String name, OrderPublisher orderPublisher) {
        this.asset = name;
        this.orderPublisher = orderPublisher;
    }


    // I need the least selling prices to come first
    private final Comparator<Order> comparator1 = Comparator.comparingDouble(Order::getPrice)//asc
            .thenComparing(Order::getTimestamp);//asc

    // I need the highest buying prices to come first
    private final Comparator<Order> comparator2 = Comparator.comparingDouble(Order::getPrice).reversed()//desc
            .thenComparing(Order::getTimestamp).reversed();//desc

    // I used PriorityQueue with synchronized methods but then searched the web and found this one to be a better alternative
    @Getter
    private final Queue<Order> sellQueue = new PriorityBlockingQueue<>(1, comparator1);

    @Getter
    private final Queue<Order> buyQueue = new PriorityBlockingQueue<>(1, comparator2);


    @Override
    public OrderResponse submit(Order order) {
        if (!asset.equals(order.getAsset())) {
            throw new IllegalArgumentException("This asset doesn't belong to this order book.");
        }

        OrderResponse response = OrderDirection.SELL.equals(order.getDirection()) ?
                sell(order) :
                buy(order);

        saveOrUpdateOrder(response);
        return response;
    }

    @Override
    public OrderResponse sell(Order order) {
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
            sellQueue.add(order);
        }
        return createOrderResponse(order, trades, pendingAmount);
    }


    @Override
    public OrderResponse buy(Order order) {
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

    /**
     * Calculates the pending amount for an order by processing trades against the other queue.
     *
     * @param order      The order for which the pending amount is calculated.
     * @param trades     The list of trades made during the processing of the order.
     * @param condition  The condition to check whether to continue processing trades.
     * @param otherQueue The queue against which the trades are processed (either buy or sell).
     * @return The remaining pending amount after processing trades.
     **/
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
            UpdateCounterpart ucp = UpdateCounterpart.builder()
                    .triggerId(order.getId())
                    .counterPartId(nextSell.getId())
                    .counterpartAmount(tradeAmount)
                    .counterpartPrice(order.getPrice())
                    .build();
            updateCounterpart(ucp);

            trades.add(currentTrade);

            pendingAmount -= tradeAmount;
        }
        return pendingAmount;
    }

    /**
     * Notifies subscribers about Save/Update of an order.
     */
    public void saveOrUpdateOrder(OrderResponse response) {
        OrderEvent event = new OrderEvent(response,
                "Update the order",
                OrderEventType.SAVE_OR_UPDATE_ORDER);
        event.setData(response.getId());

        orderPublisher.publish(event);
    }


    /**
     * Notifies subscribers to Update the counterpart order in the archive.
     **/
    public void updateCounterpart(UpdateCounterpart updateCounterpart) {
        long triggerId = updateCounterpart.getTriggerId();
        log.info("Updating archive for order {}", triggerId);

        OrderEvent event = new OrderEvent(updateCounterpart,
                "Update the counterpart",
                OrderEventType.UPDATE_COUNTERPART);
        event.setData(triggerId);

        orderPublisher.publish(event);
    }

}
