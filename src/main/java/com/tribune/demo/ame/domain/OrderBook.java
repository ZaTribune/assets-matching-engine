package com.tribune.demo.ame.domain;


import com.tribune.demo.ame.model.*;



/**
 * Contract for an Order Book that manages orders for a specific asset.
 * * This interface defines methods for submitting orders, processing buy and sell orders,
 * * creating order responses, and updating orders in Event Driven manner.
 **/
public interface OrderBook {


    /**
     * Adds an order to the order book
     * If the order is a SELL order, it will be processed against the BUY queue.
     * If the order is a BUY order, it will be processed against the SELL queue.
     * The method will create an {@code OrderResponse} containing the order details,
     * trades made, and any pending amount.
     *
     * @param order The order to be added.
     **/
    Order submit(Order order);

    /**
     * Processes a SELL order by checking against the BUY queue.
     *
     * @param order The SELL order to be processed.
     * @return OrderResponse containing the order details, trades made, and any pending amount.
     */
    Order sell(Order order);

    /**
     * Processes a BUY order by checking against the BUY queue.
     *
     * @param order The BUY order to be processed.
     * @return OrderResponse containing the order details, trades made, and any pending amount.
     */
    Order buy(Order order);

    /**
     * Saves/Updates an order.
     *
     * @param response The OrderResponse containing the updated order details.
     */
    void saveOrUpdateOrder(Order response);


    /**
     * Updates the counterpart order.
     * @param updateCounterpart the details of the order to be updated.
     **/
    void updateCounterpart(UpdateCounterpart updateCounterpart);
}
