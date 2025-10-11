package com.example.mapper;

import com.example.entity.EngineData;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

public interface EngineDataMapper {
    @Select("SELECT COUNT(*) FROM t_engine_data WHERE vin = #{vin} AND collect_time >= #{collectTime}")
    int existsNewerRecord(@Param("vin") String vin, @Param("collectTime") LocalDateTime collectTime);

    @Insert("INSERT INTO t_engine_data (vin, collect_time, engine_status, crankshaft_speed, fuel_consumption) " +
            "VALUES (#{vin}, #{collectTime}, #{engineStatus}, #{crankshaftSpeed}, #{fuelConsumption}) " +
            "ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), " +
            "engine_status = VALUES(engine_status), " +
            "crankshaft_speed = VALUES(crankshaft_speed), " +
            "fuel_consumption = VALUES(fuel_consumption)")
    int insertOrUpdate(EngineData engineData);

    @Select("SELECT * FROM t_engine_data WHERE vin = #{vin}")
    EngineData selectByVin(String vin);
}
