package com.example.entity;

import java.time.LocalDateTime;

public class VehicleLoginLogout {
    private Long id;
    private String vin;
    private Integer loginSerialNo;
    private LocalDateTime loginTime;
    private Integer logoutSerialNo;
    private LocalDateTime logoutTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public Integer getLoginSerialNo() { return loginSerialNo; }
    public void setLoginSerialNo(Integer loginSerialNo) { this.loginSerialNo = loginSerialNo; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public Integer getLogoutSerialNo() { return logoutSerialNo; }
    public void setLogoutSerialNo(Integer logoutSerialNo) { this.logoutSerialNo = logoutSerialNo; }

    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
}
