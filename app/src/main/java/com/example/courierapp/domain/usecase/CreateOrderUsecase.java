package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;

public class CreateOrderUsecase {
    private final OrderRepository repository = new OrderRepository();

    public Order execute(String clientId, String pickupAddress, String deliveryAddress,
                         double weight, double length, double width, double height, double productPrice)
            throws IllegalArgumentException, RuntimeException {
        String error = validate(pickupAddress, deliveryAddress, weight, length, width, height, productPrice);
        if (error != null) throw new IllegalArgumentException(error);
        double price = calculate(weight, length, width, height, productPrice);

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