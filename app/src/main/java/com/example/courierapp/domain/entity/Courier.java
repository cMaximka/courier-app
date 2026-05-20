package com.example.courierapp.domain.entity;

public class Courier extends User {
    private String passportData;
    private String driverLicense;

    public Courier(String fullName, String login, String phone, String password, String passportData, String driverLicense) {
        super(fullName, login, phone, password);
        this.passportData = passportData;
        this.driverLicense = driverLicense;
    }

    public String getPassportData() {
        return passportData;
    }

    public void setPassportData(String passportData) {
        this.passportData = passportData;
    }

    public String getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    @Override
    public int getUserType() {
        return 2;
    }
}
