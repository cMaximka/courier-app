package com.example.courierapp.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.courierapp.R;
import com.example.courierapp.domain.entity.Order;
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.usecase.CreateOrderUsecase;

public class CreateOrderActivity extends AppCompatActivity {

    private EditText etPickupAddress, etDeliveryAddress, etWeight, etLength, etWidth, etHeight, etProductPrice;
    private TextView tvCalculatedPrice;
    private Button btnCreate;
    private CreateOrderUsecase createOrderUsecase;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        currentUser = (User) getIntent().getSerializableExtra("user");

        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        createOrderUsecase = new CreateOrderUsecase();

        etPickupAddress = findViewById(R.id.et_pickup_address);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etWeight = findViewById(R.id.et_weight);
        etLength = findViewById(R.id.et_length);
        etWidth = findViewById(R.id.et_width);
        etHeight = findViewById(R.id.et_height);
        etProductPrice = findViewById(R.id.et_product_price);
        tvCalculatedPrice = findViewById(R.id.tv_calculated_price);
        btnCreate = findViewById(R.id.btn_create);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOrder();
            }
        });
    }

    private void createOrder() {
        final String pickupAddress = etPickupAddress.getText().toString().trim();
        final String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        final double weight = parseDouble(etWeight.getText().toString());
        final double length = parseDouble(etLength.getText().toString());
        final double width = parseDouble(etWidth.getText().toString());
        final double height = parseDouble(etHeight.getText().toString());
        final double productPrice = parseDouble(etProductPrice.getText().toString());

        if (pickupAddress.isEmpty()) {
            Toast.makeText(this, "Введите адрес забора", Toast.LENGTH_SHORT).show();
            return;
        }
        if (deliveryAddress.isEmpty()) {
            Toast.makeText(this, "Введите адрес доставки", Toast.LENGTH_SHORT).show();
            return;
        }
        if (weight <= 0) {
            Toast.makeText(this, "Введите корректный вес", Toast.LENGTH_SHORT).show();
            return;
        }
        if (length <= 0 || width <= 0 || height <= 0) {
            Toast.makeText(this, "Введите корректные габариты", Toast.LENGTH_SHORT).show();
            return;
        }
        if (productPrice <= 0) {
            Toast.makeText(this, "Введите корректную цену товара", Toast.LENGTH_SHORT).show();
            return;
        }

        final Order order = new Order(
                currentUser.getId(),
                pickupAddress,
                deliveryAddress,
                weight,
                length,
                width,
                height,
                productPrice
        );

        btnCreate.setEnabled(false);
        btnCreate.setText("Создание...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = createOrderUsecase.createOrder(order);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnCreate.setEnabled(true);
                        btnCreate.setText("Создать заказ");

                        if (success) {
                            Toast.makeText(CreateOrderActivity.this, "Заказ успешно создан!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(CreateOrderActivity.this, "Ошибка при создании заказа", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }

    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}