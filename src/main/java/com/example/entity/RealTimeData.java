package com.example.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RealTimeData {
    private Long id;
    private String vin;
    private String jsonData;
    private String updateTime;

    // 构造函数
    public RealTimeData() {}

    public RealTimeData(String vin, String jsonData) {
        this.vin = vin;
        this.jsonData = jsonData;
//        this.updateTime = System.currentTimeMillis();
//        this.updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Getter和Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getJsonData() { return jsonData; }
    public void setJsonData(String jsonData) { this.jsonData = jsonData; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
