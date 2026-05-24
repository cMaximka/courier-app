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
    private Button btnConfirmDelivery;

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
        btnConfirmDelivery = findViewById(R.id.btn_confirm_delivery);

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
        } else if (status == 7) {
            tvStatus.setTextColor(0xFFFF9800);
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
                btnPickupOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPickupConfirmation();
                    }
                });
                btnCompleteOrder.setVisibility(View.GONE);
            } else if (order.getStatus() == 4) {
                btnPickupOrder.setVisibility(View.GONE);
                btnCompleteOrder.setVisibility(View.VISIBLE);
                btnCompleteOrder.setEnabled(true);
                btnCompleteOrder.setText("Доставил заказ");
                btnCompleteOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showCompleteConfirmation();
                    }
                });
            } else {
                btnPickupOrder.setVisibility(View.GONE);
                btnCompleteOrder.setVisibility(View.GONE);
            }
        } else {
            clientButtons.setVisibility(View.VISIBLE);
            btnPickupOrder.setVisibility(View.GONE);
            btnCompleteOrder.setVisibility(View.GONE);

            int status = order.getStatus();
            boolean canCancel = (status != 5 && status != 6);
            boolean canEdit = (status < 3);

            if (canEdit) {
                btnEditOrder.setVisibility(View.VISIBLE);
                btnEditOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editOrder();
                    }
                });
            } else {
                btnEditOrder.setVisibility(View.GONE);
            }

            if (canCancel) {
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showCancelConfirmation();
                    }
                });
            } else {
                btnCancelOrder.setVisibility(View.GONE);
            }

            // Other client actions depending on status
            if (status == 3) {
                btnDeliverOrder.setVisibility(View.VISIBLE);
                btnDeliverOrder.setEnabled(true);
                btnDeliverOrder.setText("Отдал заказ");
                btnDeliverOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeliverConfirmation();
                    }
                });
                btnConfirmDelivery.setVisibility(View.GONE);
            } else if (status == 7) {
                btnDeliverOrder.setVisibility(View.GONE);
                btnConfirmDelivery.setVisibility(View.VISIBLE);
                btnConfirmDelivery.setEnabled(true);
                btnConfirmDelivery.setText("Подтвердить получение");
                btnConfirmDelivery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDelivery();
                    }
                });
            } else {
                btnDeliverOrder.setVisibility(View.GONE);
                btnDeliverOrder.setEnabled(false);
                btnConfirmDelivery.setVisibility(View.GONE);
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
                updateOrderStatus(3);
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
                updateOrderStatus(4);
            }
        });
        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void showCompleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подтверждение");
        builder.setMessage("Вы доставили заказ получателю? После этого клиент должен будет подтвердить получение, и только тогда средства поступят на ваш счёт.");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateOrderStatus(7);
            }
        });
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

    private void confirmDelivery() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CompleteOrderUsecase completeUsecase = new CompleteOrderUsecase();
                    completeUsecase.execute(order.getId(), order.getClientId(), order.getCourierId(), order.getPrice());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderDetailActivity.this, "Доставка подтверждена, средства списаны", Toast.LENGTH_LONG).show();
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
            displayOrderInfo();
            setupButtons();
        }
    }
}