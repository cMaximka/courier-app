package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.AuthRepository;
import com.example.courierapp.domain.entity.User;

public class UpdateProfileUsecase {
    private final AuthRepository repository = new AuthRepository();

    public void execute(User user) throws IllegalArgumentException, RuntimeException {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty())
            throw new IllegalArgumentException("Введите полное имя");
        if (user.getPhone() == null || user.getPhone().trim().isEmpty())
            throw new IllegalArgumentException("Введите телефон");

        boolean success = repository.updateProfile(user);
        if (!success) throw new RuntimeException("Ошибка обновления профиля");
    }
}