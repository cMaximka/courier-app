package com.example.courierapp.domain.entity;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String id;
    private String fullName;
    private String login;
    private String phone;
    private String password;

    private double balance;

    public User(String fullName, String login, String phone, String password) {
        this.fullName = fullName;
        this.login = login;
        this.phone = phone;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public abstract int getUserType();
}
