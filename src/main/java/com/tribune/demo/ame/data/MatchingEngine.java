package com.tribune.demo.ame.data;


import com.tribune.demo.ame.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Component
public class MatchingEngine {

    @Getter
    private final AtomicLong counter = new AtomicLong(1);

    // on archive, initial amount is constant
    private final Map<Long, OrderResponse> archive = new ConcurrentHashMap<>();

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


    public OrderResponse addOrder(Order order) {
        order.setId(counter.getAndIncrement());
        double pendingAmount = order.getAmount();
        List<Trade> trades = new ArrayList<>();



        if (order.getDirection() == OrderDirection.SELL) {
            log.info("Adding order to SELL queue");
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
                        updateArchive(order.getId(), nextBuy.getId(), tradeAmount, order.getPrice());

                        trades.add(currentTrade);

                        pendingAmount -= tradeAmount;
                    }
                }
            } else {
                buyQueue.add(order);
            }
        } else {
            log.info("Adding order to BUY queue");
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
                        updateArchive(order.getId(), nextSell.getId(), tradeAmount, order.getPrice());

                        trades.add(currentTrade);

                        pendingAmount -= tradeAmount;
                    }
                }
            } else {
                buyQueue.add(order);
            }
        }

        OrderResponse response = OrderResponse.builder()
                .id(order.getId())
                .price(order.getPrice())
                .asset(order.getAsset())
                .direction(order.getDirection())
                .time(LocalDateTime.now())
                .amount(order.getAmount())
                .pendingAmount(pendingAmount)
                .trades(trades)
                .build();
        updateArchive(order.getId(), response);
        System.out.println(sellQueue);
        return response;
    }

    private void updateArchive(Long id, OrderResponse response) {
        archive.put(id, response);
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
        archive.put(order.getId(), o);
    }


    public void updateArchive(Long triggerId, Long counterPartId, double counterpartAmount, double counterpartPrice) {
        log.info("Updating archive for order {}", counterPartId);
        if (archive.containsKey(counterPartId)) {
            OrderResponse o = archive.get(counterPartId);
            Trade trade = Trade.builder()
                    .orderId(triggerId)
                    .price(counterpartPrice)
                    .amount(counterpartAmount)
                    .build();
            o.setPendingAmount(o.getAmount() - counterpartAmount);
            o.addTrade(trade);
        }

    }

    public Order findById(long id) {

        return archive.get(id);
    }


}
