package com.example.service;

import com.example.Gb32960ParserApplication;
import com.example.entity.*;
import com.example.mapper.*;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
            String[] mapperResources = {
                    "RealTimeDataMapper.xml",
                    "mapper/VehicleLoginLogoutMapper.xml",
                    "mapper/VehicleDataMapper.xml",
                    "mapper/MotorDataMapper.xml"
            };
            for (String resource : mapperResources) {
                InputStream inputStream = Resources.getResourceAsStream(resource);
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                mapperParser.parse();
                inputStream.close();
            }

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

    public void saveVehicleLogin(String vin, int serialNo, LocalDateTime loginTime) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            VehicleLoginLogoutMapper mapper = session.getMapper(VehicleLoginLogoutMapper.class);
            int ret = mapper.insertOrUpdateLogin(vin, serialNo, loginTime);
            // 提交事务
            session.commit();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = loginTime.format(formatter);
            logger.info("车辆登录记录已保存 - VIN:{}, SerialNo:{}, loginTime:{}, ret:{}", vin, serialNo, formattedTime, ret);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = loginTime.format(formatter);
                logger.error("保存车辆登录记录失败 - VIN:{}, loginTime:{}", vin, formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存车辆登录记录失败2", ex);
            }
        }
    }

    public void saveVehicleLogout(String vin, int serialNo, LocalDateTime logoutTime) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            VehicleLoginLogoutMapper mapper = session.getMapper(VehicleLoginLogoutMapper.class);
            int ret = mapper.updateLogout(vin, serialNo, logoutTime);
            session.commit();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = logoutTime.format(formatter);
            logger.info("车辆登出记录已保存 - VIN:{}, SerialNo:{}, logoutTime:{}, ret:{}", vin, serialNo, formattedTime, ret);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = logoutTime.format(formatter);
                logger.error("保存车辆登出记录失败 - VIN:{}, logoutTime:{}", vin, formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存车辆登出记录失败2", ex);
            }
        }
    }

    public void saveVehicleData(VehicleData vehicleData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            VehicleDataMapper mapper = session.getMapper(VehicleDataMapper.class);
            int ret = mapper.insertOrUpdate(vehicleData);
            session.commit();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = vehicleData.getCollectTime().format(formatter);
            logger.info("整车数据已保存 - VIN:{}, collectTime:{}, ret:{}", vehicleData.getVin(), formattedTime, ret);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = vehicleData.getCollectTime().format(formatter);
                logger.error("保存整车数据失败 - VIN:{}, collectTime:{}", vehicleData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存整车数据失败2", ex);
            }
        }
    }

    public void saveMotorData(List<MotorData> motorDataList) {
        if (motorDataList == null || motorDataList.isEmpty()) {
            logger.warn("驱动电机数据为空");
            return;
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            MotorDataMapper mapper = session.getMapper(MotorDataMapper.class);
            int ret = mapper.insertOrUpdate(motorDataList);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = motorDataList.get(0).getCollectTime().format(formatter);
            logger.info("驱动电机数据已保存 - VIN:{}, collectTime:{}, 电机数量:{}, ret:{}",
                    motorDataList.get(0).getVin(), formattedTime, motorDataList.size(), ret);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = motorDataList.get(0).getCollectTime().format(formatter);
                logger.error("保存驱动电机数据失败 - VIN:{}, collectTime:{}, 电机数量:{}",
                        motorDataList.get(0).getVin(), formattedTime, motorDataList.size(), e);
            } catch (Exception ex) {
                logger.error("保存驱动电机数据失败2", ex);
            }
        }
    }

    public boolean saveVehicleDataWithTimeCheck(VehicleData vehicleData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            VehicleDataMapper mapper = session.getMapper(VehicleDataMapper.class);

            // 检查是否存在更新的记录
            int newerRecordCount = mapper.existsNewerRecord(vehicleData.getVin(), vehicleData.getCollectTime());

            if (newerRecordCount > 0) {
                // 存在更新的记录，不保存当前数据
                logger.info("存在更新的整车数据记录，跳过保存 - VIN: {}, collectTime: {}",
                        vehicleData.getVin(), vehicleData.getCollectTime());
                return false;
            }

            // 保存数据
            int ret = mapper.insertOrUpdate(vehicleData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = vehicleData.getCollectTime().format(formatter);
            logger.info("整车数据已保存 - VIN:{}, collectTime:{}, ret:{}", vehicleData.getVin(), formattedTime, ret);
            return true;
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = vehicleData.getCollectTime().format(formatter);
                logger.error("保存整车数据失败 - VIN:{}, collectTime:{}", vehicleData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存整车数据失败2", ex);
            }
            return false;
        }
    }

    public boolean saveMotorDataWithTimeCheck(List<MotorData> motorDataList) {
        if (motorDataList == null || motorDataList.isEmpty()) {
            logger.warn("驱动电机数据为空");
            return false;
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            MotorDataMapper mapper = session.getMapper(MotorDataMapper.class);

            // 过滤出需要保存的电机数据
            List<MotorData> filteredMotorDataList = new ArrayList<>();

            for (MotorData motorData : motorDataList) {
                // 检查是否存在更新的记录
                int newerRecordCount = mapper.existsNewerRecordForMotor(
                        motorData.getVin(),
                        motorData.getMotorSeq(),
                        motorData.getCollectTime()
                );

                if (newerRecordCount > 0) {
                    // 存在更新的记录，跳过该项
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedTime = motorData.getCollectTime().format(formatter);
                    logger.info("存在更新的电机数据记录，跳过保存 - VIN: {}, MotorSeq: {}, collectTime: {}",
                            motorData.getVin(), motorData.getMotorSeq(), formattedTime);
                } else {
                    // 没有更新的记录，需要保存
                    filteredMotorDataList.add(motorData);
                }
            }

            // 如果没有需要保存的数据，直接返回
            if (filteredMotorDataList.isEmpty()) {
                logger.info("所有电机数据都已存在更新记录，无需保存");
                return false;
            }

            // 保存过滤后的数据
            int ret = mapper.insertOrUpdate(filteredMotorDataList);
            session.commit();

            MotorData firstMotorData = filteredMotorDataList.get(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = firstMotorData.getCollectTime().format(formatter);
            logger.info("驱动电机数据已保存 - VIN:{}, collectTime:{}, 电机数量:{}, ret:{}",
                    firstMotorData.getVin(), formattedTime, filteredMotorDataList.size(), ret);
            return true;
        } catch (Exception e) {
            try {
                MotorData firstMotorData = motorDataList.get(0);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = firstMotorData.getCollectTime().format(formatter);
                logger.error("保存驱动电机数据失败 - VIN:{}, collectTime:{}, 电机数量:{}",
                        firstMotorData.getVin(), formattedTime, motorDataList.size(), e);
            } catch (Exception ex) {
                logger.error("保存驱动电机数据失败2", ex);
            }
            return false;
        }
    }

    public void saveFuelCellData(FuelCellData fuelCellData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            FuelCellDataMapper mapper = session.getMapper(FuelCellDataMapper.class);

            // 保存数据
            int ret = mapper.insertOrUpdate(fuelCellData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = fuelCellData.getCollectTime().format(formatter);
            logger.info("燃料电池数据已保存 - VIN:{}, collectTime:{}, ret:{}", fuelCellData.getVin(), formattedTime, ret);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = fuelCellData.getCollectTime().format(formatter);
                logger.error("保存燃料电池数据失败 - VIN:{}, collectTime:{}", fuelCellData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存燃料电池数据失败2", ex);
            }
        }
    }

    public boolean saveFuelCellDataWithTimeCheck(FuelCellData fuelCellData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            FuelCellDataMapper mapper = session.getMapper(FuelCellDataMapper.class);

            // 检查是否存在更新的记录
            int newerRecordCount = mapper.existsNewerRecord(fuelCellData.getVin(), fuelCellData.getCollectTime());

            if (newerRecordCount > 0) {
                // 存在更新的记录，不保存当前数据
                logger.info("存在更新的燃料电池数据记录，跳过保存 - VIN: {}, collectTime: {}",
                        fuelCellData.getVin(), fuelCellData.getCollectTime());
                return false;
            }

            // 保存数据
            int ret = mapper.insertOrUpdate(fuelCellData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = fuelCellData.getCollectTime().format(formatter);
            logger.info("燃料电池数据已保存 - VIN:{}, collectTime:{}, ret:{}", fuelCellData.getVin(), formattedTime, ret);
            return true;
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = fuelCellData.getCollectTime().format(formatter);
                logger.error("保存燃料电池数据失败 - VIN:{}, collectTime:{}", fuelCellData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存燃料电池数据失败2", ex);
            }
            return false;
        }
    }

    public void saveEngineData(EngineData engineData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            EngineDataMapper mapper = session.getMapper(EngineDataMapper.class);

            // 保存数据
            int ret = mapper.insertOrUpdate(engineData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = engineData.getCollectTime().format(formatter);
            logger.info("发动机数据已保存 - VIN:{}, collectTime:{}, ret:{}", engineData.getVin(), formattedTime, ret);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = engineData.getCollectTime().format(formatter);
                logger.error("保存发动机数据失败 - VIN:{}, collectTime:{}", engineData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存发动机数据失败2", ex);
            }
        }
    }

    public boolean saveEngineDataWithTimeCheck(EngineData engineData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            EngineDataMapper mapper = session.getMapper(EngineDataMapper.class);

            // 检查是否存在更新的记录
            int newerRecordCount = mapper.existsNewerRecord(engineData.getVin(), engineData.getCollectTime());

            if (newerRecordCount > 0) {
                // 存在更新的记录，不保存当前数据
                logger.info("存在更新的发动机数据记录，跳过保存 - VIN: {}, collectTime: {}",
                        engineData.getVin(), engineData.getCollectTime());
                return false;
            }

            // 保存数据
            int ret = mapper.insertOrUpdate(engineData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = engineData.getCollectTime().format(formatter);
            logger.info("发动机数据已保存 - VIN:{}, collectTime:{}, ret:{}", engineData.getVin(), formattedTime, ret);
            return true;
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = engineData.getCollectTime().format(formatter);
                logger.error("保存发动机数据失败 - VIN:{}, collectTime:{}", engineData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存发动机数据失败2", ex);
            }
            return false;
        }
    }

    // 在类中添加以下方法
    public void saveLocationData(LocationData locationData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LocationDataMapper mapper = session.getMapper(LocationDataMapper.class);

            // 保存数据
            int ret = mapper.insertOrUpdate(locationData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = locationData.getCollectTime().format(formatter);
            logger.info("车辆位置数据已保存 - VIN:{}, collectTime:{}, ret:{}", locationData.getVin(), formattedTime, ret);
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = locationData.getCollectTime().format(formatter);
                logger.error("保存车辆位置数据失败 - VIN:{}, collectTime:{}", locationData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存车辆位置数据失败2", ex);
            }
        }
    }

    public boolean saveLocationDataWithTimeCheck(LocationData locationData) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            LocationDataMapper mapper = session.getMapper(LocationDataMapper.class);

            // 检查是否存在更新的记录
            int newerRecordCount = mapper.existsNewerRecord(locationData.getVin(), locationData.getCollectTime());

            if (newerRecordCount > 0) {
                // 存在更新的记录，不保存当前数据
                logger.info("存在更新的车辆位置数据记录，跳过保存 - VIN: {}, collectTime: {}",
                        locationData.getVin(), locationData.getCollectTime());
                return false;
            }

            // 保存数据
            int ret = mapper.insertOrUpdate(locationData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = locationData.getCollectTime().format(formatter);
            logger.info("车辆位置数据已保存 - VIN:{}, collectTime:{}, ret:{}", locationData.getVin(), formattedTime, ret);
            return true;
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = locationData.getCollectTime().format(formatter);
                logger.error("保存车辆位置数据失败 - VIN:{}, collectTime:{}", locationData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存车辆位置数据失败2", ex);
            }
            return false;
        }
    }

    public boolean saveExtremeData(ExtremeData extremeData, boolean bTimeCheck) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ExtremeDataMapper mapper = session.getMapper(ExtremeDataMapper.class);

            if(bTimeCheck) {
                // 检查是否存在更新的记录
                int newerRecordCount = mapper.existsNewerRecord(extremeData.getVin(), extremeData.getCollectTime());

                if (newerRecordCount > 0) {
                    // 存在更新的记录，不保存当前数据
                    logger.info("存在更新的极值数据记录，跳过保存 - VIN: {}, collectTime: {}",
                            extremeData.getVin(), extremeData.getCollectTime());
                    return false;
                }
            }

            // 保存数据
            int ret = mapper.insertOrUpdate(extremeData);
            session.commit();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = extremeData.getCollectTime().format(formatter);
            logger.info("极值数据已保存 - VIN:{}, collectTime:{}, ret:{}", extremeData.getVin(), formattedTime, ret);
            return true;
        } catch (Exception e) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = extremeData.getCollectTime().format(formatter);
                logger.error("保存极值数据失败 - VIN:{}, collectTime:{}", extremeData.getVin(), formattedTime, e);
            } catch (Exception ex) {
                logger.error("保存极值数据失败2", ex);
            }
            return false;
        }
    }
}
