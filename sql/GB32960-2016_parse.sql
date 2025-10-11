CREATE TABLE IF NOT EXISTS t_vehicle_login_logout (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '车辆编号',
    vin VARCHAR(17) NOT NULL COMMENT 'VIN号',
    login_serial_no INT DEFAULT NULL COMMENT '登入流水号',
    login_time DATETIME DEFAULT NULL COMMENT '登入时间',
    logout_serial_no INT DEFAULT NULL COMMENT '登出流水号',
    logout_time DATETIME DEFAULT NULL COMMENT '登出时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_vin (vin)
)COMMENT='车辆登入登出信息表';

-- 创建整车数据表
CREATE TABLE IF NOT EXISTS t_vehicle_data (
	id INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号',
    -- 车辆识别码
    vin VARCHAR(17) NOT NULL COMMENT 'VIN号',
    
    -- 数据采集时间
    collect_time DATETIME DEFAULT NULL COMMENT '数据采集时间',
    
    -- 整车数据字段
    vehicle_status TINYINT UNSIGNED DEFAULT NULL COMMENT '车辆状态: 0x01=启动状态, 0x02=熄火, 0x03=其他状态, 0xFE=异常, 0xFF=无效',
    charge_status TINYINT UNSIGNED DEFAULT NULL COMMENT '充电状态: 0x01=停车充电, 0x02=行驶充电, 0x03=未充电状态, 0x04=充电完成, 0xFE=异常, 0xFF=无效',
    run_mode TINYINT UNSIGNED DEFAULT NULL COMMENT '运行模式: 0x01=纯电, 0x02=混动, 0x03=燃油, 0xFE=异常, 0xFF=无效',
    speed VARCHAR(8) DEFAULT NULL COMMENT '车速(km/h), 取值为分辨率0.1的小数或者异常或者无效',
    mileage VARCHAR(8) DEFAULT NULL COMMENT '累计里程(km), 取值为分辨率0.1的小数或者异常或者无效',
    total_voltage VARCHAR(8) DEFAULT NULL COMMENT '总电压(V), 取值为分辨率0.1的小数或者异常或者无效',
    total_current VARCHAR(8) DEFAULT NULL COMMENT '总电流(A), 取值为分辨率0.1的小数或者异常或者无效',
    soc TINYINT UNSIGNED DEFAULT NULL COMMENT '电池余量百分比',
    dc_dc_status TINYINT UNSIGNED DEFAULT NULL COMMENT 'DC-DC状态: 0x01=工作, 0x02=断开, 0xFE=异常, 0xFF=无效',
    gear VARCHAR(13) DEFAULT NULL COMMENT '挡位信息',
    insulation_resistance SMALLINT UNSIGNED DEFAULT NULL COMMENT '绝缘电阻(kΩ)',
    
    -- 更新时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一索引
    UNIQUE KEY uk_vin (vin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='整车数据表';

-- 为VehicleData表添加索引以提高VIN和collect_time查询性能
ALTER TABLE t_vehicle_data ADD INDEX idx_vin_collect_time (vin, collect_time);

-- 创建驱动电机数据表
CREATE TABLE t_motor_data (
	id INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号',
	vin VARCHAR(17) NOT NULL COMMENT 'VIN号',
    
    -- 数据采集时间
    collect_time DATETIME DEFAULT NULL COMMENT '数据采集时间',
    
    -- 电机序号
    motor_seq TINYINT UNSIGNED DEFAULT NULL COMMENT '驱动电机序号',
    
    -- 驱动电机数据字段
    motor_status TINYINT UNSIGNED DEFAULT NULL COMMENT '驱动电机状态: 0x01=耗电, 0x02=发电, 0x03=关闭状态, 0x04=准备状态, 0xFE=异常, 0xFF=无效',
    controller_temp SMALLINT DEFAULT NULL COMMENT '驱动电机控制器温度(°C): -40~+210, 0xFE表示异常, 0xFF表示无效',
    motor_speed INT DEFAULT NULL COMMENT '驱动电机转速(r/min)：-20000~45531，0xFFFE表示异常, 0xFFFF表示无效',
    motor_torque VARCHAR(8) DEFAULT NULL COMMENT '驱动电机转矩(N·m)：取值为分辨率0.1的小数或者异常或者无效',
    motor_temp SMALLINT DEFAULT NULL COMMENT '驱动电机温度(°C): -40~+210, 0xFE表示异常, 0xFF表示无效',
    controller_voltage VARCHAR(8) DEFAULT NULL COMMENT '电机控制器输入电压(V)：取值为分辨率0.1的小数或者异常或者无效',
    controller_current VARCHAR(8) DEFAULT NULL COMMENT '电机控制器直流母线电流(A)：取值为分辨率0.1的小数或者异常或者无效',
    
    -- 更新时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一索引
    UNIQUE KEY uk_vin_motor (vin, motor_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驱动电机数据表';

-- 为MotorData表添加索引以提高VIN和collect_time查询性能
ALTER TABLE t_motor_data ADD INDEX idx_vin_collect_time (vin, collect_time);

-- 创建燃料电池数据表
CREATE TABLE `t_fuel_cell_data` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号',
  `vin` VARCHAR(17) NOT NULL COMMENT 'VIN号',
  `collect_time` DATETIME DEFAULT NULL COMMENT '数据采集时间',
  `fuel_voltage` VARCHAR(8) COMMENT '燃料电池电压(V)：取值为分辨率0.1的小数或者异常或者无效',
  `fuel_current` VARCHAR(8) COMMENT '燃料电池电流(A)：取值为分辨率0.1的小数或者异常或者无效',
  `fuel_consumption` VARCHAR(8) COMMENT '燃料消耗率(kg/100km)：取值为分辨率0.01的小数或者异常或者无效',
  `probe_count` INT COMMENT '燃料电池探针总数：0~65531，0xFFFE表示异常, 0xFFFF表示无效',
  `probe_temps` TEXT COMMENT '探针温度值数组，JSON格式存储',
  `h2_max_temp` VARCHAR(8) COMMENT '氢系统最高温度(°C)：范围-40℃~200℃，取值为分辨率0.1的小数或者异常或者无效',
  `h2_max_temp_probe_code` TINYINT UNSIGNED COMMENT '氢系统最高温度探针代号：1~252，0xFE表示异常, 0xFF表示无效',
  `h2_max_concentration` SMALLINT UNSIGNED COMMENT '氢气最高浓度(mg/kg)：取值为整数，0~60000，0xFFFE表示异常, 0xFFFF表示无效',
  `h2_max_concentration_sensor_code` TINYINT UNSIGNED COMMENT '氢气最高浓度传感器代号：1~252，0xFE表示异常, 0xFF表示无效',
  `h2_max_pressure` VARCHAR(8) COMMENT '氢气最高压力(MPa)：0~100 MPa，取值为分辨率0.1的小数或者异常或者无效',
  `h2_max_pressure_sensor_code` TINYINT UNSIGNED COMMENT '氢气最高压力传感器代号：1~252，0xFE表示异常, 0xFF表示无效',
  `high_voltage_dc_dc_status` TINYINT UNSIGNED COMMENT '高压DC/DC状态：0x01工作，0x02断开，0xFE异常，0xFF无效',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_vin (vin),
  INDEX `idx_vin_collect_time` (`vin`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='燃料电池数据表';

-- 发动机数据表
CREATE TABLE `t_engine_data` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号',
  `vin` VARCHAR(17) NOT NULL COMMENT 'VIN号',
  `collect_time` DATETIME DEFAULT NULL COMMENT '数据采集时间',
  `engine_status` TINYINT UNSIGNED DEFAULT NULL COMMENT '发动机状态: 0x01-启动状态, 0x02-关闭状态, 0xFE-异常, 0xFF-无效',
  `crankshaft_speed` SMALLINT UNSIGNED DEFAULT NULL COMMENT '曲轴转速(r/min)：0~60000，0xFFFE表示异常, 0xFFFF表示无效',
  `fuel_consumption` VARCHAR(8) DEFAULT NULL COMMENT '燃料消耗率(L/100km)：范围0~600，取值为分辨率0.01的小数或者异常或者无效',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_vin` (`vin`),
  INDEX `idx_vin_collect_time` (`vin`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发动机数据表';

-- 车辆位置数据表
CREATE TABLE `t_location_data` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号',
  `vin` VARCHAR(17) NOT NULL COMMENT 'VIN号',
  `collect_time` DATETIME DEFAULT NULL COMMENT '数据采集时间',
  `location_valid` TINYINT(1) DEFAULT NULL COMMENT '定位有效性: 0-有效, 1-无效',
  `longitude` VARCHAR(11) DEFAULT NULL COMMENT '经度(度)：精确到百万分之一度，例如113.339004E',
  `latitude` VARCHAR(10) DEFAULT NULL COMMENT '纬度(度)：精确到百万分之一度，例如25.655451N',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_vin` (`vin`),
  INDEX `idx_vin_collect_time` (`vin`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆位置数据表';

-- 极值数据表
CREATE TABLE `t_extreme_data` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号',
    `vin` VARCHAR(17) NOT NULL COMMENT 'VIN号',
	`collect_time` DATETIME DEFAULT NULL COMMENT '数据采集时间',
    `highest_voltage_system_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最高电压电池子系统号，范围1-250，0xFE=异常，0xFF=无效',
    `highest_voltage_cell_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最高电压电池单体代号，范围1-250，0xFE=异常，0xFF=无效',
    `highest_cell_voltage` VARCHAR(8) DEFAULT NULL COMMENT '电池单体电压最高值，单位V，范围0-15，取值为分辨率0.001的小数或者异常或者无效',
    `lowest_voltage_system_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最低电压电池子系统号，范围1-250，0xFE=异常，0xFF=无效',
    `lowest_voltage_cell_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最低电压电池单体代号，范围1-250，0xFE=异常，0xFF=无效',
    `lowest_cell_voltage` VARCHAR(8) DEFAULT NULL COMMENT '电池单体电压最低值，单位V，范围0-15，取值为分辨率0.001的小数或者异常或者无效',
    `highest_temp_system_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最高温度子系统号，范围1-250，0xFE=异常，0xFF=无效',
    `highest_temp_probe_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最高温度探针序号，范围1-250，0xFE=异常，0xFF=无效',
    `highest_temp_value` SMALLINT DEFAULT NULL COMMENT '最高温度值，单位℃，范围-40℃~+210℃，0xFE=异常，0xFF=无效',
    `lowest_temp_system_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最低温度子系统号，范围1-250，0xFE=异常，0xFF=无效',
    `lowest_temp_probe_no` TINYINT UNSIGNED DEFAULT NULL COMMENT '最低温度探针序号，范围1-250，0xFE=异常，0xFF=无效',
    `lowest_temp_value` SMALLINT DEFAULT NULL COMMENT '最低温度值，单位℃，范围-40℃~+210℃，0xFE=异常，0xFF=无效',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一索引
    UNIQUE KEY `uk_vin` (`vin`),
    
    -- 索引优化查询
    INDEX `idx_vin_collect_time` (`vin`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='极值数据表';

-- 报警数据表
CREATE TABLE `t_alarm_data` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键，非车辆编号',
    `vin` VARCHAR(17) NOT NULL COMMENT 'VIN号',
	`collect_time` DATETIME DEFAULT NULL COMMENT '数据采集时间',
    `max_alarm_level` TINYINT UNSIGNED DEFAULT NULL COMMENT '最高报警等级：范围:0~3，0=无故障，1=1级故障，2=2级故障，3=3级故障，0xFE=异常，0xFF=无效',
    `alarm_types` TINYTEXT DEFAULT NULL COMMENT '报警类型：温度差异报警、电池高温报警、车载储能装置类型过压报警、车载储能装置类型欠压报警、SOC 低报警、单体电池过压报警、单体电池欠压报警、SOC 过高报警、SOC 跳变报警、可充电储能系统不匹配报警、电池单体一致性差报警、绝缘报警、DC-DC 温度报警、制动系统报警、DC-DC 状态报警、驱动电机控制器温度报警、高压互锁状态报警、驱动电机温度报警、车载储能装置类型过充报警，取值为上述报警类型的一种或多种，多种用英文逗号相连',
    `energy_storage_fault_count` TINYINT UNSIGNED DEFAULT NULL COMMENT '可充电储能装置故障总数：范围:0~252, 0xFE=异常，0xFF=无效',
    `energy_storage_faults` TEXT DEFAULT NULL COMMENT '可充电储能装置故障代码的数组，JSON格式存储，例如["158974578","158748978"]',
    `motor_fault_count` TINYINT UNSIGNED DEFAULT NULL COMMENT '驱动电机故障总数：范围:0~252, 0xFE=异常，0xFF=无效',
    `motorFaults` TEXT DEFAULT NULL COMMENT '驱动电机故障代码的数组，JSON格式存储',
    `engineFaultCount` TINYINT UNSIGNED DEFAULT NULL COMMENT '发动机故障总数：范围:0~252, 0xFE=异常，0xFF=无效',
    `engineFaults` TEXT DEFAULT NULL COMMENT '发动机故障代码的数组，JSON格式存储',
    `otherFaultCount` TINYINT UNSIGNED DEFAULT NULL COMMENT '其他故障总数：范围:0~252, 0xFE=异常，0xFF=无效',
    `otherFaults` TEXT DEFAULT NULL COMMENT '其他故障代码的数组，JSON格式存储',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一索引
    UNIQUE KEY `uk_vin` (`vin`),
    
    -- 索引优化查询
    INDEX `idx_vin_collect_time` (`vin`, `collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警数据表';




