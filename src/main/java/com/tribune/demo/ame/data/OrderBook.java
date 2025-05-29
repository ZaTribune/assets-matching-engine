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
    private String asset;

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
    private final Queue<Order> sellQueue = new PriorityBlockingQueue<>(1, comparator1);

    @Getter
    private final Queue<Order> buyQueue = new PriorityBlockingQueue<>(1, comparator2);

    /**
     * Adds an order to the order book.
     * If the order is a SELL order, it will be processed against the BUY queue.
     * If the order is a BUY order, it will be processed against the SELL queue.
     * The method will create an {@code OrderResponse} containing the order details,
     * trades made, and any pending amount.
     *
     * @param order The order to be added.
     **/
    public OrderResponse addOrder(Order order) {
        if (!asset.equals(order.getAsset())) {
            throw new IllegalArgumentException("This asset doesn't belong to this order book.");
        }
        order.setId(counter.getAndIncrement());

        OrderResponse response = OrderDirection.SELL.equals(order.getDirection()) ?
                processSell(order) :
                processBuy(order);

        saveOrUpdateOrder(response);
        return response;
    }

    /**
     * Processes a SELL order by checking against the BUY queue.
     *
     * @param order The SELL order to be processed.
     * @return OrderResponse containing the order details, trades made, and any pending amount.
     */
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
            sellQueue.add(order);
        }
        return createOrderResponse(order, trades, pendingAmount);
    }

    /**
     * Processes a BUY order by checking against the BUY queue.
     *
     * @param order The BUY order to be processed.
     * @return OrderResponse containing the order details, trades made, and any pending amount.
     */
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
            updateCounterpart(order.getId(), nextSell.getId(), tradeAmount, order.getPrice());

            trades.add(currentTrade);

            pendingAmount -= tradeAmount;
        }
        return pendingAmount;
    }

    /**
     * Creates an OrderResponse object from the given order, trades, and pending amount.
     **/
    OrderResponse createOrderResponse(Order order, List<Trade> trades, double pendingAmount) {
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

    /**
     * Updates the order in the event bus.
     * This method creates an OrderEvent with the given response and publishes it to the event bus.
     *
     * @param response The OrderResponse containing the updated order details.
     */
    void saveOrUpdateOrder(OrderResponse response) {
        OrderEvent event = new OrderEvent(response,
                "Update the order",
                EventType.SAVE_OR_UPDATE_ORDER);
        event.setData(response.getId());

        eventBus.publish(event);
    }


    /**
     * Updates the counterpart order in the archive.
     * This method creates an UpdateCounterpart object with the given details and publishes an OrderEvent to the event bus.
     *
     * @param triggerId         The ID of the order that triggered the update.
     * @param counterPartId     The ID of the counterpart order being updated.
     * @param counterpartAmount The amount of the counterpart order.
     * @param counterpartPrice  The price of the counterpart order.
     **/
    private void updateCounterpart(Long triggerId, Long counterPartId, double counterpartAmount, double counterpartPrice) {
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
