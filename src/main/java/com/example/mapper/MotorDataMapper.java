package com.example.mapper;

import com.example.entity.MotorData;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface MotorDataMapper {
    /**
     * 插入或更新驱动电机数据
     */
    @Insert("<script>" +
            "INSERT INTO t_motor_data (" +
            "vin, collect_time, motor_seq, motor_status, controller_temp, motor_speed, " +
            "motor_torque, motor_temp, controller_voltage, controller_current" +
            ") VALUES " +
            "<foreach collection='motorDataList' item='motorData' separator=','>" +
            "(#{motorData.vin}, #{motorData.collectTime}, #{motorData.motorSeq}, " +
            "#{motorData.motorStatus}, #{motorData.controllerTemp}, #{motorData.motorSpeed}, " +
            "#{motorData.motorTorque}, #{motorData.motorTemp}, #{motorData.controllerVoltage}, " +
            "#{motorData.controllerCurrent})" +
            "</foreach>" +
            " ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), " +
            "motor_status = VALUES(motor_status), " +
            "controller_temp = VALUES(controller_temp), " +
            "motor_speed = VALUES(motor_speed), " +
            "motor_torque = VALUES(motor_torque), " +
            "motor_temp = VALUES(motor_temp), " +
            "controller_voltage = VALUES(controller_voltage), " +
            "controller_current = VALUES(controller_current), " +
            "updated_at = CURRENT_TIMESTAMP" +
            "</script>")
    int insertOrUpdate(@Param("motorDataList") List<MotorData> motorDataList);
}
