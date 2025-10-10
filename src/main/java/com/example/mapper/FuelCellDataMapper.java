package com.example.mapper;

import com.example.entity.FuelCellData;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

public interface FuelCellDataMapper {
    @Select("SELECT COUNT(*) FROM t_fuel_cell_data WHERE vin = #{vin} AND collect_time >= #{collectTime}")
    int existsNewerRecord(@Param("vin") String vin, @Param("collectTime") LocalDateTime collectTime);

    @Insert("INSERT INTO t_fuel_cell_data (vin, collect_time, fuel_voltage, fuel_current, fuel_consumption, " +
            "probe_count, probe_temps, h2_max_temp, h2_max_temp_probe_code, h2_max_concentration, " +
            "h2_max_concentration_sensor_code, h2_max_pressure, h2_max_pressure_sensor_code, high_voltage_dc_dc_status) " +
            "VALUES (#{vin}, #{collectTime}, #{fuelVoltage}, #{fuelCurrent}, #{fuelConsumption}, #{probeCount}, " +
            "#{probeTemps}, #{h2MaxTemp}, #{H2MaxTempProbeCode}, #{H2MaxConcentration}, #{H2MaxConcentrationSensorCode}, " +
            "#{H2MaxPressure}, #{H2MaxPressureSensorCode}, #{HighVoltageDcDcStatus}) " +
            "ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), " +
            "fuel_voltage = VALUES(fuel_voltage), " +
            "fuel_current = VALUES(fuel_current), " +
            "fuel_consumption = VALUES(fuel_consumption), " +
            "probe_count = VALUES(probe_count), " +
            "probe_temps = VALUES(probe_temps), " +
            "h2_max_temp = VALUES(h2_max_temp), " +
            "h2_max_temp_probe_code = VALUES(h2_max_temp_probe_code), " +
            "h2_max_concentration = VALUES(h2_max_concentration), " +
            "h2_max_concentration_sensor_code = VALUES(h2_max_concentration_sensor_code), " +
            "h2_max_pressure = VALUES(h2_max_pressure), " +
            "h2_max_pressure_sensor_code = VALUES(h2_max_pressure_sensor_code), " +
            "high_voltage_dc_dc_status = VALUES(high_voltage_dc_dc_status), " +
            "updated_at = CURRENT_TIMESTAMP")
    int insertOrUpdate(FuelCellData fuelCellData);
}
