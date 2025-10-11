package com.example.entity;

import java.time.LocalDateTime;

public class ExtremeData {
    private int id;
    private String vin;
    private LocalDateTime collectTime;
    private Integer highestVoltageSystemNo;
    private Integer highestVoltageCellNo;
    private String highestCellVoltage;
    private Integer lowestVoltageSystemNo;
    private Integer lowestVoltageCellNo;
    private String lowestCellVoltage;
    private Integer highestTempSystemNo;
    private Integer highestTempProbeNo;
    private Integer highestTempValue;
    private Integer lowestTempSystemNo;
    private Integer lowestTempProbeNo;
    private Integer lowestTempValue;
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

    public Integer getHighestVoltageSystemNo() {
        return highestVoltageSystemNo;
    }

    public void setHighestVoltageSystemNo(Integer highestVoltageSystemNo) {
        this.highestVoltageSystemNo = highestVoltageSystemNo;
    }

    public Integer getHighestVoltageCellNo() {
        return highestVoltageCellNo;
    }

    public void setHighestVoltageCellNo(Integer highestVoltageCellNo) {
        this.highestVoltageCellNo = highestVoltageCellNo;
    }

    public String getHighestCellVoltage() {
        return highestCellVoltage;
    }

    public void setHighestCellVoltage(String highestCellVoltage) {
        this.highestCellVoltage = highestCellVoltage;
    }

    public Integer getLowestVoltageSystemNo() {
        return lowestVoltageSystemNo;
    }

    public void setLowestVoltageSystemNo(Integer lowestVoltageSystemNo) {
        this.lowestVoltageSystemNo = lowestVoltageSystemNo;
    }

    public Integer getLowestVoltageCellNo() {
        return lowestVoltageCellNo;
    }

    public void setLowestVoltageCellNo(Integer lowestVoltageCellNo) {
        this.lowestVoltageCellNo = lowestVoltageCellNo;
    }

    public String getLowestCellVoltage() {
        return lowestCellVoltage;
    }

    public void setLowestCellVoltage(String lowestCellVoltage) {
        this.lowestCellVoltage = lowestCellVoltage;
    }

    public Integer getHighestTempSystemNo() {
        return highestTempSystemNo;
    }

    public void setHighestTempSystemNo(Integer highestTempSystemNo) {
        this.highestTempSystemNo = highestTempSystemNo;
    }

    public Integer getHighestTempProbeNo() {
        return highestTempProbeNo;
    }

    public void setHighestTempProbeNo(Integer highestTempProbeNo) {
        this.highestTempProbeNo = highestTempProbeNo;
    }

    public Integer getHighestTempValue() {
        return highestTempValue;
    }

    public void setHighestTempValue(Integer highestTempValue) {
        this.highestTempValue = highestTempValue;
    }

    public Integer getLowestTempSystemNo() {
        return lowestTempSystemNo;
    }

    public void setLowestTempSystemNo(Integer lowestTempSystemNo) {
        this.lowestTempSystemNo = lowestTempSystemNo;
    }

    public Integer getLowestTempProbeNo() {
        return lowestTempProbeNo;
    }

    public void setLowestTempProbeNo(Integer lowestTempProbeNo) {
        this.lowestTempProbeNo = lowestTempProbeNo;
    }

    public Integer getLowestTempValue() {
        return lowestTempValue;
    }

    public void setLowestTempValue(Integer lowestTempValue) {
        this.lowestTempValue = lowestTempValue;
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
}
