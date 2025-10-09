package com.example.mapper;

import com.example.entity.VehicleLoginLogout;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

//@Mapper
public interface VehicleLoginLogoutMapper {

    // 根据 VIN 查询 ID
    Long selectIdByVin(@Param("vin") String vin);

    // 获取最大 ID
    Long selectMaxId();

    // 插入或更新登录记录
    int insertOrUpdateLogin(@Param("vin") String vin, @Param("serialNo") int serialNo, @Param("loginTime") LocalDateTime loginTime);

    // 更新登出记录
    int updateLogout(@Param("vin") String vin, @Param("serialNo") int serialNo, @Param("logoutTime") LocalDateTime logoutTime);
}
