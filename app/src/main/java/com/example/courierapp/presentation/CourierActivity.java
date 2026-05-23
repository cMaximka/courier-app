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
import com.example.courierapp.domain.usecase.AcceptOrderUsecase;
import com.example.courierapp.domain.usecase.CancelOrderUsecase;
import com.example.courierapp.domain.usecase.GetOrdersUsecase;

import java.util.ArrayList;
import java.util.List;

public class CourierActivity extends AppCompatActivity {

    private RadioGroup rgOrderType;
    private Button btnRefresh;
    private Button btnProfile;
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private User currentUser;
    private List<Order> ordersList;
    private GetOrdersUsecase getOrdersUsecase;
    private AcceptOrderUsecase acceptOrderUsecase;
    private CancelOrderUsecase cancelOrderUsecase;

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

        ordersList = new ArrayList<>();

        rgOrderType = findViewById(R.id.rg_order_type);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnProfile = findViewById(R.id.btn_profile);
        rvOrders = findViewById(R.id.rv_courierOrders);
        Button btnBack = findViewById(R.id.btn_back);

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
                if (order.getStatus() >= 2) {
                    openOrderDetail(order);
                }
            }
        }, 1);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);

        getOrdersUsecase = new GetOrdersUsecase();
        acceptOrderUsecase = new AcceptOrderUsecase();
        cancelOrderUsecase = new CancelOrderUsecase();
        loadAvailableOrders();
    }

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
                List<Order> orders = getOrdersUsecase.getAvailableOrders();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ordersList.clear();
                        ordersList.addAll(orders);
                        orderAdapter.updateOrders(ordersList);
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
                List<Order> orders = getOrdersUsecase.getMyOrders(currentUser.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ordersList.clear();
                        ordersList.addAll(orders);
                        orderAdapter.updateOrders(ordersList);
                    }
                });
            }
        }).start();
    }

    private void acceptOrder(final Order order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    double newBalance = acceptOrderUsecase.execute(order.getId(), currentUser.getId(), order.getPrice());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentUser.setBalance(newBalance);
                            Toast.makeText(CourierActivity.this, "Заказ принят! Списано " + (order.getPrice()/2) + " руб.", Toast.LENGTH_LONG).show();
                            if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders)
                                loadAvailableOrders();
                            else
                                loadMyOrders();
                        }
                    });
                } catch (IllegalArgumentException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CourierActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (RuntimeException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CourierActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void cancelOrder(final Order order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cancelOrderUsecase.execute(order.getId());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CourierActivity.this, "Заказ отменён!", Toast.LENGTH_LONG).show();
                            if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders)
                                loadAvailableOrders();
                            else
                                loadMyOrders();
                        }
                    });
                } catch (RuntimeException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CourierActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
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