package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;

public class CancelOrderUsecase {
    private final OrderRepository repository = new OrderRepository();

    public void execute(String orderId) throws RuntimeException {
        boolean success = repository.cancelOrder(orderId);
        if (!success) throw new RuntimeException("Ошибка при отмене заказа");
    }
}