package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;

public class UpdateOrderUsecase {
    private final OrderRepository repository = new OrderRepository();

    public void execute(Order order) throws RuntimeException {
        if (order.getId() == null || order.getId().isEmpty()) {
            throw new RuntimeException("ID заказа не найден");
        }
        
        if (order.getPickupAddress() == null || order.getPickupAddress().trim().isEmpty()) {
            throw new RuntimeException("Укажите адрес забора");
        }
        
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            throw new RuntimeException("Укажите адрес доставки");
        }
        
        if (order.getWeight() <= 0 || order.getLength() <= 0 || 
            order.getWidth() <= 0 || order.getHeight() <= 0) {
            throw new RuntimeException("Все размеры должны быть больше нуля");
        }
        
        boolean success = repository.updateOrder(order);
        if (!success) throw new RuntimeException("Ошибка при обновлении заказа. Заказ может быть принят курьером.");
    }

    public static double calculate(double weight, double length, double width, double height, double productPrice) {
        // Базовая ставка за доставку
        double baseRate = 50.0;
        
        // Расчет объема (см³)
        double volume = length * width * height;
        
        // Коэффициент за вес (0.5 рубля за кг)
        double weightCost = weight * 0.5;
        
        // Коэффициент за объем (0.01 рубля за см³)
        double volumeCost = volume * 0.01;
        
        // Коэффициент за стоимость товара (1% от стоимости)
        double valueCost = productPrice * 0.01;
        
        // Итоговая стоимость доставки
        double totalCost = baseRate + weightCost + volumeCost + valueCost;
        
        // Минимальная стоимость 100 рублей
        return Math.max(totalCost, 100.0);
    }
}
