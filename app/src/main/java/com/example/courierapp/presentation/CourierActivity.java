package com.example.courierapp.presentation;

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

        // Обработчик переключения RadioButton
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
                acceptOrder(order);
            }

            @Override
            public void onCancelClick(Order order) {
                cancelOrder(order);
            }
        }, 1);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);

        loadAvailableOrders();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = courierOrderUsecase.acceptOrder(order.getId(), currentUser.getId());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(CourierActivity.this, "Заказ принят!", Toast.LENGTH_SHORT).show();
                            // Обновляем текущий список
                            if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders) {
                                loadAvailableOrders();
                            } else {
                                loadMyOrders();
                            }
                        } else {
                            Toast.makeText(CourierActivity.this, "Ошибка при принятии заказа", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(CourierActivity.this, "Заказ отменен!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (rgOrderType.getCheckedRadioButtonId() == R.id.rb_available_orders) {
            loadAvailableOrders();
        } else {
            loadMyOrders();
        }
    }
}