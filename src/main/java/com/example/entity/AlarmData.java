package com.example.entity;

import java.time.LocalDateTime;

public class AlarmData {
    private int id;
    private String vin;
    private LocalDateTime collectTime;
    private Integer maxAlarmLevel;
    private String alarmTypes;
    private Integer energyStorageFaultCount;
    private String energyStorageFaults;
    private Integer motorFaultCount;
    private String motorFaults;
    private Integer engineFaultCount;
    private String engineFaults;
    private Integer otherFaultCount;
    private String otherFaults;
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

    public Integer getMaxAlarmLevel() {
        return maxAlarmLevel;
    }

    public void setMaxAlarmLevel(Integer maxAlarmLevel) {
        this.maxAlarmLevel = maxAlarmLevel;
    }

    public String getAlarmTypes() {
        return alarmTypes;
    }

    public void setAlarmTypes(String alarmTypes) {
        this.alarmTypes = alarmTypes;
    }

    public Integer getEnergyStorageFaultCount() {
        return energyStorageFaultCount;
    }

    public void setEnergyStorageFaultCount(Integer energyStorageFaultCount) {
        this.energyStorageFaultCount = energyStorageFaultCount;
    }

    public String getEnergyStorageFaults() {
        return energyStorageFaults;
    }

    public void setEnergyStorageFaults(String energyStorageFaults) {
        this.energyStorageFaults = energyStorageFaults;
    }

    public Integer getMotorFaultCount() {
        return motorFaultCount;
    }

    public void setMotorFaultCount(Integer motorFaultCount) {
        this.motorFaultCount = motorFaultCount;
    }

    public String getMotorFaults() {
        return motorFaults;
    }

    public void setMotorFaults(String motorFaults) {
        this.motorFaults = motorFaults;
    }

    public Integer getEngineFaultCount() {
        return engineFaultCount;
    }

    public void setEngineFaultCount(Integer engineFaultCount) {
        this.engineFaultCount = engineFaultCount;
    }

    public String getEngineFaults() {
        return engineFaults;
    }

    public void setEngineFaults(String engineFaults) {
        this.engineFaults = engineFaults;
    }

    public Integer getOtherFaultCount() {
        return otherFaultCount;
    }

    public void setOtherFaultCount(Integer otherFaultCount) {
        this.otherFaultCount = otherFaultCount;
    }

    public String getOtherFaults() {
        return otherFaults;
    }

    public void setOtherFaults(String otherFaults) {
        this.otherFaults = otherFaults;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}