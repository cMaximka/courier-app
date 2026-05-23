
package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;
import java.util.List;

public class GetOrdersUsecase {
    private final OrderRepository repository = new OrderRepository();

    // для клиента
    public List<Order> getActiveOrders(String clientId) {
        return repository.getActiveOrdersByClientId(clientId);
    }
    public List<Order> getCompletedOrdersByClient(String clientId) {
        return repository.getCompletedOrdersByClientId(clientId);
    }

    // для курьера
    public List<Order> getAvailableOrders() {
        return repository.getAllAvailableOrders();
    }
    public List<Order> getMyOrders(String courierId) {
        return repository.getOrdersByCourierId(courierId);
    }
    public List<Order> getCompletedOrdersByCourier(String courierId) {
        return repository.getCompletedOrdersByCourierId(courierId);
    }
}