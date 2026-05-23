package com.example.courierapp.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courierapp.R;
import com.example.courierapp.domain.entity.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders;
    private OnOrderActionListener actionListener;
    private int mode;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnOrderActionListener {
        void onAcceptClick(Order order);
        void onCancelClick(Order order);
        void onItemClick(Order order);
    }

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
        this.actionListener = null;
        this.mode = 0;
    }

    public OrderAdapter(List<Order> orders, OnOrderActionListener listener, int mode) {
        this.orders = orders;
        this.actionListener = listener;
        this.mode = mode;
    }

    public void updateMode(int newMode) {
        this.mode = newMode;
        notifyDataSetChanged();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionListener != null) {
                    actionListener.onItemClick(order);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
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

        public void bind(Order order) {
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
                default:
                    tvStatus.setTextColor(0xFF9E9E9E); 
                    break;
            }
        }
    }
}