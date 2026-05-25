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

public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

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
        holder.bind(order, actionListener, mode);
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
}