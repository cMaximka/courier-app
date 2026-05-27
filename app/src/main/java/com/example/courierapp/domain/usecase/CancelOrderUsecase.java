package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;

public class CancelOrderUsecase {
    private final OrderRepository repository = new OrderRepository();

    // Отказ курьера от заказа: статус 1, залог не возвращается
    public void executeByCourier(String orderId) throws RuntimeException {
        boolean success = repository.cancelOrderByCourier(orderId);
        if (!success) throw new RuntimeException("Ошибка при отказе от заказа");
    }

    // Отмена заказа клиентом: статус 6
    public void executeByClient(String orderId) throws RuntimeException {
        boolean success = repository.cancelOrderByClient(orderId);
        if (!success) throw new RuntimeException("Ошибка при отмене заказа");
    }
}