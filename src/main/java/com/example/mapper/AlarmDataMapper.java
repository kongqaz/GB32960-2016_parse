package com.example.mapper;

import com.example.entity.AlarmData;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

public interface AlarmDataMapper {

    /**
     * 创建报警数据表（如果不存在）
     */
    @Update("CREATE TABLE IF NOT EXISTS t_alarm_data (" +
            "id INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号', " +
            "vin VARCHAR(17) NOT NULL COMMENT 'VIN号', " +
            "collect_time DATETIME DEFAULT NULL COMMENT '数据采集时间', " +
            "max_alarm_level TINYINT UNSIGNED DEFAULT NULL COMMENT '最高报警等级：范围:0~3，0=无故障，1=1级故障，2=2级故障，3=3级故障，0xFE=异常，0xFF=无效', " +
            "alarm_types TINYTEXT DEFAULT NULL COMMENT '报警类型：温度差异报警、电池高温报警等', " +
            "energy_storage_fault_count TINYINT UNSIGNED DEFAULT NULL COMMENT '可充电储能装置故障总数', " +
            "energy_storage_faults TEXT DEFAULT NULL COMMENT '可充电储能装置故障代码的数组，JSON格式', " +
            "motor_fault_count TINYINT UNSIGNED DEFAULT NULL COMMENT '驱动电机故障总数', " +
            "motor_faults TEXT DEFAULT NULL COMMENT '驱动电机故障代码的数组，JSON格式', " +
            "engine_fault_count TINYINT UNSIGNED DEFAULT NULL COMMENT '发动机故障总数', " +
            "engine_faults TEXT DEFAULT NULL COMMENT '发动机故障代码的数组，JSON格式', " +
            "other_fault_count TINYINT UNSIGNED DEFAULT NULL COMMENT '其他故障总数', " +
            "other_faults TEXT DEFAULT NULL COMMENT '其他故障代码的数组，JSON格式', " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
            "UNIQUE KEY `uk_vin` (`vin`), " +
            "INDEX `idx_vin_collect_time` (`vin`, `collect_time`) " +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警数据表'")
    void createTable();

    /**
     * 检查是否存在更新的记录
     */
    @Select("SELECT COUNT(*) FROM t_alarm_data WHERE vin = #{vin} AND collect_time >= #{collectTime}")
    int existsNewerRecord(@Param("vin") String vin, @Param("collectTime") LocalDateTime collectTime);

    /**
     * 插入或更新报警数据
     */
    @Insert("INSERT INTO t_alarm_data (" +
            "vin, collect_time, max_alarm_level, alarm_types, energy_storage_fault_count, " +
            "energy_storage_faults, motor_fault_count, motor_faults, engine_fault_count, " +
            "engine_faults, other_fault_count, other_faults" +
            ") VALUES (" +
            "#{vin}, #{collectTime}, #{maxAlarmLevel}, #{alarmTypes}, #{energyStorageFaultCount}, " +
            "#{energyStorageFaults}, #{motorFaultCount}, #{motorFaults}, #{engineFaultCount}, " +
            "#{engineFaults}, #{otherFaultCount}, #{otherFaults}" +
            ") ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), " +
            "max_alarm_level = VALUES(max_alarm_level), " +
            "alarm_types = VALUES(alarm_types), " +
            "energy_storage_fault_count = VALUES(energy_storage_fault_count), " +
            "energy_storage_faults = VALUES(energy_storage_faults), " +
            "motor_fault_count = VALUES(motor_fault_count), " +
            "motor_faults = VALUES(motor_faults), " +
            "engine_fault_count = VALUES(engine_fault_count), " +
            "engine_faults = VALUES(engine_faults), " +
            "other_fault_count = VALUES(other_fault_count), " +
            "other_faults = VALUES(other_faults), " +
            "updated_at = CURRENT_TIMESTAMP")
    int insertOrUpdate(AlarmData alarmData);
}