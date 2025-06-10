package com.tribune.demo.ame.domain;


import com.tribune.demo.ame.model.Order;
import com.tribune.demo.ame.model.OrderResponse;

import java.util.List;


/**
 * The MatchingEngine interface defines the contract for a matching engine that manages OrderBooks.
 * It provides methods to retrieve, create, delete OrderBooks, and find orders by ID or asset.
 */
public interface MatchingEngine {

    /**
     * Gets the next available order ID.
     */
    Long getNextOrderId();

    /**
     * Retrieves an OrderBook by its name.
     * If the OrderBook does not exist, it throws an IllegalArgumentException.
     *
     * @param name The name of the asset for which to retrieve the OrderBook.
     * @return The OrderBook associated with the specified asset name.
     */
    OrderBook getOrderBook(String name);

    /**
     * Creates a new OrderBook for the specified asset name.
     * If an OrderBook with the same name already exists, it returns the existing one.
     *
     * @param name The name of the asset for which to create an OrderBook.
     * @return The created or existing OrderBook.
     */
    OrderBook newOrderBook(String name);

    /**
     * Deletes an OrderBook by its name.
     * If the OrderBook does not exist, it returns false.
     *
     * @param name The name of the asset for which to delete the OrderBook.
     * @return true if the OrderBook was successfully deleted, false otherwise.
     */
    boolean deleteOrderBook(String name);

    /**
     * Finds an order by ID.
     *
     * @param id The ID of the order to find.
     * @return The Order related to the provided ID.
     */
    OrderResponse findOrderById(long id);

    /**
     * Finds all live orders by a given asset.
     *
     * @param name The name of the asset for which to find live orders.
     * @return A list of all live orders for a specified asset name.
     */
    List<Order> findAllLiveOrdersByAsset(String name, String direction);
}
