package com.example.service;

import com.example.entity.RealTimeData;
import com.example.mapper.RealTimeDataMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DatabaseService {
    private static DatabaseService instance;
    private SqlSessionFactory sqlSessionFactory;

    private DatabaseService() {
        try {
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

            // 初始化表
            try (SqlSession session = sqlSessionFactory.openSession()) {
                RealTimeDataMapper mapper = session.getMapper(RealTimeDataMapper.class);
                mapper.createTable();
                session.commit();
            }
        } catch (IOException e) {
            throw new RuntimeException("初始化数据库连接失败", e);
        }
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public void saveRealTimeData(String vin, String jsonData, String formattedTime) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            RealTimeDataMapper mapper = session.getMapper(RealTimeDataMapper.class);
            RealTimeData data = new RealTimeData(vin, jsonData);
            data.setUpdateTime(formattedTime);
            mapper.insertOrUpdate(data);
            session.commit();
        }
    }

    public String getRealTimeData(String vin) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            RealTimeDataMapper mapper = session.getMapper(RealTimeDataMapper.class);
            RealTimeData data = mapper.selectByVin(vin);
            return data != null ? data.getJsonData() : null;
        }
    }

    public List<RealTimeData> getAllRealTimeData() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            RealTimeDataMapper mapper = session.getMapper(RealTimeDataMapper.class);
            return mapper.selectAll();
        }
    }
}
