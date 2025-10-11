// ExtremeDataMapper.java
package com.example.mapper;

import com.example.entity.ExtremeData;
import com.example.entity.VehicleData;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface ExtremeDataMapper {
    /**
     * 创建极值数据表
     */
    @Results(id = "extremeDataResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "vin", column = "vin"),
            @Result(property = "highestVoltageSystemNo", column = "highest_voltage_system_no"),
            @Result(property = "highestVoltageCellNo", column = "highest_voltage_cell_no"),
            @Result(property = "highestCellVoltage", column = "highest_cell_voltage"),
            @Result(property = "lowestVoltageSystemNo", column = "lowest_voltage_system_no"),
            @Result(property = "lowestVoltageCellNo", column = "lowest_voltage_cell_no"),
            @Result(property = "lowestCellVoltage", column = "lowest_cell_voltage"),
            @Result(property = "highestTempSystemNo", column = "highest_temp_system_no"),
            @Result(property = "highestTempProbeNo", column = "highest_temp_probe_no"),
            @Result(property = "highestTempValue", column = "highest_temp_value"),
            @Result(property = "lowestTempSystemNo", column = "lowest_temp_system_no"),
            @Result(property = "lowestTempProbeNo", column = "lowest_temp_probe_no"),
            @Result(property = "lowestTempValue", column = "lowest_temp_value"),
            @Result(property = "collectTime", column = "collect_time"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT * FROM t_extreme_data WHERE vin = #{vin}")
    ExtremeData selectByVin(@Param("vin") String vin);

    @Select("SELECT COUNT(*) FROM t_extreme_data WHERE vin = #{vin} AND collect_time >= #{collectTime}")
    int existsNewerRecord(@Param("vin") String vin, @Param("collectTime") LocalDateTime collectTime);

    /**
     * 插入或更新极值数据
     */
    @Insert("INSERT INTO t_extreme_data (" +
            "vin, collect_time, highest_voltage_system_no, highest_voltage_cell_no, highest_cell_voltage, lowest_voltage_system_no, lowest_voltage_cell_no, lowest_cell_voltage, " +
            "highest_temp_system_no, highest_temp_probe_no, highest_temp_value, lowest_temp_system_no, lowest_temp_probe_no, lowest_temp_value" +
            ") VALUES (" +
            "#{vin}, #{collectTime}, #{highestVoltageSystemNo}, #{highestVoltageCellNo}, #{highestCellVoltage}, " +
            "#{lowestVoltageSystemNo}, #{lowestVoltageCellNo}, #{lowestCellVoltage}, " +
            "#{highestTempSystemNo}, #{highestTempProbeNo}, #{highestTempValue}, #{lowestTempSystemNo}, #{lowestTempProbeNo}, #{lowestTempValue}" +
            ") ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), " +
            "highest_voltage_system_no = VALUES(highest_voltage_system_no), " +
            "highest_voltage_cell_no = VALUES(highest_voltage_cell_no), " +
            "highest_cell_voltage = VALUES(highest_cell_voltage), " +
            "lowest_voltage_system_no = VALUES(lowest_voltage_system_no), " +
            "lowest_voltage_cell_no = VALUES(lowest_voltage_cell_no), " +
            "lowest_cell_voltage = VALUES(lowest_cell_voltage), " +
            "highest_temp_system_no = VALUES(highest_temp_system_no), " +
            "highest_temp_probe_no = VALUES(highest_temp_probe_no), " +
            "highest_temp_value = VALUES(highest_temp_value), " +
            "lowest_temp_system_no = VALUES(lowest_temp_system_no), " +
            "lowest_temp_probe_no = VALUES(lowest_temp_probe_no), " +
            "lowest_temp_value = VALUES(lowest_temp_value), " +
            "updated_at = CURRENT_TIMESTAMP")
    int insertOrUpdate(ExtremeData extremeData);
}