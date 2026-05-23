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
import com.example.courierapp.domain.entity.Courier;
import com.example.courierapp.domain.usecase.AuthUsecase;

public class RegisterActivity extends AppCompatActivity {

    private RadioGroup rgUserType;
    private View courierFields;
    private View clientFields;
    private EditText etFullName, etLogin, etPhone, etPassword, etAddress, etPassport, etDriverLicense;
    private Button btnRegister;
    private AuthUsecase authUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authUseCase = new AuthUsecase();

        rgUserType = findViewById(R.id.rg_user_type);
        courierFields = findViewById(R.id.courier_fields);
        clientFields = findViewById(R.id.client_fields);
        etFullName = findViewById(R.id.et_full_name);
        etLogin = findViewById(R.id.et_login);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etAddress = findViewById(R.id.et_address);
        etPassport = findViewById(R.id.et_passport);
        etDriverLicense = findViewById(R.id.et_driver_license);
        btnRegister = findViewById(R.id.btn_register);
        Button btnBack = findViewById(R.id.btn_back);

        rgUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_courier) {
                    courierFields.setVisibility(View.VISIBLE);
                    clientFields.setVisibility(View.GONE);
                } else {
                    courierFields.setVisibility(View.GONE);
                    clientFields.setVisibility(View.VISIBLE);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = etFullName.getText().toString().trim();
                final String login = etLogin.getText().toString().trim();
                final String phone = etPhone.getText().toString().trim();
                final String password = etPassword.getText().toString().trim();

                if (fullName.isEmpty() || login.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnRegister.setEnabled(false);
                btnRegister.setText("Регистрация...");

                if (rgUserType.getCheckedRadioButtonId() == R.id.rb_client) {
                    final String address = etAddress.getText().toString().trim();
                    if (address.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Введите адрес", Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Зарегистрироваться");
                        return;
                    }
                    final Client client = new Client(fullName, login, phone, password, address);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean success = authUseCase.registerClient(client);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Зарегистрироваться");

                                    if (success) {
                                        Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, ClientActivity.class);
                                        intent.putExtra("user", client);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).start();

                } else {
                    final String passport = etPassport.getText().toString().trim();
                    final String driverLicense = etDriverLicense.getText().toString().trim();

                    if (passport.isEmpty() || driverLicense.isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Зарегистрироваться");
                        return;
                    }

                    final Courier courier = new Courier(fullName, login, phone, password, passport, driverLicense);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean success = authUseCase.registerCourier(courier);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Зарегистрироваться");

                                    if (success) {
                                        Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, CourierActivity.class);
                                        intent.putExtra("user", courier);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}