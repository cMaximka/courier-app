package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;

public class CompleteOrderUsecase {
    private final OrderRepository repository = new OrderRepository();

    public void execute(String orderId, String clientId, String courierId, double price)
            throws RuntimeException {
        boolean success = repository.completeOrderTransaction(orderId, clientId, courierId, price);
        if (!success) {
            throw new RuntimeException("Ошибка завершения заказа: возможно, недостаточно средств у клиента");
        }
    }
}
