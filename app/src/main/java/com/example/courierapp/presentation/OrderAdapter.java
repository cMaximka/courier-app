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
    private int mode; // 0 - для клиента, 1 - для курьера (доступные), 2 - для курьера (мои)
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

        // Обработчик клика по карточке
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
            // Основная информация о заказе
            tvPickupAddress.setText("Откуда: " + order.getPickupAddress());
            tvDeliveryAddress.setText("Куда: " + order.getDeliveryAddress());
            tvWeight.setText(String.format("Вес: %.2f кг", order.getWeight()));
            tvDimensions.setText(String.format("Габариты: %.0f x %.0f x %.0f см",
                    order.getLength(), order.getWidth(), order.getHeight()));
            tvPrice.setText(String.format("Стоимость: %.2f руб", order.getPrice()));

            // Отображение взноса (половина стоимости)
            double deposit = order.getPrice() / 2;
            tvDeposit.setText(String.format("Взнос за заказ: %.2f руб.", deposit));

            // Статус заказа
            tvStatus.setText("Статус: " + order.getStatusText());
            updateStatusColor(order.getStatus());

            // Дата создания
            if (order.getCreatedAt() != null) {
                tvCreatedAt.setText("Создан: " + order.getCreatedAt());
            }

            // Настройка отображения в зависимости от режима
            if (mode == 0) {
                // РЕЖИМ КЛИЕНТА - показываем только информацию о курьере
                tvClientInfo.setVisibility(View.GONE);
                tvDeposit.setVisibility(View.GONE); // Клиенту не показываем взнос

                if (order.getCourierName() != null && !order.getCourierName().isEmpty()) {
                    tvCourierInfo.setText("Курьер: " + order.getCourierName() + " | Тел: " + order.getCourierPhone());
                    tvCourierInfo.setVisibility(View.VISIBLE);
                } else {
                    tvCourierInfo.setVisibility(View.GONE);
                }
                llButtons.setVisibility(View.GONE);

            } else if (mode == 1) {
                // РЕЖИМ КУРЬЕРА - Доступные заказы
                // НЕ показываем информацию о клиенте
                tvCourierInfo.setVisibility(View.GONE);
                tvClientInfo.setVisibility(View.GONE); // Скрываем информацию о клиенте
                tvDeposit.setVisibility(View.VISIBLE); // Показываем взнос курьеру

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
                // РЕЖИМ КУРЬЕРА - Мои заказы (принятые)
                // Показываем информацию о клиенте
                tvCourierInfo.setVisibility(View.GONE);
                tvDeposit.setVisibility(View.VISIBLE); // Показываем взнос курьеру

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
                case 1: // Курьер не назначен
                    tvStatus.setTextColor(0xFFF44336); // Красный
                    break;
                case 2: // Ожидайте курьера
                    tvStatus.setTextColor(0xFFFF9800); // Оранжевый
                    break;
                case 3: // Отдайте заказ курьеру
                    tvStatus.setTextColor(0xFF2196F3); // Синий
                    break;
                case 4: // В пути
                    tvStatus.setTextColor(0xFF4CAF50); // Зелёный
                    break;
                default:
                    tvStatus.setTextColor(0xFF9E9E9E); // Серый
                    break;
            }
        }
    }
}