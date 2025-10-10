package com.example.mapper;

import com.example.entity.VehicleData;
import org.apache.ibatis.annotations.*;

public interface VehicleDataMapper {

    /**
     * 创建表（如果不存在）
     */
    @Update("CREATE TABLE IF NOT EXISTS t_vehicle_data (" +
            "vin VARCHAR(17) NOT NULL COMMENT '车辆识别码(VIN)', " +
            "collect_time DATETIME DEFAULT NULL COMMENT '数据采集时间', " +
            "vehicle_status TINYINT UNSIGNED DEFAULT NULL COMMENT '车辆状态', " +
            "charge_status TINYINT UNSIGNED DEFAULT NULL COMMENT '充电状态', " +
            "run_mode TINYINT UNSIGNED DEFAULT NULL COMMENT '运行模式', " +
            "speed DECIMAL(5,1) DEFAULT NULL COMMENT '车速(km/h)', " +
            "mileage DECIMAL(8,1) DEFAULT NULL COMMENT '累计里程(km)', " +
            "total_voltage DECIMAL(6,1) DEFAULT NULL COMMENT '总电压(V)', " +
            "total_current DECIMAL(7,1) DEFAULT NULL COMMENT '总电流(A)', " +
            "soc TINYINT UNSIGNED DEFAULT NULL COMMENT 'SOC(%)', " +
            "dc_dc_status TINYINT UNSIGNED DEFAULT NULL COMMENT 'DC-DC状态', " +
            "gear VARCHAR(4) DEFAULT NULL COMMENT '挡位信息', " +
            "insulation_resistance SMALLINT UNSIGNED DEFAULT NULL COMMENT '绝缘电阻(kΩ)', " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
            "UNIQUE KEY uk_vin (vin)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GB32960整车数据表'")
    void createTable();

    /**
     * 插入或更新整车数据
     */
    @Insert("INSERT INTO t_vehicle_data (" +
            "vin, collect_time, vehicle_status, charge_status, run_mode, speed, mileage, " +
            "total_voltage, total_current, soc, dc_dc_status, gear, insulation_resistance" +
            ") VALUES (" +
            "#{vin}, #{collectTime}, #{vehicleStatus}, #{chargeStatus}, #{runMode}, " +
            "#{speed}, #{mileage}, #{totalVoltage}, #{totalCurrent}, #{soc}, " +
            "#{dcDcStatus}, #{gear}, #{insulationResistance}" +
            ") ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), " +
            "vehicle_status = VALUES(vehicle_status), " +
            "charge_status = VALUES(charge_status), " +
            "run_mode = VALUES(run_mode), " +
            "speed = VALUES(speed), " +
            "mileage = VALUES(mileage), " +
            "total_voltage = VALUES(total_voltage), " +
            "total_current = VALUES(total_current), " +
            "soc = VALUES(soc), " +
            "dc_dc_status = VALUES(dc_dc_status), " +
            "gear = VALUES(gear), " +
            "insulation_resistance = VALUES(insulation_resistance), " +
            "updated_at = CURRENT_TIMESTAMP")
    int insertOrUpdate(VehicleData vehicleData);
}
