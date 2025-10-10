package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VehicleData {
    private int id;
    private String vin;
    private LocalDateTime collectTime;
    private Integer vehicleStatus;
    private Integer chargeStatus;
    private Integer runMode;
    private String speed;
    private String mileage;
    private String totalVoltage;
    private String totalCurrent;
    private Integer soc;
    private Integer dcDcStatus;
    private String gear;
    private Integer insulationResistance;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public LocalDateTime getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(LocalDateTime collectTime) {
        this.collectTime = collectTime;
    }

    public Integer getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(Integer vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public Integer getChargeStatus() {
        return chargeStatus;
    }

    public void setChargeStatus(Integer chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    public Integer getRunMode() {
        return runMode;
    }

    public void setRunMode(Integer runMode) {
        this.runMode = runMode;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getTotalVoltage() {
        return totalVoltage;
    }

    public void setTotalVoltage(String totalVoltage) {
        this.totalVoltage = totalVoltage;
    }

    public String getTotalCurrent() {
        return totalCurrent;
    }

    public void setTotalCurrent(String totalCurrent) {
        this.totalCurrent = totalCurrent;
    }

    public Integer getSoc() {
        return soc;
    }

    public void setSoc(Integer soc) {
        this.soc = soc;
    }

    public Integer getDcDcStatus() {
        return dcDcStatus;
    }

    public void setDcDcStatus(Integer dcDcStatus) {
        this.dcDcStatus = dcDcStatus;
    }

    public String getGear() {
        return gear;
    }

    public void setGear(String gear) {
        this.gear = gear;
    }

    public Integer getInsulationResistance() {
        return insulationResistance;
    }

    public void setInsulationResistance(Integer insulationResistance) {
        this.insulationResistance = insulationResistance;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
