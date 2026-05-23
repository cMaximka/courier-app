package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.AuthRepository;

public class AddBalanceUsecase {
    private final AuthRepository repository = new AuthRepository();

    public double execute(String userId, double amount) throws IllegalArgumentException, RuntimeException {
        if (amount <= 0) throw new IllegalArgumentException("Сумма должна быть больше 0");
        boolean success = repository.addBalance(userId, amount);
        if (!success) throw new RuntimeException("Ошибка пополнения баланса");

        return amount;
    }
}
