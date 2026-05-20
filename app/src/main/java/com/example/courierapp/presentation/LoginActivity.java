package com.example.courierapp.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.courierapp.R;
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.entity.Client;
import com.example.courierapp.domain.usecase.AuthUsecase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etLogin;
    private EditText etPassword;
    private Button btnLogin;
    private AuthUsecase authUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authUseCase = new AuthUsecase();

        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            final String login = etLogin.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            if (login.isEmpty()) {
                Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            // Блокируем кнопку, чтобы не было повторных нажатий
            btnLogin.setEnabled(false);
            btnLogin.setText("Вход...");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final User user = authUseCase.login(login, password);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Войти");

                            if (user != null) {
                                Toast.makeText(LoginActivity.this, "Добро пожаловать, " + user.getFullName(), Toast.LENGTH_SHORT).show();

                                Intent intent;
                                if (user instanceof Client) {
                                    intent = new Intent(LoginActivity.this, ClientActivity.class);
                                } else {
                                    intent = new Intent(LoginActivity.this, CourierActivity.class);
                                }
                                intent.putExtra("user", user);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).start();
        }
    }
}