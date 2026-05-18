package com.example.courierapp.domain.entity;

public class Client extends User {
    private String address;

    public Client(String fullName, String login, String phone, String password, String address) {
        super(fullName, login, phone, password);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getUserType() {
        return "Клиент";
    }
}
