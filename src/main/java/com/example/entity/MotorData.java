// 文件路径: src/main/java/com/example/entity/MotorData.java
package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MotorData {
    private String vin;
    private LocalDateTime collectTime;
    private Integer motorSeq;
    private Integer motorStatus;
    private Integer controllerTemp;
    private Integer motorSpeed;
    private String motorTorque;
    private Integer motorTemp;
    private String controllerVoltage;
    private String controllerCurrent;
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

    public Integer getMotorSeq() {
        return motorSeq;
    }

    public void setMotorSeq(Integer motorSeq) {
        this.motorSeq = motorSeq;
    }

    public Integer getMotorStatus() {
        return motorStatus;
    }

    public void setMotorStatus(Integer motorStatus) {
        this.motorStatus = motorStatus;
    }

    public Integer getControllerTemp() {
        return controllerTemp;
    }

    public void setControllerTemp(Integer controllerTemp) {
        this.controllerTemp = controllerTemp;
    }

    public Integer getMotorSpeed() {
        return motorSpeed;
    }

    public void setMotorSpeed(Integer motorSpeed) {
        this.motorSpeed = motorSpeed;
    }

    public String getMotorTorque() {
        return motorTorque;
    }

    public void setMotorTorque(String motorTorque) {
        this.motorTorque = motorTorque;
    }

    public Integer getMotorTemp() {
        return motorTemp;
    }

    public void setMotorTemp(Integer motorTemp) {
        this.motorTemp = motorTemp;
    }

    public String getControllerVoltage() {
        return controllerVoltage;
    }

    public void setControllerVoltage(String controllerVoltage) {
        this.controllerVoltage = controllerVoltage;
    }

    public String getControllerCurrent() {
        return controllerCurrent;
    }

    public void setControllerCurrent(String controllerCurrent) {
        this.controllerCurrent = controllerCurrent;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
