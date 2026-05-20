package com.example.courierapp.presentation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courierapp.R;
import com.example.courierapp.domain.entity.Order;
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.usecase.CourierOrderUsecase;

import java.util.ArrayList;
import java.util.List;

public class CourierActivity extends AppCompatActivity {

    private RadioGroup rgOrderType;
    private Button btnRefresh;
    private Button btnProfile;
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private CourierOrderUsecase courierOrderUsecase;
    private User currentUser;
    private List<Order> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);

        currentUser = (User) getIntent().getSerializableExtra("user");

        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        courierOrderUsecase = new CourierOrderUsecase();
        ordersList = new ArrayList<>();

        rgOrderType = findViewById(R.id.rg_order_type);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnProfile = findViewById(R.id.btn_profile);
        rvOrders = findViewById(R.id.rv_courierOrders);

        rgOrderType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_available_orders) {
                    loadAvailableOrders();
                } else if (checkedId == R.id.rb_my_orders) {
                    loadMyOrders();
                }
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders) {
                    loadAvailableOrders();
                } else {
                    loadMyOrders();
                }
                Toast.makeText(CourierActivity.this, "Заказы обновлены", Toast.LENGTH_SHORT).show();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CourierActivity.this, ProfileActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });

        orderAdapter = new OrderAdapter(ordersList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onAcceptClick(Order order) {
                showAcceptConfirmationDialog(order);
            }

            @Override
            public void onCancelClick(Order order) {
                showCancelConfirmationDialog(order);
            }

            @Override
            public void onItemClick(Order order) {
                // Только принятые заказы (статус 2 и выше) могут быть кликабельны
                if (order.getStatus() >= 2) {
                    openOrderDetail(order);
                }
            }
        }, 1);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);

        loadAvailableOrders();
    }

    // Диалог подтверждения принятия заказа
    private void showAcceptConfirmationDialog(final Order order) {
        double deposit = order.getPrice() / 2;
        double currentBalance = currentUser.getBalance();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение принятия заказа");
        builder.setMessage(String.format(
                "Вы уверены, что хотите принять этот заказ?\n\n" +
                        "Стоимость заказа: %.2f руб.\n" +
                        "Взнос за заказ: %.2f руб.\n" +
                        "Ваш текущий баланс: %.2f руб.\n\n" +
                        "После принятия заказа с вашего баланса будет списано %.2f руб.\n" +
                        "Отказаться от заказа после принятия будет возможно, но взнос НЕ возвращается!",
                order.getPrice(), deposit, currentBalance, deposit));

        builder.setPositiveButton("Принять", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                acceptOrder(order);
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    // Диалог подтверждения отмены заказа
    private void showCancelConfirmationDialog(final Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение отмены заказа");
        builder.setMessage(String.format(
                "Вы уверены, что хотите отказаться от заказа №%s?\n\n" +
                        "ВНИМАНИЕ: Взнос за заказ (%.2f руб.) НЕ ВОЗВРАЩАЕТСЯ!",
                order.getId(), order.getPrice() / 2));

        builder.setPositiveButton("Отказаться", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelOrder(order);
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void loadAvailableOrders() {
        orderAdapter.updateMode(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Order> orders = courierOrderUsecase.getAvailableOrders();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ordersList.clear();
                        ordersList.addAll(orders);
                        orderAdapter.updateOrders(ordersList);

                        if (ordersList.isEmpty()) {
                            Toast.makeText(CourierActivity.this, "Нет доступных заказов", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void loadMyOrders() {
        orderAdapter.updateMode(2);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Order> orders = courierOrderUsecase.getMyOrders(currentUser.getId());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ordersList.clear();
                        ordersList.addAll(orders);
                        orderAdapter.updateOrders(ordersList);

                        if (ordersList.isEmpty()) {
                            Toast.makeText(CourierActivity.this, "У вас нет принятых заказов", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void acceptOrder(final Order order) {
        final double deposit = order.getPrice() / 2;

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = courierOrderUsecase.acceptOrder(order.getId(), currentUser.getId(), order.getPrice());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            // Обновляем баланс в объекте пользователя
                            double newBalance = currentUser.getBalance() - deposit;
                            currentUser.setBalance(newBalance);

                            Toast.makeText(CourierActivity.this,
                                    String.format("Заказ принят! С баланса списано %.2f руб.", deposit),
                                    Toast.LENGTH_LONG).show();

                            // Обновляем текущий список
                            if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders) {
                                loadAvailableOrders();
                            } else {
                                loadMyOrders();
                            }
                        } else {
                            // Проверяем причину ошибки
                            if (currentUser.getBalance() < deposit) {
                                Toast.makeText(CourierActivity.this,
                                        "Недостаточно средств на балансе! Пополните баланс в профиле.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(CourierActivity.this, "Ошибка при принятии заказа", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void cancelOrder(final Order order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = courierOrderUsecase.cancelOrder(order.getId());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(CourierActivity.this,
                                    "Заказ отменен! Взнос не возвращен.",
                                    Toast.LENGTH_LONG).show();

                            if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders) {
                                loadAvailableOrders();
                            } else {
                                loadMyOrders();
                            }
                        } else {
                            Toast.makeText(CourierActivity.this, "Ошибка при отмене заказа", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(CourierActivity.this, OrderDetailActivity.class);
        intent.putExtra("order", order);
        intent.putExtra("user", currentUser);
        intent.putExtra("userType", "courier");
        startActivityForResult(intent, 100);
    }

    // Добавьте onActivityResult для обновления списка после возврата
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders) {
                loadAvailableOrders();
            } else {
                loadMyOrders();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем баланс пользователя при возвращении на экран
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.example.courierapp.data.AuthRepository authRepo = new com.example.courierapp.data.AuthRepository();
                final User updatedUser = authRepo.getUserById(currentUser.getId());
                if (updatedUser != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentUser.setBalance(updatedUser.getBalance());
                        }
                    });
                }
            }
        }).start();

        if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders) {
            loadAvailableOrders();
        } else {
            loadMyOrders();
        }
    }
}