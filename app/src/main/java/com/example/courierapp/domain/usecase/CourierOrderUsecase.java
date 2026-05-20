package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;

import java.util.List;

public class CourierOrderUsecase {
    private final OrderRepository repository;

    public CourierOrderUsecase() {
        this.repository = new OrderRepository();
    }

    public List<Order> getAvailableOrders() {
        return repository.getAllAvailableOrders();
    }

    public List<Order> getMyOrders(String courierId) {
        return repository.getOrdersByCourierId(courierId);
    }

    // Принять заказ со списанием средств
    public boolean acceptOrder(String orderId, String courierId, double price) {
        return repository.acceptOrderWithPayment(orderId, courierId, price);
    }

    // Отменить заказ (без возврата)
    public boolean cancelOrder(String orderId) {
        return repository.cancelOrder(orderId);
    }
}