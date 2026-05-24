package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.data.AuthRepository;
import com.example.courierapp.domain.entity.Order;
import com.example.courierapp.domain.entity.User;

public class CreateOrderUsecase {
    private final OrderRepository repository = new OrderRepository();
    private final AuthRepository authRepository = new AuthRepository();

    public Order execute(String clientId, String pickupAddress, String deliveryAddress,
                         double weight, double length, double width, double height, double productPrice)
            throws IllegalArgumentException, RuntimeException {
        String error = validate(pickupAddress, deliveryAddress, weight, length, width, height, productPrice);
        if (error != null) throw new IllegalArgumentException(error);
        double price = calculate(weight, length, width, height, productPrice);
        
        User client = authRepository.getUserById(clientId);
        if (client == null) throw new IllegalArgumentException("Клиент не найден");

        double balance = client.getBalance();
        double activeSum = 0.0;
        for (Order o : repository.getActiveOrdersByClientId(clientId)) {
            activeSum += o.getPrice();
        }

        if (balance < price) {
            throw new IllegalArgumentException("Недостаточно средств на балансе для оплаты этого заказа");
        }

        if (balance < (activeSum + price)) {
            throw new IllegalArgumentException("Недостаточно средств: у вас есть незавершённые заказы и недостаточно баланса, чтобы оплатить их вместе с новым");
        }

        Order order = new Order(clientId, pickupAddress, deliveryAddress,
                weight, length, width, height, productPrice);
        order.setPrice(price);

        boolean created = repository.createOrder(order);
        if (!created) throw new RuntimeException("Не удалось создать заказ");
        return order;
    }

    private String validate(String pickup, String delivery, double weight,
                            double length, double width, double height, double productPrice) {
        if (pickup == null || pickup.trim().isEmpty()) return "Введите адрес забора";
        if (delivery == null || delivery.trim().isEmpty()) return "Введите адрес доставки";
        if (weight <= 0) return "Введите корректный вес";
        if (length <= 0 || width <= 0 || height <= 0) return "Введите корректные габариты";
        if (productPrice <= 0) return "Введите корректную цену товара";
        return null;
    }

    public static double calculate(double weight, double length, double width, double height, double productPrice) {
        double basePrice = 100.0;
        double weightCoeff = weight * 20;
        double volume = (length * width * height) / 1000.0;
        double volumeCoeff = volume * 10;
        double insuranceCoeff = productPrice * 0.02;
        return basePrice + weightCoeff + volumeCoeff + insuranceCoeff;
    }
}