package com.example.courierapp.domain.usecase;

import android.os.Handler;
import android.os.Looper;

import com.example.courierapp.data.AuthRepository;
import com.example.courierapp.domain.entity.Client;
import com.example.courierapp.domain.entity.Courier;
import com.example.courierapp.domain.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthUsecase {

    private final AuthRepository repository;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public AuthUsecase() {
        this.repository = new AuthRepository();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Один интерфейс для всех операций
    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    // Регистрация клиента
    public void registerClient(Client client, AuthCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String validationError = validateClientData(client);
                if (validationError != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(validationError);
                        }
                    });
                    return;
                }

                if (repository.isLoginExists(client.getLogin())) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("Пользователь с таким логином уже существует");
                        }
                    });
                    return;
                }

                boolean success = repository.registerClient(client);
                if (success) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(client);
                        }
                    });
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("Ошибка при регистрации. Попробуйте позже.");
                        }
                    });
                }
            }
        });
    }

    // Регистрация курьера
    public void registerCourier(Courier courier, AuthCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                String validationError = validateCourierData(courier);
                if (validationError != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(validationError);
                        }
                    });
                    return;
                }

                if (repository.isLoginExists(courier.getLogin())) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("Пользователь с таким логином уже существует");
                        }
                    });
                    return;
                }

                boolean success = repository.registerCourier(courier);
                if (success) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(courier);
                        }
                    });
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("Ошибка при регистрации. Попробуйте позже.");
                        }
                    });
                }
            }
        });
    }

    // Вход в систему
    public void login(String login, String password, AuthCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Валидация
                if (login == null || login.trim().isEmpty()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("Введите логин");
                        }
                    });
                    return;
                }

                if (password == null || password.trim().isEmpty()) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("Введите пароль");
                        }
                    });
                    return;
                }

                // Аутентификация
                User user = repository.login(login, password);
                if (user != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(user);
                        }
                    });
                } else {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure("Неверный логин или пароль");
                        }
                    });
                }
            }
        });
    }

    // Валидация данных клиента
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

    // Валидация данных курьера
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

    // Закрытие executor'а (вызывать при завершении работы приложения)
    public void shutdown() {
        executorService.shutdown();
    }
}