package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;

import java.util.List;

public class GetOrdersUsecase {
    private final OrderRepository repository;

    public GetOrdersUsecase() {
        this.repository = new OrderRepository();
    }
}