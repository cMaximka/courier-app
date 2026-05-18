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
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.usecase.AuthUsecase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private RadioGroup rgUserType;
    private View courierFields;
    private View clientFields;
    private EditText etFullName;
    private EditText etLogin;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etAddress;
    private EditText etPassport;
    private EditText etDriverLicense;
    private Button btnRegister;

    private AuthUsecase authUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authUseCase = new AuthUsecase();
        init();
    }

    private void init() {
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

        rgUserType.setOnCheckedChangeListener(this);
        btnRegister.setOnClickListener(this);
    }

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_register) {
            performRegistration();
        }
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String login = etLogin.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (rgUserType == null) {
            Toast.makeText(this, "Ошибка инициализации формы", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedUserType = rgUserType.getCheckedRadioButtonId();

        if (selectedUserType == R.id.rb_client) {
            String address = etAddress.getText().toString().trim();
            Client client = new Client(fullName, login, phone, password, address);

            authUseCase.registerClient(client, new AuthUsecase.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, ClientActivity.class);
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
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else if (selectedUserType == R.id.rb_courier) {
            String passport = etPassport.getText().toString().trim();
            String driverLicense = etDriverLicense.getText().toString().trim();
            Courier courier = new Courier(fullName, login, phone, password, passport, driverLicense);

            authUseCase.registerCourier(courier, new AuthUsecase.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, CourierActivity.class);
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
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "Выберите тип пользователя", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (authUseCase != null) {
            authUseCase.shutdown();
        }
    }
}