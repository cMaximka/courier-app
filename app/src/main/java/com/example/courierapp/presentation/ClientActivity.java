package com.example.courierapp.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courierapp.R;
import com.example.courierapp.data.OrderRepository;
import com.example.courierapp.domain.entity.Order;
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.usecase.GetOrdersUsecase;

import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity {

    private Button btnCreateOrder;
    private Button btnProfile;
    private Button btnRefresh;
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;
    private GetOrdersUsecase getOrdersUsecase;
    private User currentUser;
    private List<Order> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        currentUser = (User) getIntent().getSerializableExtra("user");

        if (currentUser == null) {
            Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        getOrdersUsecase = new GetOrdersUsecase();
        ordersList = new ArrayList<>();

        btnCreateOrder = findViewById(R.id.btn_createOrder);
        btnProfile = findViewById(R.id.btn_profile);
        btnRefresh = findViewById(R.id.btn_refresh);

        rvOrders = findViewById(R.id.rv_clientOrders);

        btnCreateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this, CreateOrderActivity.class);
                intent.putExtra("user", currentUser);
                startActivityForResult(intent, 1);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this, ProfileActivity.class);
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadOrders();
                Toast.makeText(ClientActivity.this, "Заказы обновлены", Toast.LENGTH_SHORT).show();
            }
        });

        // ИСПРАВЛЕНО: Создаём адаптер ОДИН раз, с правильным слушателем
        orderAdapter = new OrderAdapter(ordersList, new OrderAdapter.OnOrderActionListener() {
            @Override
            public void onAcceptClick(Order order) { }
            @Override
            public void onCancelClick(Order order) { }
            @Override
            public void onItemClick(Order order) {
                openOrderDetail(order);
            }
        }, 0);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);

        loadOrders();
    }

    private void loadOrders() {
        new Thread(() -> {
            List<Order> orders = new OrderRepository().getActiveOrdersByClientId(currentUser.getId());
            runOnUiThread(() -> {
                ordersList.clear();
                ordersList.addAll(orders);
                orderAdapter.updateOrders(ordersList);
                if (ordersList.isEmpty()) {
                    Toast.makeText(ClientActivity.this, "У вас нет активных заказов", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(ClientActivity.this, OrderDetailActivity.class);
        intent.putExtra("order", order);
        intent.putExtra("user", currentUser);
        intent.putExtra("userType", "client");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadOrders();
        }
        if ((requestCode == 101 || requestCode == 100) && resultCode == RESULT_OK) {
            loadOrders();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}