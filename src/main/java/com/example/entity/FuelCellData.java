package com.example.entity;
import java.time.LocalDateTime;

public class FuelCellData {
    private String vin;
    private LocalDateTime collectTime;
    private String fuelVoltage;
    private String fuelCurrent;
    private String fuelConsumption;
    private Integer probeCount;
    private String probeTemps;
    private LocalDateTime updatedAt;

    // Getters and Setters
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

    public String getFuelVoltage() {
        return fuelVoltage;
    }

    public void setFuelVoltage(String fuelVoltage) {
        this.fuelVoltage = fuelVoltage;
    }

    public String getFuelCurrent() {
        return fuelCurrent;
    }

    public void setFuelCurrent(String fuelCurrent) {
        this.fuelCurrent = fuelCurrent;
    }

    public String getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(String fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public Integer getProbeCount() {
        return probeCount;
    }

    public void setProbeCount(Integer probeCount) {
        this.probeCount = probeCount;
    }

    public String getProbeTemps() {
        return probeTemps;
    }

    public void setProbeTemps(String probeTemps) {
        this.probeTemps = probeTemps;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
