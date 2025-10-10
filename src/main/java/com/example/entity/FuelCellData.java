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
    private String h2MaxTemp;
    private Integer h2MaxTempProbeCode;
    private Integer h2MaxConcentration;
    private Integer h2MaxConcentrationSensorCode;
    private String h2MaxPressure;
    private Integer h2MaxPressureSensorCode;
    private Integer highVoltageDcDcStatus;
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

    public String getH2MaxTemp() {
        return h2MaxTemp;
    }

    public void setH2MaxTemp(String h2MaxTemp) {
        this.h2MaxTemp = h2MaxTemp;
    }

    public Integer getH2MaxTempProbeCode() {
        return h2MaxTempProbeCode;
    }

    public void setH2MaxTempProbeCode(Integer h2MaxTempProbeCode) {
        this.h2MaxTempProbeCode = h2MaxTempProbeCode;
    }

    public Integer getH2MaxConcentration() {
        return h2MaxConcentration;
    }

    public void setH2MaxConcentration(Integer h2MaxConcentration) {
        this.h2MaxConcentration = h2MaxConcentration;
    }

    public Integer getH2MaxConcentrationSensorCode() {
        return h2MaxConcentrationSensorCode;
    }

    public void setH2MaxConcentrationSensorCode(Integer h2MaxConcentrationSensorCode) {
        this.h2MaxConcentrationSensorCode = h2MaxConcentrationSensorCode;
    }

    public String getH2MaxPressure() {
        return h2MaxPressure;
    }

    public void setH2MaxPressure(String h2MaxPressure) {
        this.h2MaxPressure = h2MaxPressure;
    }

    public Integer getH2MaxPressureSensorCode() {
        return h2MaxPressureSensorCode;
    }

    public void setH2MaxPressureSensorCode(Integer h2MaxPressureSensorCode) {
        this.h2MaxPressureSensorCode = h2MaxPressureSensorCode;
    }

    public Integer getHighVoltageDcDcStatus() {
        return highVoltageDcDcStatus;
    }

    public void setHighVoltageDcDcStatus(Integer highVoltageDcDcStatus) {
        this.highVoltageDcDcStatus = highVoltageDcDcStatus;
    }
}
