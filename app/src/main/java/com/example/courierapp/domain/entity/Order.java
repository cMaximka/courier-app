package com.example.courierapp.domain.entity;

import java.io.Serializable;

public class Order implements Serializable {
    private String id;
    private String clientId;
    private String courierId;
    private String pickupAddress;
    private String deliveryAddress;
    private int status; // 1 = Ожидает курьера, 2 = В пути (принят курьером), 3 = Доставлен
    private double price;
    private String createdAt;

    // Информация о клиенте (для курьера) - только для отображения
    private String clientName;
    private String clientPhone;

    // Информация о курьере (для клиента) - только для отображения
    private String courierName;
    private String courierPhone;

    // Поля для расчета цены
    private double weight;
    private double length;
    private double width;
    private double height;
    private double productPrice;

    public Order(String clientId, String pickupAddress, String deliveryAddress,
                 double weight, double length, double width, double height, double productPrice) {
        this.clientId = clientId;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
        this.productPrice = productPrice;
        this.status = 1;
        this.price = 0; // Цена устанавливается через setPrice() из CreateOrderUsecase
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getCourierId() { return courierId; }
    public void setCourierId(String courierId) { this.courierId = courierId; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public int getStatus() { return status; }

    public String getStatusText() {
        switch (status) {
            case 1: return "Курьер не назначен";
            case 2: return "Ожидайте курьера";
            case 3: return "Отдайте заказ курьеру";
            case 4: return "В пути";
            case 5: return "Доставлен";         
            default: return "Неизвестно";
        }
    }

    public void setStatus(int status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getCourierPhone() { return courierPhone; }
    public void setCourierPhone(String courierPhone) { this.courierPhone = courierPhone; }
}