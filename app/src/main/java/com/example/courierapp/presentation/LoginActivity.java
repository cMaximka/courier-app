package com.example.courierapp.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.courierapp.R;
import com.example.courierapp.domain.entity.Client;
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.usecase.AuthUsecase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etLogin;
    private EditText etPassword;
    private Button btnLogin;
    private RadioGroup rgUserType;
    private AuthUsecase authUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authUseCase = new AuthUsecase();
        init();
    }

    private void init() {
        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_register);
        rgUserType = findViewById(R.id.rg_user_type);

        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_register) {
            performLogin();
        }
    }

    private void performLogin() {
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty()) {
            Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        authUseCase.login(login, password, new AuthUsecase.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Добро пожаловать, " + user.getFullName(), Toast.LENGTH_SHORT).show();

                        Intent intent;
                        if (user instanceof Client) {
                            intent = new Intent(LoginActivity.this, ClientActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, CourierActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authUseCase != null) {
            authUseCase.shutdown();
        }
    }
}