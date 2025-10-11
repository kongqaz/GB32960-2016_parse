package com.example.entity;

import java.time.LocalDateTime;

public class EngineData {
    private int id;
    private String vin;
    private LocalDateTime collectTime;
    private int engineStatus;
    private int crankshaftSpeed;
    private String fuelConsumption;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(int engineStatus) {
        this.engineStatus = engineStatus;
    }

    public int getCrankshaftSpeed() {
        return crankshaftSpeed;
    }

    public void setCrankshaftSpeed(int crankshaftSpeed) {
        this.crankshaftSpeed = crankshaftSpeed;
    }

    public String getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(String fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }
}
