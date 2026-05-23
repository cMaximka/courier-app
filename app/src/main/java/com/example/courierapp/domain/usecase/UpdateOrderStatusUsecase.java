package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;

public class UpdateOrderStatusUsecase {
    private final OrderRepository repository = new OrderRepository();

    public void execute(String orderId, int newStatus) throws RuntimeException {
        boolean success = repository.updateOrderStatus(orderId, newStatus);
        if (!success) throw new RuntimeException("Ошибка обновления статуса");
    }
}
