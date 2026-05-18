package com.example.courierapp.domain.usecase;

import android.os.Handler;
import android.os.Looper;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetOrdersUsecase {

    private final OrderRepository repository;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public GetOrdersUsecase() {
        this.repository = new OrderRepository();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface OrdersCallback {
        void onSuccess(List<Order> orders);
        void onFailure(String errorMessage);
    }

    public void getOrdersByClientId(String clientId, OrdersCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Order> orders = repository.getOrdersByClientId(clientId);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (orders != null && !orders.isEmpty()) {
                            callback.onSuccess(orders);
                        } else {
                            callback.onFailure("У вас пока нет заказов");
                        }
                    }
                });
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
