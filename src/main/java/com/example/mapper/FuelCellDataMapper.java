package com.example.mapper;

import com.example.entity.FuelCellData;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

public interface FuelCellDataMapper {
    @Select("SELECT COUNT(*) FROM t_fuel_cell_data WHERE vin = #{vin} AND collect_time >= #{collectTime}")
    int existsNewerRecord(@Param("vin") String vin, @Param("collectTime") LocalDateTime collectTime);

    @Insert("INSERT INTO t_fuel_cell_data (vin, collect_time, fuel_voltage, fuel_current, fuel_consumption, probe_count, probe_temps) " +
            "VALUES (#{vin}, #{collectTime}, #{fuelVoltage}, #{fuelCurrent}, #{fuelConsumption}, #{probeCount}, #{probeTemps}) " +
            "ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), fuel_voltage = VALUES(fuel_voltage), fuel_current = VALUES(fuel_current), " +
            "fuel_consumption = VALUES(fuel_consumption), probe_count = VALUES(probe_count), probe_temps = VALUES(probe_temps), " +
            "updated_at = CURRENT_TIMESTAMP")
    int insertOrUpdate(FuelCellData fuelCellData);
}
