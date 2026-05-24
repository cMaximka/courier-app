package com.example.courierapp.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.courierapp.domain.usecase.UpdateOrderUsecase;

public class CreateOrderActivity extends AppCompatActivity {

    private EditText etPickupAddress, etDeliveryAddress, etWeight, etLength, etWidth, etHeight, etProductPrice;
    private TextView tvCalculatedPrice;
    private Button btnCreate;
    private User currentUser;
    private Order orderToEdit;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        currentUser = (User) getIntent().getSerializableExtra("user");
        orderToEdit = (Order) getIntent().getSerializableExtra("order");
        isEditMode = orderToEdit != null;

        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etPickupAddress = findViewById(R.id.et_pickup_address);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etWeight = findViewById(R.id.et_weight);
        etLength = findViewById(R.id.et_length);
        etWidth = findViewById(R.id.et_width);
        etHeight = findViewById(R.id.et_height);
        etProductPrice = findViewById(R.id.et_product_price);
        tvCalculatedPrice = findViewById(R.id.tv_calculated_price);
        btnCreate = findViewById(R.id.btn_create);
        Button btnBack = findViewById(R.id.btn_back);

        // Установить текст кнопки в зависимости от режима
        if (isEditMode) {
            btnCreate.setText("Сохранить");
            setTitle("Редактирование заказа");
            populateFormWithOrderData();
        } else {
            btnCreate.setText("Создать");
            setTitle("Создание заказа");
        }

        setupPriceCalculation();

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    updateOrder();
                } else {
                    createOrder();
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

    private void populateFormWithOrderData() {
        if (orderToEdit != null) {
            etPickupAddress.setText(orderToEdit.getPickupAddress());
            etDeliveryAddress.setText(orderToEdit.getDeliveryAddress());
            etWeight.setText(String.valueOf(orderToEdit.getWeight()));
            etLength.setText(String.valueOf(orderToEdit.getLength()));
            etWidth.setText(String.valueOf(orderToEdit.getWidth()));
            etHeight.setText(String.valueOf(orderToEdit.getHeight()));
            etProductPrice.setText(String.valueOf(orderToEdit.getProductPrice()));
            updateCalculatedPrice();
        }
    }

    private void setupPriceCalculation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateCalculatedPrice();
            }
        };
        etWeight.addTextChangedListener(watcher);
        etLength.addTextChangedListener(watcher);
        etWidth.addTextChangedListener(watcher);
        etHeight.addTextChangedListener(watcher);
        etProductPrice.addTextChangedListener(watcher);
    }

    private void updateCalculatedPrice() {
        double w = parseDouble(etWeight.getText().toString());
        double l = parseDouble(etLength.getText().toString());
        double wd = parseDouble(etWidth.getText().toString());
        double h = parseDouble(etHeight.getText().toString());
        double prodPrice = parseDouble(etProductPrice.getText().toString());
        double price = CreateOrderUsecase.calculate(w, l, wd, h, prodPrice);
        tvCalculatedPrice.setText(String.format("Стоимость доставки: %.2f руб", price));
    }

    private void createOrder() {
        final String pickupAddress = etPickupAddress.getText().toString().trim();
        final String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        final double weight = parseDouble(etWeight.getText().toString());
        final double length = parseDouble(etLength.getText().toString());
        final double width = parseDouble(etWidth.getText().toString());
        final double height = parseDouble(etHeight.getText().toString());
        final double productPrice = parseDouble(etProductPrice.getText().toString());

        CreateOrderUsecase usecase = new CreateOrderUsecase();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Order order = usecase.execute(
                            currentUser.getId(), pickupAddress, deliveryAddress,
                            weight, length, width, height, productPrice
                    );
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateOrderActivity.this, "Заказ создан!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } catch (RuntimeException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateOrderActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnCreate.setEnabled(true);
                        }
                    });
                }
            }
        }).start();
    }

    private void updateOrder() {
        final String pickupAddress = etPickupAddress.getText().toString().trim();
        final String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        final double weight = parseDouble(etWeight.getText().toString());
        final double length = parseDouble(etLength.getText().toString());
        final double width = parseDouble(etWidth.getText().toString());
        final double height = parseDouble(etHeight.getText().toString());
        final double productPrice = parseDouble(etProductPrice.getText().toString());

        // Обновить данные заказа
        orderToEdit.setPickupAddress(pickupAddress);
        orderToEdit.setDeliveryAddress(deliveryAddress);
        orderToEdit.setWeight(weight);
        orderToEdit.setLength(length);
        orderToEdit.setWidth(width);
        orderToEdit.setHeight(height);
        orderToEdit.setProductPrice(productPrice);
        double newPrice = UpdateOrderUsecase.calculate(weight, length, width, height, productPrice);
        orderToEdit.setPrice(newPrice);

        UpdateOrderUsecase usecase = new UpdateOrderUsecase();
        btnCreate.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    usecase.execute(orderToEdit);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateOrderActivity.this, "Заказ обновлен!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } catch (RuntimeException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateOrderActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnCreate.setEnabled(true);
                        }
                    });
                }
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