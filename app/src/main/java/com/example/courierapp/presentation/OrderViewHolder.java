package com.example.courierapp.presentation;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courierapp.R;
import com.example.courierapp.domain.entity.Order;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderViewHolder extends RecyclerView.ViewHolder {
    private TextView tvClientInfo;
    private TextView tvCourierInfo;
    private TextView tvPickupAddress;
    private TextView tvDeliveryAddress;
    private TextView tvWeight;
    private TextView tvDimensions;
    private TextView tvPrice;
    private TextView tvDeposit;
    private TextView tvStatus;
    private TextView tvCreatedAt;
    private LinearLayout llButtons;
    private Button btnAccept;
    private Button btnCancel;
    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        tvClientInfo = itemView.findViewById(R.id.tv_client_info);
        tvCourierInfo = itemView.findViewById(R.id.tv_courier_info);
        tvPickupAddress = itemView.findViewById(R.id.tv_pickup_address);
        tvDeliveryAddress = itemView.findViewById(R.id.tv_delivery_address);
        tvWeight = itemView.findViewById(R.id.tv_weight);
        tvDimensions = itemView.findViewById(R.id.tv_dimensions);
        tvPrice = itemView.findViewById(R.id.tv_price);
        tvDeposit = itemView.findViewById(R.id.tv_deposit);
        tvStatus = itemView.findViewById(R.id.tv_status);
        tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
        llButtons = itemView.findViewById(R.id.ll_buttons);
        btnAccept = itemView.findViewById(R.id.btn_accept);
        btnCancel = itemView.findViewById(R.id.btn_cancel);
    }

    public void bind(Order order, OrderAdapter.OnOrderActionListener actionListener, int mode) {
        tvPickupAddress.setText("Откуда: " + order.getPickupAddress());
        tvDeliveryAddress.setText("Куда: " + order.getDeliveryAddress());
        tvWeight.setText(String.format("Вес: %.2f кг", order.getWeight()));
        tvDimensions.setText(String.format("Габариты: %.0f x %.0f x %.0f см",
                order.getLength(), order.getWidth(), order.getHeight()));
        tvPrice.setText(String.format("Стоимость: %.2f руб", order.getPrice()));

        double deposit = order.getPrice() / 2;
        tvDeposit.setText(String.format("Взнос за заказ: %.2f руб.", deposit));

        tvStatus.setText("Статус: " + order.getStatusText());
        updateStatusColor(order.getStatus());

        if (order.getCreatedAt() != null) {
            tvCreatedAt.setText("Создан: " + order.getCreatedAt());
        }

        if (mode == 0) {
            tvClientInfo.setVisibility(View.GONE);
            tvDeposit.setVisibility(View.GONE);

            if (order.getCourierName() != null && !order.getCourierName().isEmpty()) {
                tvCourierInfo.setText("Курьер: " + order.getCourierName() + " | Тел: " + order.getCourierPhone());
                tvCourierInfo.setVisibility(View.VISIBLE);
            } else {
                tvCourierInfo.setVisibility(View.GONE);
            }
            llButtons.setVisibility(View.GONE);

        } else if (mode == 1) {
            tvCourierInfo.setVisibility(View.GONE);
            tvClientInfo.setVisibility(View.GONE);
            tvDeposit.setVisibility(View.VISIBLE);

            llButtons.setVisibility(View.VISIBLE);
            btnAccept.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        actionListener.onAcceptClick(order);
                    }
                }
            });

        } else if (mode == 2) {
            tvCourierInfo.setVisibility(View.GONE);
            tvDeposit.setVisibility(View.VISIBLE);

            if (order.getClientName() != null && !order.getClientName().isEmpty()) {
                tvClientInfo.setText("Клиент: " + order.getClientName() + " | Тел: " + order.getClientPhone());
                tvClientInfo.setVisibility(View.VISIBLE);
            } else {
                tvClientInfo.setVisibility(View.GONE);
            }

            llButtons.setVisibility(View.VISIBLE);
            btnAccept.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (actionListener != null) {
                        actionListener.onCancelClick(order);
                    }
                }
            });
        }
    }

    private void updateStatusColor(int status) {
        switch (status) {
            case 1:
                tvStatus.setTextColor(0xFFF44336);
                break;
            case 2:
                tvStatus.setTextColor(0xFFFF9800);
                break;
            case 3:
                tvStatus.setTextColor(0xFF2196F3);
                break;
            case 4:
                tvStatus.setTextColor(0xFF4CAF50);
                break;
            case 5:
                tvStatus.setTextColor(0xFF607D8B);
                break;
            case 7:
                tvStatus.setTextColor(0xFFFF9800);
                break;
            default:
                tvStatus.setTextColor(0xFF9E9E9E);
                break;
        }
    }
}
