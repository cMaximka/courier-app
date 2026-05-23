package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.AuthRepository;
import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.User;

public class AcceptOrderUsecase {
    private final OrderRepository orderRepository = new OrderRepository();
    private final AuthRepository authRepository = new AuthRepository();

    public double execute(String orderId, String courierId, double orderPrice)
            throws IllegalArgumentException, RuntimeException {
        User courier = authRepository.getUserById(courierId);
        if (courier == null) throw new IllegalArgumentException("Курьер не найден");

        double deposit = orderPrice / 2.0;
        if (courier.getBalance() < deposit) {
            throw new IllegalArgumentException("Недостаточно средств на балансе");
        }

        boolean success = orderRepository.acceptOrderWithPayment(orderId, courierId, deposit);
        if (!success) throw new RuntimeException("Ошибка при принятии заказа");

        return courier.getBalance() - deposit;
    }
}
