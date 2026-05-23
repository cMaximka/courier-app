package com.example.courierapp.presentation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courierapp.R;
import com.example.courierapp.domain.entity.Client;
import com.example.courierapp.domain.entity.Courier;
import com.example.courierapp.domain.entity.Order;
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.usecase.AddBalanceUsecase;
import com.example.courierapp.domain.usecase.GetOrdersUsecase;
import com.example.courierapp.domain.usecase.UpdateProfileUsecase;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvFullName, tvPhone, tvLogin, tvBalance, tvSpecialInfo;
    private Button btnEditProfile, btnAddBalance, btnBack;
    private RecyclerView rvOrderHistory;
    private OrderAdapter orderAdapter;
    private List<Order> ordersList;

    private User currentUser;
    private GetOrdersUsecase getOrdersUsecase = new GetOrdersUsecase();
    private UpdateProfileUsecase updateProfileUsecase = new UpdateProfileUsecase();
    private AddBalanceUsecase addBalanceUsecase = new AddBalanceUsecase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        currentUser = (User) getIntent().getSerializableExtra("user");

        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        ordersList = new ArrayList<>();

        tvFullName = findViewById(R.id.tv_full_name);
        tvPhone = findViewById(R.id.tv_phone);
        tvLogin = findViewById(R.id.tv_login);
        tvBalance = findViewById(R.id.tv_balance);
        tvSpecialInfo = findViewById(R.id.tv_special_info);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnAddBalance = findViewById(R.id.btn_add_balance);
        btnBack = findViewById(R.id.btn_back);
        rvOrderHistory = findViewById(R.id.rv_order_history);

        orderAdapter = new OrderAdapter(ordersList);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        rvOrderHistory.setAdapter(orderAdapter);

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        btnAddBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBalanceDialog();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        
        loadUserData();
        loadOrderHistory();
    }

    private void loadUserData() {
        tvFullName.setText(currentUser.getFullName());
        tvPhone.setText(currentUser.getPhone());
        tvLogin.setText(currentUser.getLogin());
        tvBalance.setText(String.format("%.2f руб.", currentUser.getBalance()));

        if (currentUser instanceof Client) {
            Client client = (Client) currentUser;
            tvSpecialInfo.setText("Адрес: " + client.getAddress());
        } else if (currentUser instanceof Courier) {
            Courier courier = (Courier) currentUser;
            tvSpecialInfo.setText("Паспорт: " + courier.getPassportData() + "\nПрава: " + courier.getDriverLicense());
        }
    }

    private void loadOrderHistory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Order> orders;
                if (currentUser instanceof Client) {
                    orders = getOrdersUsecase.getCompletedOrdersByClient(currentUser.getId());
                } else {
                    orders = getOrdersUsecase.getCompletedOrdersByCourier(currentUser.getId());
                }
                final List<Order> finalOrders = orders;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ordersList.clear();
                        ordersList.addAll(finalOrders);
                        orderAdapter.updateOrders(ordersList);
                    }
                });
            }
        }).start();
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Редактирование профиля");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        final EditText etFullName = new EditText(this);
        etFullName.setHint("Полное имя");
        etFullName.setText(currentUser.getFullName());
        layout.addView(etFullName);

        final EditText etPhone = new EditText(this);
        etPhone.setHint("Телефон");
        etPhone.setText(currentUser.getPhone());
        layout.addView(etPhone);

        final EditText etSpecial = new EditText(this);
        if (currentUser instanceof Client) {
            etSpecial.setHint("Адрес");
            etSpecial.setText(((Client) currentUser).getAddress());
        } else if (currentUser instanceof Courier) {
            etSpecial.setHint("Паспортные данные");
            etSpecial.setText(((Courier) currentUser).getPassportData());
        }
        layout.addView(etSpecial);

        if (currentUser instanceof Courier) {
            final EditText etDriverLicense = new EditText(this);
            etDriverLicense.setHint("Водительские права");
            etDriverLicense.setText(((Courier) currentUser).getDriverLicense());
            layout.addView(etDriverLicense);
        }

        builder.setView(layout);

        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newFullName = etFullName.getText().toString().trim();
                final String newPhone = etPhone.getText().toString().trim();
                final String newSpecial = etSpecial.getText().toString().trim();

                if (newFullName.isEmpty() || newPhone.isEmpty() || newSpecial.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentUser.setFullName(newFullName);
                currentUser.setPhone(newPhone);

                if (currentUser instanceof Client) {
                    ((Client) currentUser).setAddress(newSpecial);
                } else if (currentUser instanceof Courier) {
                    ((Courier) currentUser).setPassportData(newSpecial);
                    if (layout.getChildCount() > 3) {
                        EditText etLicense = (EditText) layout.getChildAt(3);
                        String newLicense = etLicense.getText().toString().trim();
                        if (!newLicense.isEmpty()) {
                            ((Courier) currentUser).setDriverLicense(newLicense);
                        }
                    }
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateProfileUsecase.execute(currentUser);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProfileActivity.this, "Профиль обновлён", Toast.LENGTH_SHORT).show();
                                    loadUserData();
                                }
                            });
                        } catch (RuntimeException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showAddBalanceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Пополнение баланса");

        final EditText etAmount = new EditText(this);
        etAmount.setHint("Введите сумму");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(etAmount);

        builder.setPositiveButton("Пополнить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String amountStr = etAmount.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Введите сумму", Toast.LENGTH_SHORT).show();
                    return;
                }

                final double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(ProfileActivity.this, "Сумма должна быть больше 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            addBalanceUsecase.execute(currentUser.getId(), amount);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    currentUser.setBalance(currentUser.getBalance() + amount);
                                    loadUserData();
                                    Toast.makeText(ProfileActivity.this, "Баланс пополнен", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (RuntimeException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }
}
