// file: src/main/java/com/example/mapper/LocationDataMapper.java
package com.example.mapper;

import com.example.entity.LocationData;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

public interface LocationDataMapper {
    @Select("SELECT COUNT(*) FROM t_location_data WHERE vin = #{vin} AND collect_time >= #{collectTime}")
    int existsNewerRecord(@Param("vin") String vin, @Param("collectTime") LocalDateTime collectTime);

    @Insert("INSERT INTO t_location_data (vin, collect_time, location_valid, longitude, latitude) " +
            "VALUES (#{vin}, #{collectTime}, #{locationValid}, #{longitude}, #{latitude}) " +
            "ON DUPLICATE KEY UPDATE " +
            "collect_time = VALUES(collect_time), " +
            "location_valid = VALUES(location_valid), " +
            "longitude = VALUES(longitude), " +
            "latitude = VALUES(latitude)")
    int insertOrUpdate(LocationData locationData);

    @Select("SELECT * FROM t_location_data WHERE vin = #{vin}")
    LocationData selectByVin(String vin);
}
