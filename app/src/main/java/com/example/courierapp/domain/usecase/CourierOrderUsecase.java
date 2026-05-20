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

    public boolean acceptOrder(String orderId, String courierId) {
        return repository.acceptOrder(orderId, courierId);
    }

    public boolean cancelOrder(String orderId) {
        return repository.cancelOrder(orderId);
    }
}