package com.example.courierapp.presentation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.courierapp.R;
import com.example.courierapp.domain.entity.Order;
import com.example.courierapp.domain.entity.User;
import com.example.courierapp.domain.usecase.CancelOrderUsecase;
import com.example.courierapp.domain.usecase.CompleteOrderUsecase;
import com.example.courierapp.domain.usecase.UpdateOrderStatusUsecase;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvPickupAddress, tvDeliveryAddress, tvWeight, tvDimensions;
    private TextView tvPrice, tvDeposit, tvStatus, tvCreatedAt;
    private TextView tvClientInfo, tvCourierInfo;
    private Button btnPickupOrder, btnDeliverOrder, btnBack, btnCompleteOrder;
    private Button btnEditOrder, btnCancelOrder;
    private LinearLayout clientButtons;
    private Order order;
    private User currentUser;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        order = (Order) getIntent().getSerializableExtra("order");
        currentUser = (User) getIntent().getSerializableExtra("user");
        userType = getIntent().getStringExtra("userType");

        if (order == null || currentUser == null) {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        tvPickupAddress = findViewById(R.id.tv_detail_pickup_address);
        tvDeliveryAddress = findViewById(R.id.tv_detail_delivery_address);
        tvWeight = findViewById(R.id.tv_detail_weight);
        tvDimensions = findViewById(R.id.tv_detail_dimensions);
        tvPrice = findViewById(R.id.tv_detail_price);
        tvDeposit = findViewById(R.id.tv_detail_deposit);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvCreatedAt = findViewById(R.id.tv_detail_created_at);
        tvClientInfo = findViewById(R.id.tv_detail_client_info);
        tvCourierInfo = findViewById(R.id.tv_detail_courier_info);
        btnPickupOrder = findViewById(R.id.btn_pickup_order);
        btnDeliverOrder = findViewById(R.id.btn_deliver_order);
        btnBack = findViewById(R.id.btn_back);
        btnCompleteOrder = findViewById(R.id.btn_complete_order);
        btnEditOrder = findViewById(R.id.btn_edit_order);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        clientButtons = findViewById(R.id.client_buttons);

         btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        displayOrderInfo();
        setupButtons();
    }

    private void displayOrderInfo() {
        tvPickupAddress.setText(order.getPickupAddress());
        tvDeliveryAddress.setText(order.getDeliveryAddress());
        tvWeight.setText(String.format("%.2f кг", order.getWeight()));
        tvDimensions.setText(String.format("%.0f x %.0f x %.0f см",
                order.getLength(), order.getWidth(), order.getHeight()));
        tvPrice.setText(String.format("%.2f руб.", order.getPrice()));

        double deposit = order.getPrice() / 2;
        tvDeposit.setText(String.format("%.2f руб.", deposit));

        tvStatus.setText(order.getStatusText());
        updateStatusColor();

        if (order.getCreatedAt() != null) {
            tvCreatedAt.setText(order.getCreatedAt());
        }

        if (userType.equals("client")) {
            if (order.getCourierName() != null && !order.getCourierName().isEmpty()) {
                tvCourierInfo.setVisibility(View.VISIBLE);
                tvCourierInfo.setText(order.getCourierName() + " | Тел: " + order.getCourierPhone());
            } else {
                tvCourierInfo.setVisibility(View.GONE);
            }
            tvClientInfo.setVisibility(View.GONE);
        } else {
            tvClientInfo.setVisibility(View.VISIBLE);
            tvClientInfo.setText(order.getClientName() + " | Тел: " + order.getClientPhone());
            tvCourierInfo.setVisibility(View.GONE);
        }
    }

    private void updateStatusColor() {
        int status = order.getStatus();
        if (status == 1) {
            tvStatus.setTextColor(0xFFF44336);
        } else if (status == 2) {
            tvStatus.setTextColor(0xFFFF9800);
        } else if (status == 3) {
            tvStatus.setTextColor(0xFF2196F3);
        } else if (status == 4) {
            tvStatus.setTextColor(0xFF4CAF50);
        } else if (status == 5) {
            tvStatus.setTextColor(0xFF4CAF50);
        } else if (status == 6) {
            tvStatus.setTextColor(0xFF9C27B0);
        } else {
            tvStatus.setTextColor(0xFF9E9E9E);
        }
    }

    private void setupButtons() {
        if (userType.equals("courier")) {
            clientButtons.setVisibility(View.GONE);
            btnDeliverOrder.setVisibility(View.GONE);

            if (order.getStatus() == 2) {
                btnPickupOrder.setVisibility(View.VISIBLE);
                btnPickupOrder.setEnabled(true);
                btnPickupOrder.setText("Забрал заказ");
                btnPickupOrder.setOnClickListener(v -> showPickupConfirmation());
                btnCompleteOrder.setVisibility(View.GONE);
            }
            else if (order.getStatus() == 4) {
                btnPickupOrder.setVisibility(View.GONE);
                btnCompleteOrder.setVisibility(View.VISIBLE);
                btnCompleteOrder.setEnabled(true);
                btnCompleteOrder.setText("Доставил заказ");
                btnCompleteOrder.setOnClickListener(v -> showCompleteConfirmation());
            }
            else {
                btnPickupOrder.setVisibility(View.GONE);
                btnCompleteOrder.setVisibility(View.GONE);
            }
        }
        else { // клиент
            clientButtons.setVisibility(View.VISIBLE);
            btnPickupOrder.setVisibility(View.GONE);
            btnCompleteOrder.setVisibility(View.GONE);
            
            // Показать кнопки Редактировать и Отменить только если заказ в статусе 1 (новый)
            if (order.getStatus() == 1) {
                btnEditOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnEditOrder.setOnClickListener(v -> editOrder());
                btnCancelOrder.setOnClickListener(v -> showCancelConfirmation());
                btnDeliverOrder.setVisibility(View.GONE);
            }
            else if (order.getStatus() == 6) {
                btnEditOrder.setVisibility(View.GONE);
                btnCancelOrder.setVisibility(View.GONE);
                btnDeliverOrder.setVisibility(View.GONE);
            }
            else if (order.getStatus() == 3) {
                btnEditOrder.setVisibility(View.GONE);
                btnCancelOrder.setVisibility(View.GONE);
                btnDeliverOrder.setVisibility(View.VISIBLE);
                btnDeliverOrder.setEnabled(true);
                btnDeliverOrder.setText("Отдал заказ");
                btnDeliverOrder.setOnClickListener(v -> showDeliverConfirmation());
            } 
            else {
                btnEditOrder.setVisibility(View.GONE);
                btnCancelOrder.setVisibility(View.GONE);
                btnDeliverOrder.setVisibility(View.VISIBLE);
                btnDeliverOrder.setEnabled(false);
                btnDeliverOrder.setText("Отдал заказ");
                btnDeliverOrder.setAlpha(0.5f);
            }
        }
    }

    private void showPickupConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение");
        builder.setMessage("Вы подтверждаете, что забрали заказ у клиента?");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateOrderStatus(3); // Статус 3 = "Отдайте заказ курьеру"
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showDeliverConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение");
        builder.setMessage("Подтверждаете, что передали заказ курьеру?");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateOrderStatus(4); // Статус 4 = "В пути"
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showCompleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение");
        builder.setMessage("Вы подтверждаете, что доставили заказ получателю?");
        builder.setPositiveButton("Да", (dialog, which) -> updateOrderStatus(5));
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void updateOrderStatus(final int newStatus) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (userType.equals("courier") && newStatus == 5) {
                        CompleteOrderUsecase completeUsecase = new CompleteOrderUsecase();
                        completeUsecase.execute(order.getId(), order.getClientId(), currentUser.getId(), order.getPrice());
                    } else {
                        UpdateOrderStatusUsecase statusUsecase = new UpdateOrderStatusUsecase();
                        statusUsecase.execute(order.getId(), newStatus);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            order.setStatus(newStatus);
                            displayOrderInfo();
                            setupButtons();
                            Toast.makeText(OrderDetailActivity.this, "Статус обновлён", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                        }
                    });
                } catch (RuntimeException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void editOrder() {
        Intent intent = new Intent(OrderDetailActivity.this, CreateOrderActivity.class);
        intent.putExtra("user", currentUser);
        intent.putExtra("order", order);
        startActivityForResult(intent, 1);
    }

    private void showCancelConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение отмены");
        builder.setMessage("Вы уверены, что хотите отменить заказ? Статус будет изменён на \"Отменён\".");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelOrder();
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void cancelOrder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CancelOrderUsecase cancelUsecase = new CancelOrderUsecase();
                    cancelUsecase.execute(order.getId());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderDetailActivity.this, "Заказ отменён", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    });
                } catch (RuntimeException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Обновить информацию о заказе после редактирования
            displayOrderInfo();
            setupButtons();
        }
    }
}