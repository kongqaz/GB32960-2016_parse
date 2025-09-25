package com.example.service;

import com.example.Gb32960ParserApplication;
import com.example.entity.RealTimeData;
import com.example.mapper.RealTimeDataMapper;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.mapping.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DatabaseService {
    private static DatabaseService instance;
    private SqlSessionFactory sqlSessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    private DatabaseService() {
//        try {
//            String resource = "mybatis-config.xml";
//            InputStream inputStream = Resources.getResourceAsStream(resource);
//            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
//
//            // 初始化表
//            try (SqlSession session = sqlSessionFactory.openSession()) {
//                RealTimeDataMapper mapper = session.getMapper(RealTimeDataMapper.class);
//                mapper.createTable();
//                session.commit();
//                logger.info("DatabaseService init OK");
//            }
//        } catch (Exception e) {
////            throw new RuntimeException("初始化数据库连接失败", e);
//            logger.error("初始化数据库连接失败", e);
//        }
    }

    private void init(Config sysconfig) throws  Exception{
        try {
            // 配置 HikariCP 数据源
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(sysconfig.getString("gb32960.db.url"));
            config.setUsername(sysconfig.getString("gb32960.db.username"));
            config.setPassword(sysconfig.getString("gb32960.db.password"));
            config.setDriverClassName(sysconfig.getString("gb32960.db.driver"));

            // 连接池配置
            config.setMaximumPoolSize(sysconfig.getInt("gb32960.db.pool.maxSize"));
            config.setMinimumIdle(sysconfig.getInt("gb32960.db.pool.minIdle"));
            config.setConnectionTimeout(sysconfig.getInt("gb32960.db.pool.connectionTimeout"));
            config.setIdleTimeout(sysconfig.getInt("gb32960.db.pool.idleTimeout"));
            config.setMaxLifetime(sysconfig.getInt("gb32960.db.pool.maxLifetime"));
            config.setLeakDetectionThreshold(sysconfig.getInt("gb32960.db.pool.leakDetectionThreshold"));

            // 健康检查配置
            config.setConnectionTestQuery("SELECT 1");

            HikariDataSource dataSource = new HikariDataSource(config);

            // 创建 MyBatis 环境
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("development", transactionFactory, dataSource);

            // 构建 SqlSessionFactory
//            String resource = "mybatis-config.xml";
//            InputStream inputStream = Resources.getResourceAsStream(resource);
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);

            // 解析 XML 配置文件
//            XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(inputStream);
//            org.apache.ibatis.session.Configuration parsedConfiguration = xmlConfigBuilder.parse();
//            parsedConfiguration.setEnvironment(environment); // 替换数据源环境

            // 显式添加 XML 资源
            String resource = "RealTimeDataMapper.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
            mapperParser.parse();
            inputStream.close();

//            configuration.addMapper(RealTimeDataMapper.class);
//            configuration.addMappers("com.example.mapper");

            sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
//            sqlSessionFactory = new SqlSessionFactoryBuilder().build(parsedConfiguration);

            // 初始化表
            try (SqlSession session = sqlSessionFactory.openSession()) {
                RealTimeDataMapper mapper = session.getMapper(RealTimeDataMapper.class);
                mapper.createTable();
                session.commit();
                logger.info("DatabaseService init OK");
            }
        } catch (Exception e) {
            logger.error("初始化数据库连接失败", e);
            throw e;
        }
    }

    public static synchronized DatabaseService getInstance(Config config) {
        if (instance == null) {
            instance = new DatabaseService();
            while(true) {
                try {
                    instance.init(config);
                    break;
                } catch (Exception e) {
                    logger.error("DatabaseService init fail:", e);
                    try {
                        Thread.sleep(3000);
                    } catch (Exception ex) {
                        logger.error("Thread.sleep fail:", ex);
                    }
                }
            }
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
