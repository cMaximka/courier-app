// AuthUsecase.java - УБИРАЕМ ВСЕ callback'и и потоки
package com.example.courierapp.domain.usecase;

import com.example.courierapp.data.AuthRepository;
import com.example.courierapp.domain.entity.Client;
import com.example.courierapp.domain.entity.Courier;
import com.example.courierapp.domain.entity.User;

public class AuthUsecase {

    private final AuthRepository repository;

    public AuthUsecase() {
        this.repository = new AuthRepository();
    }

    // Просто возвращаем результат или null/exception
    public User login(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        return repository.login(login, password);
    }

    public boolean registerClient(Client client) {
        String validationError = validateClientData(client);
        if (validationError != null) {
            return false;
        }

        if (repository.isLoginExists(client.getLogin())) {
            return false;
        }

        return repository.registerClient(client);
    }

    public boolean registerCourier(Courier courier) {
        String validationError = validateCourierData(courier);
        if (validationError != null) {
            return false;
        }

        if (repository.isLoginExists(courier.getLogin())) {
            return false;
        }

        return repository.registerCourier(courier);
    }

    private String validateClientData(Client client) {
        if (client.getFullName() == null || client.getFullName().trim().isEmpty()) {
            return "Введите полное имя";
        }
        if (client.getLogin() == null || client.getLogin().trim().isEmpty()) {
            return "Введите логин";
        }
        if (client.getPhone() == null || client.getPhone().trim().isEmpty()) {
            return "Введите телефон";
        }
        if (client.getPassword() == null || client.getPassword().trim().isEmpty()) {
            return "Введите пароль";
        }
        if (client.getPassword().length() < 4) {
            return "Пароль должен содержать минимум 4 символа";
        }
        if (client.getAddress() == null || client.getAddress().trim().isEmpty()) {
            return "Введите адрес";
        }
        return null;
    }

    private String validateCourierData(Courier courier) {
        if (courier.getFullName() == null || courier.getFullName().trim().isEmpty()) {
            return "Введите полное имя";
        }
        if (courier.getLogin() == null || courier.getLogin().trim().isEmpty()) {
            return "Введите логин";
        }
        if (courier.getPhone() == null || courier.getPhone().trim().isEmpty()) {
            return "Введите телефон";
        }
        if (courier.getPassword() == null || courier.getPassword().trim().isEmpty()) {
            return "Введите пароль";
        }
        if (courier.getPassword().length() < 4) {
            return "Пароль должен содержать минимум 4 символа";
        }
        if (courier.getPassportData() == null || courier.getPassportData().trim().isEmpty()) {
            return "Введите паспортные данные";
        }
        if (courier.getDriverLicense() == null || courier.getDriverLicense().trim().isEmpty()) {
            return "Введите водительские права";
        }
        return null;
    }
}