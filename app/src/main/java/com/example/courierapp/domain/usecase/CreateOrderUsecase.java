package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;

public class CreateOrderUsecase {
    private final OrderRepository repository;

    public CreateOrderUsecase() {
        this.repository = new OrderRepository();
    }

    public boolean createOrder(Order order) {
        String validationError = validateOrder(order);
        if (validationError != null) {
            return false;
        }
        return repository.createOrder(order);
    }

    private String validateOrder(Order order) {
        if (order.getPickupAddress() == null || order.getPickupAddress().trim().isEmpty()) {
            return "Введите адрес забора";
        }
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            return "Введите адрес доставки";
        }
        if (order.getWeight() <= 0) {
            return "Введите корректный вес";
        }
        if (order.getLength() <= 0 || order.getWidth() <= 0 || order.getHeight() <= 0) {
            return "Введите корректные габариты";
        }
        if (order.getProductPrice() <= 0) {
            return "Введите корректную цену товара";
        }
        return null;
    }
}