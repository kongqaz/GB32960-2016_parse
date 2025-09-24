package com.example.mapper;

import com.example.entity.RealTimeData;
import java.util.List;

public interface RealTimeDataMapper {
    void createTable();
    void insertOrUpdate(RealTimeData data);
    RealTimeData selectByVin(String vin);
    List<RealTimeData> selectAll();
}
