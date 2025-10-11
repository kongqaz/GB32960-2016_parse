package com.example.gb32960;

import com.example.entity.*;
import com.example.service.DatabaseService;
import com.typesafe.config.Config;
//import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GB32960协议处理器: version1.0
 */
public class Gb32960ProtocolHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Gb32960ProtocolHandler.class);
    private static final Logger loggerDebug = LoggerFactory.getLogger("logger.DEBUG_MSG");

    // GB32960协议常量
    private static final byte START_DELIMITER_1 = 0x23; // 起始符 #
    private static final byte START_DELIMITER_2 = 0x23; // 起始符 #
    private static final byte ENCRYPTION_NONE = 0x01; // 数据单元未加密
    private static final byte COMMAND_PLATFORM_LOGIN = 0x05; // 平台登录命令

    private static final byte RESPONSE_SUCCESS = 0x01; // 应答成功
    private static final byte RESPONSE_ERROR = 0x02; // 应答错误
    private static final byte RESPONSE_VIN_DUPLICATE = 0x03; // VIN重复
    private static final byte FLAG_COMMAND = (byte) 0xFE; // 命令包标志

    // 协议常量（补充可充电储能装置信息类型）
    private static final byte INFO_TYPE_ENERGY_VOLTAGE = 0x08; // 可充电储能装置电压数据
    private static final byte INFO_TYPE_ENERGY_TEMPERATURE = 0x09; // 可充电储能装置温度数据
    private static final int INSULATION_RESISTANCE_MAX = 60000; // 绝缘电阻最大值（kΩ）
    private static final int RESEND_RETENTION_DAYS_PLATFORM = 7; // 平台间补发数据保留天数
    private static final int RESEND_RETENTION_DAYS_TERMINAL = 3; // 车载终端补发数据保留天数

    private String lastValidLocation; // 存储最后一次有效定位数据

    private final Config config;
//    private MqttClient mqttClient;
//    private final String mqttTopic;
//    private final int mqttQos;

    // 数据缓存队列
    private final BlockingQueue<String> dataQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private Thread mqttSenderThread;

    // 会话信息
//    private String vin; // 车辆识别码
    private boolean authenticated = false;

    private DatabaseService databaseService;

    public Gb32960ProtocolHandler(Config config) {
        this.config = config;
        this.databaseService = DatabaseService.getInstance(config);
//        this.mqttTopic = config.getString("gb32960.mqtt.topic");
//        this.mqttQos = config.getInt("gb32960.mqtt.qos");

//        initMqttClient();
//        startMqttSender();
    }

    /**
     * 初始化MQTT客户端
     */
//    private void initMqttClient() {
//        try {
//            String brokerUrl = config.getString("gb32960.mqtt.broker-url");
//            String clientId = config.getString("gb32960.mqtt.client-id");
//
//            mqttClient = new MqttClient(brokerUrl, clientId, null);
//
//            MqttConnectOptions options = new MqttConnectOptions();
//            options.setCleanSession(true);
//            options.setConnectionTimeout(30);
//            options.setKeepAliveInterval(60);
//
//            // 设置用户名密码
//            String username = config.getString("gb32960.mqtt.username");
//            String password = config.getString("gb32960.mqtt.password");
//            if (!username.isEmpty()) {
//                options.setUserName(username);
//            }
//            if (!password.isEmpty()) {
//                options.setPassword(password.toCharArray());
//            }
//
//            mqttClient.connect(options);
//            logger.info("MQTT客户端连接成功: {}", brokerUrl);
//        } catch (MqttException e) {
//            logger.error("MQTT客户端连接失败", e);
//        }
//    }

    /**
     * 启动MQTT消息发送线程
     */
    private void startMqttSender() {
        mqttSenderThread = new Thread(() -> {
            List<String> batch = new ArrayList<>();
            while (isRunning.get()) {
                try {
                    // 批量处理，最多10条消息或等待1秒
                    String data = dataQueue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
                    if (data != null) {
                        batch.add(data);
                    }

                    // 如果有数据或者批次达到一定数量，则发送
                    if (!batch.isEmpty() && (batch.size() >= 10 || data == null)) {
//                        sendBatchData(batch);
                        batch.clear();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("MQTT消息发送异常", e);
                }
            }
        });
        mqttSenderThread.setDaemon(true);
        mqttSenderThread.setName("MqttSenderThread");
        mqttSenderThread.start();
    }

    /**
     * 发送批量数据到MQTT
     */
//    private void sendBatchData(List<String> dataList) {
//        if (dataList.isEmpty() || mqttClient == null || !mqttClient.isConnected()) {
//            return;
//        }
//
//        try {
//            // 构造JSON数组
//            StringBuilder jsonBuilder = new StringBuilder("[");
//            for (int i = 0; i < dataList.size(); i++) {
//                if (i > 0) {
//                    jsonBuilder.append(",");
//                }
//                jsonBuilder.append(dataList.get(i));
//            }
//            jsonBuilder.append("]");
//
//            String jsonData = jsonBuilder.toString();
//            MqttMessage message = new MqttMessage(jsonData.getBytes(StandardCharsets.UTF_8));
//            message.setQos(mqttQos);
//            message.setRetained(false);
//
//            mqttClient.publish(mqttTopic, message);
//            logger.debug("发送MQTT消息到主题 {}: {} 条数据", mqttTopic, dataList.size());
//        } catch (MqttException e) {
//            logger.error("MQTT消息发送失败", e);
//        }
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端连接建立: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof byte[]) {
            byte[] data = (byte[]) msg;
            loggerDebug.info("接收到原始数据: {}", bytesToHex(data));

            try {
                processGb32960Message(ctx, data);
            } catch (Exception e) {
                logger.error("处理GB32960消息异常", e);
            }
        }
    }

    /**
     * 处理GB32960协议消息
     */
    private void processGb32960Message(ChannelHandlerContext ctx, byte[] data) {
        if (data.length < 25) {
            logger.error("数据长度不足，丢弃消息");
            return;
        }

        // 检查起始符
        if (data[0] != START_DELIMITER_1 || data[1] != START_DELIMITER_2) {
            logger.error("无效的起始符: 0x{} 0x{}", String.format("%02X", data[0]), String.format("%02X", data[1]));
            return;
        }

        // 解析消息头 (注意索引都向后移动一位，因为起始符变为两个字节)
        byte command = data[2]; // 命令标识
        byte response = data[3]; // 应答标识
        String vin = new String(data, 4, 17, StandardCharsets.ISO_8859_1).trim(); // VIN码
        byte encryption = data[21]; // 加密方式
        int dataLength = ((data[22] & 0xFF) << 8) | (data[23] & 0xFF); // 数据单元长度

        logger.info("从{}接收到GB32960消息 - 命令: 0x{}, 应答: 0x{}, VIN: {}, 加密: 0x{}, 数据长度: {}",
                ctx.channel().remoteAddress(), String.format("%02X", command), String.format("%02X", response),
                vin, String.format("%02X", encryption), dataLength);

        // 检查数据完整性 (注意索引变化，需要包括校验码)
        if (data.length < 25 + dataLength) {
            logger.error("数据长度不匹配，期望: {}, 实际: {}", 25 + dataLength, data.length);
            return;
        }

        // 验证校验码
        byte calculatedBcc = calculateBcc(data, 2, 24 + dataLength); // 从第一个起始符后开始计算到数据单元末尾
        byte receivedBcc = data[24 + dataLength]; // 接收到的校验码

        if (calculatedBcc != receivedBcc) {
            logger.error("BCC校验失败，计算值: 0x{}，接收值: 0x{}",
                    String.format("%02X", calculatedBcc), String.format("%02X", receivedBcc));
            return;
        }

        if (response != FLAG_COMMAND) {
            logger.info("非命令包（应答标志: 0x{}），无需处理", String.format("%02X", response));
            return;
        }

        // 保存VIN码
//        this.vin = vin;

        // 根据命令类型处理 (偏移量增加1，且需要排除校验码)
        switch (command) {
            case COMMAND_PLATFORM_LOGIN:
                handlePlatformLogin(ctx, data, 24, dataLength, vin);
                break;
            case 0x01: // 车辆登入
                handleVehicleLogin(ctx, data, 24, dataLength, vin);
                break;
            case 0x02: // 实时信息上报
                handleRealTimeData(data, 24, dataLength, vin);
                break;
            case 0x03: // 补发信息上报
                handleResendData(data, 24, dataLength, vin);
                break;
            case 0x04: // 车辆登出
                handleVehicleLogout(ctx, data, 24, dataLength, vin);
                break;
            case 0x06: // 平台登出
                handlePlatformLogout(ctx, data, 24, dataLength);
                break;
            case 0x07: // 心跳
                handleHeartbeat(ctx, vin);
                break;
            default:
                logger.error("未知命令类型: 0x{}", String.format("%02X", command));
        }
    }

    private byte calculateBcc(byte[] data, int start, int end) {
        byte bcc = 0;
        for (int i = start; i < end; i++) {
            bcc ^= (data[i] & 0xFF);
        }
        return bcc;
    }

    /**
     * 处理平台登录（修复：按标准解析12字节用户名、20字节密码）
     */
    private void handlePlatformLogin(ChannelHandlerContext ctx, byte[] data, int offset, int length, String vin) {
        // 标准平台登录数据单元最小长度：6（时间）+2（流水号）+12（用户名）+20（密码）+1（加密规则）=41字节
        if (length < 41) {
            logger.error("平台登录数据长度不足（最小41字节），实际: {}", length);
            sendPlatformLoginResponse(ctx, RESPONSE_ERROR, vin);
            return;
        }

        // 按标准解析字段（索引偏移：offset=24）
        int loginTimeYear = 2000 + (data[offset] & 0xFF); // 登录时间-年（1字节）
        int loginTimeMonth = data[offset + 1] & 0xFF; // 月
        int loginTimeDay = data[offset + 2] & 0xFF; // 日
        int loginTimeHour = data[offset + 3] & 0xFF; // 时
        int loginTimeMinute = data[offset + 4] & 0xFF; // 分
        int loginTimeSecond = data[offset + 5] & 0xFF; // 秒
        int serialNo = ((data[offset + 6] & 0xFF) << 8) | (data[offset + 7] & 0xFF); // 登入流水号（2字节）
        String username = new String(data, offset + 8, 12, StandardCharsets.ISO_8859_1).trim(); // 用户名（12字节）
        String password = new String(data, offset + 20, 20, StandardCharsets.ISO_8859_1).trim(); // 密码（20字节）
        byte encryptRule = data[offset + 40]; // 加密规则（1字节）

        // 从配置读取期望的登录信息
        String expectedUsername = config.getString("gb32960.platform.username");
        String expectedPassword = config.getString("gb32960.platform.password");
        String expectedUniqueCode = config.getString("gb32960.platform.unique-code");

        logger.info("平台登录请求 - VIN: {}, 时间: {}-{}-{} {}:{}:{}, 流水号: {}, 用户名: {}, 加密规则: 0x{}",
                vin, loginTimeYear, loginTimeMonth, loginTimeDay, loginTimeHour, loginTimeMinute, loginTimeSecond,
                serialNo, username, String.format("%02X", encryptRule));

        // 验证登录信息
        if (expectedUsername.equals(username) && expectedPassword.equals(password) && expectedUniqueCode.equals(vin) && encryptRule == ENCRYPTION_NONE) {
            authenticated = true;
            logger.info("平台登录成功 - VIN: {}", vin);
            sendPlatformLoginResponse(ctx, RESPONSE_SUCCESS, vin);
        } else {
            logger.error("平台登录失败 - 用户名/密码错误或加密规则不匹配");
            authenticated = false;
            sendPlatformLoginResponse(ctx, RESPONSE_ERROR, vin);
        }
    }

    /**
     * 发送平台登录响应（按标准构造响应包）
     */
    private void sendPlatformLoginResponse(ChannelHandlerContext ctx, byte result, String vin) {
        // 标准平台登录响应数据单元长度：6（时间）+2（流水号）+1（结果）=9字节
        byte[] response = new byte[24 + 9 + 1]; // 头24字节 + 数据9字节 + 校验1字节
        int dataUnitLength = 9;

        // 1. 填充起始符
        response[0] = START_DELIMITER_1;
        response[1] = START_DELIMITER_2;

        // 2. 命令单元（命令标识=0x05，应答标志=结果码）
        response[2] = COMMAND_PLATFORM_LOGIN;
        response[3] = result;

        // 3. VIN码（17字节，不足补0x00）
        setRspVin(vin, response);

        // 4. 加密方式（不加密）
        response[21] = ENCRYPTION_NONE;

        // 5. 数据单元长度（2字节，大端）
        response[22] = (byte) (dataUnitLength >> 8);
        response[23] = (byte) (dataUnitLength & 0xFF);

        // 6. 数据单元（当前时间+0流水号+结果）
        LocalDateTime now = LocalDateTime.now();
        response[24] = (byte) (now.getYear() - 2000); // 年（相对于2000）
        response[25] = (byte) now.getMonthValue(); // 月
        response[26] = (byte) now.getDayOfMonth(); // 日
        response[27] = (byte) now.getHour(); // 时
        response[28] = (byte) now.getMinute(); // 分
        response[29] = (byte) now.getSecond(); // 秒
        response[30] = 0x00; // 流水号高字节（默认0）
        response[31] = 0x00; // 流水号低字节
        response[32] = result; // 执行结果

        // 7. 计算BCC校验（范围：命令标识（2）到数据单元末尾（32））
        response[33] = calculateBcc(response, 2, 33);

        // 发送响应
        ctx.writeAndFlush(Unpooled.copiedBuffer(response));
        loggerDebug.info("发送平台登录响应: {}", bytesToHex(response));
    }

    private void setRspVin(String vin, byte[] response){
        // 修改后
        byte[] vinBytes = (vin != null ? vin : "").getBytes(StandardCharsets.ISO_8859_1);
        int copyLength = Math.min(vinBytes.length, 17);
        System.arraycopy(vinBytes, 0, response, 4, copyLength);
        // 剩余位置填充0x00
        for (int i = 4 + copyLength; i < 4 + 17; i++) {
            response[i] = 0x00;
        }
    }

    /**
     * 处理车辆登录（修复：解析ICCID、可充电储能子系统信息，按标准构造响应）
     */
    private void handleVehicleLogin(ChannelHandlerContext ctx, byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的车辆登录请求 - VIN: {}", vin);
            return;
        }

        // 标准车辆登录数据单元最小长度：6（时间）+2（流水号）+20（ICCID）+1（子系统数）+1（编码长度）=30字节
        if (length < 30) {
            logger.error("车辆登录数据长度不足（最小30字节），实际: {}", length);
            return;
        }

        // 按标准解析字段
        int loginTimeYear = 2000 + (data[offset] & 0xFF);
        int loginTimeMonth = data[offset + 1] & 0xFF;
        int loginTimeDay = data[offset + 2] & 0xFF;
        int loginTimeHour = data[offset + 3] & 0xFF;
        int loginTimeMinute = data[offset + 4] & 0xFF;
        int loginTimeSecond = data[offset + 5] & 0xFF;
        int serialNo = ((data[offset + 6] & 0xFF) << 8) | (data[offset + 7] & 0xFF); // 登入流水号
        String iccid = new String(data, offset + 8, 20, StandardCharsets.ISO_8859_1).trim(); // ICCID（20字节）
        int subsystemCount = data[offset + 28] & 0xFF; // 可充电储能子系统数
        int codeLength = data[offset + 29] & 0xFF; // 系统编码长度

        // 解析可充电储能系统编码（子系统数×编码长度）
        List<String> subsystemCodes = new ArrayList<>();
        if (subsystemCount > 0 && codeLength > 0 && offset + 30 + subsystemCount * codeLength <= offset + length) {
            for (int i = 0; i < subsystemCount; i++) {
                int codeOffset = offset + 30 + i * codeLength;
                String code = new String(data, codeOffset, codeLength, StandardCharsets.ISO_8859_1).trim();
                subsystemCodes.add(code);
            }
        }

        logger.info("车辆登录请求 - VIN: {}, 时间: {}-{}-{} {}:{}:{}, 流水号: {}, ICCID: {}, 子系统数: {}, 编码列表: {}",
                vin, loginTimeYear, loginTimeMonth, loginTimeDay, loginTimeHour, loginTimeMinute, loginTimeSecond,
                serialNo, iccid, subsystemCount, subsystemCodes);

        // 按标准构造响应（数据单元：6时间+2流水号+1结果=9字节）
        byte[] response = new byte[24 + 9 + 1];
        int dataUnitLength = 9;

        // 填充响应头
        response[0] = START_DELIMITER_1;
        response[1] = START_DELIMITER_2;
        response[2] = 0x01; // 命令标识=车辆登录
        response[3] = RESPONSE_SUCCESS; // 应答成功
        setRspVin(vin, response);
        response[21] = ENCRYPTION_NONE;
        response[22] = (byte) (dataUnitLength >> 8);
        response[23] = (byte) (dataUnitLength & 0xFF);

        // 填充数据单元（当前时间+原流水号+成功结果）
        LocalDateTime now = LocalDateTime.now();
        response[24] = (byte) (now.getYear() - 2000);
        response[25] = (byte) now.getMonthValue();
        response[26] = (byte) now.getDayOfMonth();
        response[27] = (byte) now.getHour();
        response[28] = (byte) now.getMinute();
        response[29] = (byte) now.getSecond();
        response[30] = (byte) (serialNo >> 8); // 原流水号
        response[31] = (byte) (serialNo & 0xFF);
        response[32] = RESPONSE_SUCCESS; // 结果

        // 计算BCC
        response[33] = calculateBcc(response, 2, 33);

        // 发送响应
        ctx.writeAndFlush(Unpooled.copiedBuffer(response));
        databaseService.saveVehicleLogin(vin, serialNo, LocalDateTime.of(loginTimeYear, loginTimeMonth, loginTimeDay,
                loginTimeHour, loginTimeMinute, loginTimeSecond));
        logger.info("车辆登录成功 - VIN: {}", vin);
    }

    /**
     * 处理实时数据（修复：解析信息类型标志，修正燃料电池/报警数据解析，增加异常值判断）
     */
    private void handleRealTimeData(byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的实时数据上报 - VIN: {}", vin);
            return;
        }

        try {
            StringBuilder parsedData = new StringBuilder();
            parsedData.append("{");

            int pos = offset;
            // 1. 先解析6字节数据采集时间（协议7.2.1强制要求）
            if (pos + 6 > offset + length) {
                logger.error("实时数据缺少6字节采集时间，丢弃 - VIN: {}", vin);
                return;
            }
            int collectYear = 2000 + (data[pos] & 0xFF);
            int collectMonth = data[pos + 1] & 0xFF;
            int collectDay = data[pos + 2] & 0xFF;
            int collectHour = data[pos + 3] & 0xFF;
            int collectMinute = data[pos + 4] & 0xFF;
            int collectSecond = data[pos + 5] & 0xFF;
            parsedData.append(String.format("\"collectTime\":\"%04d-%02d-%02d %02d:%02d:%02d\",",
                    collectYear, collectMonth, collectDay, collectHour, collectMinute, collectSecond));
            LocalDateTime collectTime = LocalDateTime.of(collectYear, collectMonth, collectDay, collectHour, collectMinute,
                    collectSecond);
            pos += 6; // 跳过已解析的6字节时间

            // 2. 解析后续的信息类型标志+信息体
            while (pos < offset + length) {
                if (pos + 1 > offset + length) break;
                byte infoType = data[pos];
                pos++;
                logger.info("实时数据类型:{}", infoType);
                // 【修复问题2：补充可充电储能装置电压/温度数据解析】
                switch (infoType) {
                    case 0x01: // 整车数据
                        VehicleData vehicleData = new VehicleData();
                        vehicleData.setVin(vin);
                        vehicleData.setCollectTime(collectTime);
                        boolean bParsed = parseVehicleData(data, pos, offset + length, parsedData, vehicleData);
                        if(bParsed) {
                            pos += 20; // 整车数据固定20字节
                            databaseService.saveVehicleData(vehicleData);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x02: // 驱动电机数据
                        List<MotorData> motorDataList = new ArrayList<>();
                        pos = parseMotorData(data, pos, offset + length, parsedData, motorDataList);
                        for (MotorData motorData : motorDataList){
                            motorData.setVin(vin);
                            motorData.setCollectTime(collectTime);
                        }
                        databaseService.saveMotorData(motorDataList);
                        break;
                    case 0x03: // 燃料电池数据
                        FuelCellData fuelCellData = new FuelCellData();
                        fuelCellData.setVin(vin);
                        fuelCellData.setCollectTime(collectTime);
                        int oldPos = pos;
                        pos = parseFuelCellData(data, pos, offset + length, parsedData, fuelCellData);
                        if(pos > oldPos) {
                            databaseService.saveFuelCellData(fuelCellData);
                        }
                        break;
                    case 0x04: // 发动机数据
                        EngineData engineData = new EngineData();
                        engineData.setVin(vin);
                        engineData.setCollectTime(collectTime);
                        boolean bEngineParsed = parseEngineData(data, pos, offset + length, parsedData, engineData);
                        if(bEngineParsed) {
                            pos += 5;
                            databaseService.saveEngineData(engineData);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x05: // 车辆位置数据
                        // 【修复问题5：处理定位有效性与历史数据】
                        LocationData locationData = new LocationData();
                        locationData.setVin(vin);
                        locationData.setCollectTime(collectTime);
                        boolean bLocationParsed = parseLocationData(data, pos, offset + length, parsedData, locationData);
                        if(bLocationParsed) {
                            pos += 9;
                            databaseService.saveLocationData(locationData);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x06: // 极值数据
                        ExtremeData extremeData = new ExtremeData();
                        extremeData.setVin(vin);
                        extremeData.setCollectTime(collectTime);
                        boolean bExtremeParsed = parseExtremeData(data, pos, offset + length, parsedData, extremeData);
                        if(bExtremeParsed) {
                            pos += 14;
                            databaseService.saveExtremeData(extremeData, false);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x07: // 报警数据
                        pos = parseAlarmData(data, pos, offset + length, parsedData);
                        break;
//                    case INFO_TYPE_ENERGY_VOLTAGE: // 可充电储能装置电压数据（新增）
//                        pos = parseEnergyVoltageData(data, pos, parsedData);
//                        break;
//                    case INFO_TYPE_ENERGY_TEMPERATURE: // 可充电储能装置温度数据（新增）
//                        pos = parseEnergyTemperatureData(data, pos, parsedData);
//                        break;
                    default:
                        logger.warn("未知信息类型标志: 0x{}，跳过 - VIN: {}",
                                String.format("%02X", infoType), vin);
                        pos++;
                }
            }

            String dataJson = parsedData.toString().replaceAll(",$", "") + "}";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedTime = LocalDateTime.now().format(formatter);
            String jsonData = String.format(
                    "{\"vin\":\"%s\",\"timestamp\":\"%s\",\"dataType\":\"realtime\",\"data\":%s}",
                    vin, formattedTime, dataJson
            );

            loggerDebug.info("实时数据数据单元{}字节: {}", length, bytesToHex(data, offset, length));
            loggerDebug.info("实时数据解析结果: {}", jsonData);
            databaseService.saveRealTimeData(vin, jsonData, formattedTime);
//            dataQueue.offer(jsonData);

        } catch (Exception e) {
            logger.error("处理实时数据异常 - VIN: {}", vin, e);
        }
    }

    /**
     * 解析整车数据（修复：增加异常值判断，按标准定义字段）
     */
    private boolean parseVehicleData(byte[] data, int pos, int endMark, StringBuilder result, VehicleData vehicleData) {
        // 整车数据：20字节
        if (pos + 20 > endMark) {
            logger.error("整车数据长度不足，跳过解析");
            return false;
        }

        // 整车核心数据（按标准定义，增加异常值判断）
        byte vehicleStatus = data[pos];
        byte chargeStatus = data[pos + 1];
        byte runMode = data[pos + 2];
        int speedRaw = ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF);
        long mileageRaw = ((data[pos + 5] & 0xFF) << 24) | ((data[pos + 6] & 0xFF) << 16) |
                ((data[pos + 7] & 0xFF) << 8) | (data[pos + 8] & 0xFF);
        int voltageRaw = ((data[pos + 9] & 0xFF) << 8) | (data[pos + 10] & 0xFF);
        int currentRaw = ((data[pos + 11] & 0xFF) << 8) | (data[pos + 12] & 0xFF);
        byte socRaw = data[pos + 13];
        byte dcStatus = data[pos + 14];
        byte gear = data[pos + 15];
        int resistanceRaw = ((data[pos + 16] & 0xFF) << 8) | (data[pos + 17] & 0xFF);
//        byte accelerator = data[pos + 18];
//        byte brake = data[pos + 19];

        // 处理异常值（按标准：0xFE=异常，0xFF=无效）
        String vehicleStatusStr = getVehicleStatus(vehicleStatus);
        String chargeStatusStr = getChargeStatus(chargeStatus);
        String runModeStr = getRunMode(runMode);
        String speedStr = (speedRaw == 0xFFFE) ? "异常" : (speedRaw == 0xFFFF) ? "无效" : String.format("%.1f", speedRaw / 10.0f);
        String mileageStr = (mileageRaw == 0xFFFFFFFEL) ? "异常" : (mileageRaw == 0xFFFFFFFFL) ? "无效" : String.format("%.1f", mileageRaw / 10.0f);
        String voltageStr = (voltageRaw == 0xFFFE) ? "异常" : (voltageRaw == 0xFFFF) ? "无效" : String.format("%.1f", voltageRaw / 10.0f);
        String currentStr = (currentRaw == 0xFFFE) ? "异常" : (currentRaw == 0xFFFF) ? "无效" : String.format("%.1f", (currentRaw - 10000) / 10.0f);
        String socStr = (socRaw == (byte)0xFE) ? "异常" : (socRaw == (byte)0xFF) ? "无效" : String.valueOf(socRaw);
        String dcStatusStr = getDcStatus(dcStatus);
        String gearStr = parseGear(gear);
        String resistanceStr = (resistanceRaw > 60000) ? "无效" : String.valueOf(resistanceRaw);

        // 拼接JSON
        result.append("\"vehicleData\":{");
        result.append(String.format("\"vehicleStatus\":\"%s\",", vehicleStatusStr));
        result.append(String.format("\"chargeStatus\":\"%s\",", chargeStatusStr));
        result.append(String.format("\"runMode\":\"%s\",", runModeStr));
        result.append(String.format("\"speed\":\"%s\",", speedStr));
        result.append(String.format("\"mileage\":\"%s\",", mileageStr));
        result.append(String.format("\"totalVoltage\":\"%s\",", voltageStr));
        result.append(String.format("\"totalCurrent\":\"%s\",", currentStr));
        result.append(String.format("\"soc\":\"%s\",", socStr));
        result.append(String.format("\"dcDcStatus\":\"%s\",", dcStatusStr));
        result.append(String.format("\"gear\":\"%s\",", gearStr));
        result.append(String.format("\"insulationResistance\":\"%s\"", resistanceStr));
//        result.append(String.format("\"accelerator\":\"%s\",", acceleratorStr));
//        result.append(String.format("\"brake\":\"%s\"", brakeStr));
        result.append("},");

        vehicleData.setVehicleStatus((int) vehicleStatus & 0xFF);
        vehicleData.setChargeStatus((int) chargeStatus & 0xFF);
        vehicleData.setRunMode((int) runMode & 0xFF);
        vehicleData.setSpeed(speedStr);
        vehicleData.setMileage(mileageStr);
        vehicleData.setTotalVoltage(voltageStr);
        vehicleData.setTotalCurrent(currentStr);
        vehicleData.setSoc((int) socRaw & 0xFF);
        vehicleData.setDcDcStatus((int) dcStatus & 0xFF);
        vehicleData.setGear(gearStr);
        vehicleData.setInsulationResistance(resistanceRaw);

        return true;
    }

    private String getVehicleStatus(byte status) {
        switch (status) {
            case 0x01: return "车辆启动状态";
            case 0x02: return "熄火";
            case 0x03: return "其他状态";
            case (byte)0xFE: return "异常";
            case (byte)0xFF: return "无效";
            default: return String.valueOf(status);
        }
    }

    private String getChargeStatus(byte status) {
        switch (status) {
            case 0x01: return "停车充电";
            case 0x02: return "行驶充电";
            case 0x03: return "未充电状态";
            case 0x04: return "充电完成";
            case (byte)0xFE: return "异常";
            case (byte)0xFF: return "无效";
            default: return String.valueOf(status);
        }
    }

    private String getRunMode(byte mode) {
        switch (mode) {
            case 0x01: return "纯电";
            case 0x02: return "混动";
            case 0x03: return "燃油";
            case (byte)0xFE: return "异常";
            case (byte)0xFF: return "无效";
            default: return String.valueOf(mode);
        }
    }

    private String getDcStatus(byte status) {
        switch (status) {
            case 0x01: return "工作";
            case 0x02: return "断开";
            case (byte)0xFE: return "异常";
            case (byte)0xFF: return "无效";
            default: return String.valueOf(status);
        }
    }

    /**
     * 解析挡位（按附录A标准）
     */
    private String parseGear(byte gear) {
        int gearCode = gear & 0x0F; // 低4位表示挡位
        switch (gearCode) {
            case 0x00: return "空挡";
            case 0x01: return "1挡";
            case 0x02: return "2挡";
            case 0x03: return "3挡";
            case 0x04: return "4挡";
            case 0x05: return "5挡";
            case 0x06: return "6挡";
            case 0x0D: return "倒挡";
            case 0x0E: return "自动D挡";
            case 0x0F: return "停车P挡";
            default: return String.valueOf(gearCode);
        }
    }

    /**
     * 解析驱动电机数据（按标准：1字节个数 + N×12字节电机信息）
     */
    private int parseMotorData(byte[] data, int pos, int endMark, StringBuilder result, List<MotorData> motorDataList) {
        if (pos + 1 > endMark) {
            logger.error("驱动电机数据长度不足，跳过解析");
            return pos;
        }

        int motorCount = data[pos] & 0xFF; // 驱动电机个数（1字节）
        pos++;

        // 校验电机数据长度（每个电机12字节）
        if (motorCount <= 0 || pos + motorCount * 12 > endMark) {
            logger.error("驱动电机个数无效（{}）或数据不足，跳过解析", motorCount);
            return pos;
        }

        result.append(String.format("\"motorCount\":%d,\"motors\":[", motorCount));
        for (int i = 0; i < motorCount; i++) {
            int motorPos = pos + i * 12;
            int seq = data[motorPos] & 0xFF;
            byte status = data[motorPos + 1];
            int controllerTempRaw = data[motorPos + 2] & 0xFF;
            int speedRaw = ((data[motorPos + 3] & 0xFF) << 8) | (data[motorPos + 4] & 0xFF);
            int torqueRaw = ((data[motorPos + 5] & 0xFF) << 8) | (data[motorPos + 6] & 0xFF);
            int motorTempRaw = data[motorPos + 7] & 0xFF;
            int controllerVoltageRaw = ((data[motorPos + 8] & 0xFF) << 8) | (data[motorPos + 9] & 0xFF);
            int controllerCurrentRaw = ((data[motorPos + 10] & 0xFF) << 8) | (data[motorPos + 11] & 0xFF);

            // 处理异常值与偏移量
            String statusStr = getMotorStatus(status);
            String controllerTempStr = (controllerTempRaw == 0xFE) ? "异常" : (controllerTempRaw == 0xFF) ? "无效" : String.valueOf(controllerTempRaw - 40);
            String speedStr = (speedRaw == 0xFFFE) ? "异常" : (speedRaw == 0xFFFF) ? "无效" : String.valueOf(speedRaw - 20000);
            String torqueStr = (torqueRaw == 0xFFFE) ? "异常" : (torqueRaw == 0xFFFF) ? "无效" : String.format("%.1f", (torqueRaw - 20000) / 10.0f);
            String motorTempStr = (motorTempRaw == 0xFE) ? "异常" : (motorTempRaw == 0xFF) ? "无效" : String.valueOf(motorTempRaw - 40);
            String controllerVoltageStr = (controllerVoltageRaw == 0xFFFE) ? "异常" : (controllerVoltageRaw == 0xFFFF) ? "无效" : String.format("%.1f", controllerVoltageRaw / 10.0f);
            String controllerCurrentStr = (controllerCurrentRaw == 0xFFFE) ? "异常" : (controllerCurrentRaw == 0xFFFF) ? "无效" : String.format("%.1f", (controllerCurrentRaw - 10000) / 10.0f);

            // 拼接电机JSON
            if (i > 0) result.append(",");
            result.append("{");
            result.append(String.format("\"motorSeq\":%d,", seq));
            result.append(String.format("\"motorStatus\":\"%s\",", statusStr));
            result.append(String.format("\"controllerTemp\":\"%s\",", controllerTempStr));
            result.append(String.format("\"motorSpeed\":\"%s\",", speedStr));
            result.append(String.format("\"motorTorque\":\"%s\",", torqueStr));
            result.append(String.format("\"motorTemp\":\"%s\",", motorTempStr));
            result.append(String.format("\"controllerVoltage\":\"%s\",", controllerVoltageStr));
            result.append(String.format("\"controllerCurrent\":\"%s\"", controllerCurrentStr));
            result.append("}");

            MotorData motorData = new MotorData();
            motorData.setMotorSeq(seq);
            motorData.setMotorStatus((int) status & 0xFF);
            if(controllerTempRaw == 0xFE || controllerTempRaw == 0xFF){
                motorData.setControllerTemp(controllerTempRaw);
            } else {
                motorData.setControllerTemp(controllerTempRaw - 40);
            }
            if(speedRaw == 0xFFFE || speedRaw == 0xFFFF){
                motorData.setMotorSpeed(speedRaw);
            } else {
                motorData.setMotorSpeed(speedRaw - 20000);
            }
            motorData.setMotorTorque(torqueStr);
            if(motorTempRaw == 0xFE || motorTempRaw == 0xFF){
                motorData.setMotorTemp(motorTempRaw);
            } else {
                motorData.setMotorTemp(motorTempRaw - 40);
            }
            motorData.setControllerVoltage(controllerVoltageStr);
            motorData.setControllerCurrent(controllerCurrentStr);
            motorDataList.add(motorData);
        }
        result.append("],");
        return pos + motorCount * 12;
    }

    private String getMotorStatus(byte motorStatus) {
        switch (motorStatus) {
            case 0x01: return "耗电";
            case 0x02: return "发电";
            case 0x03: return "关闭状态";
            case 0x04: return "准备状态";
            case (byte)0xFE: return "异常";
            case (byte)0xFF: return "无效";
            default: return String.valueOf(motorStatus);
        }
    }

    /**
     * 解析燃料电池数据（修复：温度探针总数为2字节WORD类型，按标准解析）
     */
    private int parseFuelCellData(byte[] data, int pos, int endMark, StringBuilder result, FuelCellData fuelCellData) {
        // 燃料电池基础数据长度：8字节（2电压+2电流+2消耗率+2探针数）
        if (pos + 8 > endMark) {
            logger.error("燃料电池数据长度不足（最小8字节），跳过解析");
            return pos;
        }

        // 解析基础字段
        int voltageRaw = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
        int currentRaw = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
        int consumptionRaw = ((data[pos + 4] & 0xFF) << 8) | (data[pos + 5] & 0xFF);
        int probeCount = ((data[pos + 6] & 0xFF) << 8) | (data[pos + 7] & 0xFF); // 修复：2字节WORD类型
//        int maxTempRaw = ((data[pos + 8] & 0xFF) << 8) | (data[pos + 9] & 0xFF);
//        int maxPressureRaw = ((data[pos + 10] & 0xFF) << 8) | (data[pos + 11] & 0xFF);
        pos += 8; // 基础数据8字节

        // 处理异常值
        String voltageStr = (voltageRaw == 0xFFFE) ? "异常" : (voltageRaw == 0xFFFF) ? "无效" : String.format("%.1f", voltageRaw / 10.0f);
        String currentStr = (currentRaw == 0xFFFE) ? "异常" : (currentRaw == 0xFFFF) ? "无效" : String.format("%.1f", currentRaw / 10.0f);
        String consumptionStr = (consumptionRaw == 0xFFFE) ? "异常" : (consumptionRaw == 0xFFFF) ? "无效" : String.format("%.2f", consumptionRaw / 100.0f);
        String probeCountStr = (probeCount == 0xFFFE) ? "异常" : (probeCount == 0xFFFF) ? "无效" : String.valueOf(probeCount);
//        String maxTempStr = (maxTempRaw == 0xFFFE) ? "异常" : (maxTempRaw == 0xFFFF) ? "无效" : String.valueOf((maxTempRaw / 10.0f) - 40);
//        String maxPressureStr = (maxPressureRaw == 0xFFFE) ? "异常" : (maxPressureRaw == 0xFFFF) ? "无效" : String.valueOf(maxPressureRaw / 10.0f);

        // 拼接基础JSON
        result.append("\"fuelCellData\":{");
        result.append(String.format("\"fuelVoltage\":\"%s\",", voltageStr));
        result.append(String.format("\"fuelCurrent\":\"%s\",", currentStr));
        result.append(String.format("\"fuelConsumption\":\"%s\",", consumptionStr));
        result.append(String.format("\"probeCount\":\"%s\"", probeCountStr));
//        result.append(String.format("\"maxTemp\":\"%s\",", maxTempStr));
//        result.append(String.format("\"maxPressure\":\"%s\"", maxPressureStr));

        // 解析探针温度（1字节/个）
        StringBuilder tempsArray = new StringBuilder("[");
        if (probeCount > 0 && probeCount != 0xFFFE && probeCount != 0xFFFF && pos + probeCount <= endMark) {
            result.append(",\"probeTemps\":[");
            for (int i = 0; i < probeCount; i++) {
                int tempRaw = data[pos + i] & 0xFF;
                String tempStr = (tempRaw == 0xFE) ? "异常" : (tempRaw == 0xFF) ? "无效" : String.valueOf(tempRaw - 40);
                if (i > 0) {
                    result.append(",");
                    tempsArray.append(",");
                }
                result.append("\"").append(tempStr).append("\"");
                tempsArray.append("\"").append(tempStr).append("\"");
            }
            result.append("]");
            pos += probeCount;
        }

        tempsArray.append("]");

        // 解析氢系统最高温度（2字节WORD）
        if (pos + 2 <= endMark) {
            int maxTempRaw = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
            String maxTempStr = (maxTempRaw == 0xFFFE) ? "异常" : (maxTempRaw == 0xFFFF) ? "无效" : String.format("%.1f", maxTempRaw / 10.0f - 40);
            result.append(String.format(",\"h2MaxTemp\":\"%s\"", maxTempStr));
            pos += 2;
            fuelCellData.setH2MaxTemp(maxTempStr);
        }

        // 解析氢系统最高温度探针代号（1字节BYTE）
        if (pos + 1 <= endMark) {
            int maxTempProbe = data[pos] & 0xFF;
            String maxTempProbeStr = (maxTempProbe == 0xFE) ? "异常" : (maxTempProbe == 0xFF) ? "无效" : String.valueOf(maxTempProbe);
            result.append(String.format(",\"h2MaxTempProbeCode\":\"%s\"", maxTempProbeStr));
            pos++;
            fuelCellData.setH2MaxTempProbeCode(maxTempProbe);
        }

        // 解析氢气最高浓度（2字节WORD）
        if (pos + 2 <= endMark) {
            int maxConcentrationRaw = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
            String maxConcentrationStr = (maxConcentrationRaw == 0xFFFE) ? "异常" : (maxConcentrationRaw == 0xFFFF) ? "无效" : String.valueOf(maxConcentrationRaw);
            result.append(String.format(",\"h2MaxConcentration\":\"%s\"", maxConcentrationStr));
            pos += 2;
            fuelCellData.setH2MaxConcentration(maxConcentrationRaw);
        }

        // 解析氢气最高浓度传感器代号（1字节BYTE）
        if (pos + 1 <= endMark) {
            int maxConcentrationSensor = data[pos] & 0xFF;
            String maxConcentrationSensorStr = (maxConcentrationSensor == 0xFE) ? "异常" : (maxConcentrationSensor == 0xFF) ? "无效" : String.valueOf(maxConcentrationSensor);
            result.append(String.format(",\"h2MaxConcentrationSensorCode\":\"%s\"", maxConcentrationSensorStr));
            pos++;
            fuelCellData.setH2MaxConcentrationSensorCode(maxConcentrationSensor);
        }

        // 解析氢气最高压力（2字节WORD）
        if (pos + 2 <= endMark) {
            int maxPressureRaw = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF);
            String maxPressureStr = (maxPressureRaw == 0xFFFE) ? "异常" : (maxPressureRaw == 0xFFFF) ? "无效" : String.format("%.1f", maxPressureRaw / 10.0f);
            result.append(String.format(",\"h2MaxPressure\":\"%s\"", maxPressureStr));
            fuelCellData.setH2MaxPressure(maxPressureStr);
            pos += 2;
        }

        // 解析氢气最高压力传感器代号（1字节BYTE）
        if (pos + 1 <= endMark) {
            int maxPressureSensor = data[pos] & 0xFF;
            String maxPressureSensorStr = (maxPressureSensor == 0xFE) ? "异常" : (maxPressureSensor == 0xFF) ? "无效" : String.valueOf(maxPressureSensor);
            result.append(String.format(",\"h2MaxPressureSensorCode\":\"%s\"", maxPressureSensorStr));
            pos++;
            fuelCellData.setH2MaxPressureSensorCode(maxPressureSensor);
        }

        // 解析高压DC/DC状态（1字节BYTE）
        if (pos + 1 <= endMark) {
            int dcStatus = data[pos] & 0xFF;
            String dcStatusStr = (dcStatus == 0x01) ? "工作" : (dcStatus == 0x02) ? "断开" :
                    (dcStatus == 0xFE) ? "异常" : (dcStatus == 0xFF) ? "无效" : String.valueOf(dcStatus);
            result.append(String.format(",\"highVoltageDcDcStatus\":\"%s\"", dcStatusStr));
            pos++;
            fuelCellData.setHighVoltageDcDcStatus(dcStatus);
        }

        result.append("},");

        fuelCellData.setFuelVoltage(voltageStr);
        fuelCellData.setFuelCurrent(currentStr);
        fuelCellData.setFuelConsumption(consumptionStr);
        fuelCellData.setProbeCount(probeCount);
        fuelCellData.setProbeTemps(tempsArray.toString());

        return pos;
    }

    /**
     * 解析发动机数据（按标准定义）
     */
    private boolean parseEngineData(byte[] data, int pos, int endMark, StringBuilder result, EngineData engineData) {
        if (pos + 5 > endMark) {
            logger.error("发动机数据长度不足（5字节），跳过解析");
            return false;
        }

        byte status = data[pos];
        int crankshaftSpeedRaw = ((data[pos + 1] & 0xFF) << 8) | (data[pos + 2] & 0xFF);
        int consumptionRaw = ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF);

        // 处理异常值
        String statusStr = getEngineStatus(status);
        String crankshaftSpeedStr = (crankshaftSpeedRaw == 0xFFFE) ? "异常" : (crankshaftSpeedRaw == 0xFFFF) ? "无效" : String.valueOf(crankshaftSpeedRaw);
        String consumptionStr = (consumptionRaw == 0xFFFE) ? "异常" : (consumptionRaw == 0xFFFF) ? "无效" : String.format("%.2f", consumptionRaw / 100.0f);

        // 拼接JSON
        result.append("\"engineData\":{");
        result.append(String.format("\"engineStatus\":\"%s\",", statusStr));
        result.append(String.format("\"crankshaftSpeed\":\"%s\",", crankshaftSpeedStr));
        result.append(String.format("\"fuelConsumption\":\"%s\"", consumptionStr));
        result.append("},");

        engineData.setEngineStatus(status & 0xFF);
        engineData.setCrankshaftSpeed(crankshaftSpeedRaw);
        engineData.setFuelConsumption(consumptionStr);

        return true;
    }

    private String getEngineStatus(byte engineStatus) {
        switch (engineStatus) {
            case 0x01: return "启动状态";
            case 0x02: return "关闭状态";
            case (byte)0xFE: return "异常";
            case (byte)0xFF: return "无效";
            default: return String.valueOf(engineStatus);
        }
    }

    /**
     * 解析车辆位置数据（按标准处理经纬度偏移）
     */
    private boolean parseLocationData(byte[] data, int pos, int endMark, StringBuilder result, LocationData locationData) {
        if (pos + 9 > endMark) {
            logger.error("车辆位置数据长度不足（9字节），跳过解析");
            return false;
        }

        byte locationStatus = data[pos];
        long longitudeRaw = ((long) (data[pos + 1] & 0xFF) << 24) | ((data[pos + 2] & 0xFF) << 16) |
                ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF);
        long latitudeRaw = ((long) (data[pos + 5] & 0xFF) << 24) | ((data[pos + 6] & 0xFF) << 16) |
                ((data[pos + 7] & 0xFF) << 8) | (data[pos + 8] & 0xFF);

        // 解析定位状态（位0：有效/无效，位1：北纬/南纬，位2：东经/西经）
        boolean valid = (locationStatus & 0x01) == 0;
        String latDir = (locationStatus & 0x02) == 0 ? "N" : "S";
        String lonDir = (locationStatus & 0x04) == 0 ? "E" : "W";

        // 经纬度转换（标准：×10^6度）
        double longitude = longitudeRaw / 1000000.0;
        double latitude = latitudeRaw / 1000000.0;

        // 拼接JSON
        result.append("\"locationData\":{");
        result.append(String.format("\"locationValid\":%b,", valid));
        result.append(String.format("\"longitude\":\"%.6f%s\",", longitude, lonDir));
        result.append(String.format("\"latitude\":\"%.6f%s\"", latitude, latDir));
        result.append("},");

        byte byteValid = 1;
        if(valid) {
            byteValid = 0;
        }
        locationData.setLocationValid(byteValid);
        locationData.setLongitude(String.format("%.6f%s", longitude, lonDir));
        locationData.setLatitude(String.format("%.6f%s", latitude, latDir));

        return true;
    }

    /**
     * 解析极值数据（按标准处理偏移量与异常值）
     */
    private boolean parseExtremeData(byte[] data, int pos, int endMark, StringBuilder result, ExtremeData extremeData) {
        if (pos + 14 > endMark) {
            logger.error("极值数据长度不足（14字节），跳过解析");
            return false;
        }

        // 解析字段
        int maxVoltSys = data[pos] & 0xFF;
        int maxVoltCell = data[pos + 1] & 0xFF;
        int maxVoltRaw = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF);
        int minVoltSys = data[pos + 4] & 0xFF;
        int minVoltCell = data[pos + 5] & 0xFF;
        int minVoltRaw = ((data[pos + 6] & 0xFF) << 8) | (data[pos + 7] & 0xFF);
        int maxTempSys = data[pos + 8] & 0xFF;
        int maxTempProbe = data[pos + 9] & 0xFF;
        int maxTempRaw = data[pos + 10] & 0xFF;
        int minTempSys = data[pos + 11] & 0xFF;
        int minTempProbe = data[pos + 12] & 0xFF;
        int minTempRaw = data[pos + 13] & 0xFF;

        // 处理异常值与单位转换
        String maxVoltSysStr = (maxVoltSys == 0xFE) ? "异常" : (maxVoltSys == 0xFF) ? "无效" : String.valueOf(maxVoltSys);
        String maxVoltCellStr = (maxVoltCell == 0xFE) ? "异常" : (maxVoltCell == 0xFF) ? "无效" : String.valueOf(maxVoltCell);
        String maxVoltStr = (maxVoltRaw == 0xFFFE) ? "异常" : (maxVoltRaw == 0xFFFF) ? "无效" : String.format("%.3f", maxVoltRaw / 1000.0f);
        String minVoltSysStr = (minVoltSys == 0xFE) ? "异常" : (minVoltSys == 0xFF) ? "无效" : String.valueOf(minVoltSys);
        String minVoltCellStr = (minVoltCell == 0xFE) ? "异常" : (minVoltCell == 0xFF) ? "无效" : String.valueOf(minVoltCell);
        String minVoltStr = (minVoltRaw == 0xFFFE) ? "异常" : (minVoltRaw == 0xFFFF) ? "无效" : String.format("%.3f", minVoltRaw / 1000.0f);
        String maxTempSysStr = (maxTempSys == 0xFE) ? "异常" : (maxTempSys == 0xFF) ? "无效" : String.valueOf(maxTempSys);
        String maxTempProbeStr = (maxTempProbe == 0xFE) ? "异常" : (maxTempProbe == 0xFF) ? "无效" : String.valueOf(maxTempProbe);
        String maxTempStr = (maxTempRaw == 0xFE) ? "异常" : (maxTempRaw == 0xFF) ? "无效" : String.valueOf(maxTempRaw - 40);
        String minTempSysStr = (minTempSys == 0xFE) ? "异常" : (minTempSys == 0xFF) ? "无效" : String.valueOf(minTempSys);
        String minTempProbeStr = (minTempProbe == 0xFE) ? "异常" : (minTempProbe == 0xFF) ? "无效" : String.valueOf(minTempProbe);
        String minTempStr = (minTempRaw == 0xFE) ? "异常" : (minTempRaw == 0xFF) ? "无效" : String.valueOf(minTempRaw - 40);

        // 拼接JSON
        result.append("\"extremeData\":{");
        result.append(String.format("\"maxVoltSys\":\"%s\",", maxVoltSysStr));
        result.append(String.format("\"maxVoltCell\":\"%s\",", maxVoltCellStr));
        result.append(String.format("\"maxVolt\":\"%s\",", maxVoltStr));
        result.append(String.format("\"minVoltSys\":\"%s\",", minVoltSysStr));
        result.append(String.format("\"minVoltCell\":\"%s\",", minVoltCellStr));
        result.append(String.format("\"minVolt\":\"%s\",", minVoltStr));
        result.append(String.format("\"maxTempSys\":\"%s\",", maxTempSysStr));
        result.append(String.format("\"maxTempProbe\":\"%s\",", maxTempProbeStr));
        result.append(String.format("\"maxTemp\":\"%s\",", maxTempStr));
        result.append(String.format("\"minTempSys\":\"%s\",", minTempSysStr));
        result.append(String.format("\"minTempProbe\":\"%s\",", minTempProbeStr));
        result.append(String.format("\"minTemp\":\"%s\"", minTempStr));
        result.append("},");

        extremeData.setHighestVoltageSystemNo(maxVoltSys);
        extremeData.setHighestVoltageCellNo(maxVoltCell);
        extremeData.setHighestCellVoltage(maxVoltStr);
        extremeData.setLowestVoltageSystemNo(minVoltSys);
        extremeData.setLowestVoltageCellNo(minVoltCell);
        extremeData.setLowestCellVoltage(minVoltStr);
        extremeData.setHighestTempSystemNo(maxTempSys);
        extremeData.setHighestTempProbeNo(maxTempProbe);
        extremeData.setHighestTempValue(maxTempRaw - 40);
        extremeData.setLowestTempSystemNo(minTempSys);
        extremeData.setLowestTempProbeNo(minTempProbe);
        extremeData.setLowestTempValue(minTempRaw - 40);

        return true;
    }

    /**
     * 解析报警数据（修复：解析故障代码列表，按标准处理多类型故障）
     */
    private int parseAlarmData(byte[] data, int pos, int endMark, StringBuilder result) {
        if (pos + 9 > endMark) {
            logger.error("报警数据长度不足（最小9字节），跳过解析");
            return pos;
        }

        boolean bHasAlarm = false;

        // 解析基础报警字段
        int maxAlarmLevel = data[pos] & 0xFF;
        long generalAlarm = ((long) (data[pos + 1] & 0xFF) << 24) | ((data[pos + 2] & 0xFF) << 16) |
                ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF);
        pos += 5;

        // 处理报警等级
        String maxAlarmLevelStr = (maxAlarmLevel == 0xFE) ? "异常" : (maxAlarmLevel == 0xFF) ? "无效" : String.valueOf(maxAlarmLevel);

        // 拼接基础JSON
        result.append("\"alarmData\":{");
        result.append(String.format("\"maxAlarmLevel\":\"%s\"", maxAlarmLevelStr));
        List<String> alarmTypes = parseGeneralAlarm(generalAlarm);
        if(!alarmTypes.isEmpty()){
            bHasAlarm = true;
            result.append(String.format(",\"alarmTypes\":\"%s\"", String.join(",", alarmTypes)));
        }

        // 解析可充电储能装置故障（1字节个数 + N×4字节故障代码）
        if (pos + 1 <= endMark) {
            int energyFaultCount = data[pos] & 0xFF;
            pos++;
            if (energyFaultCount > 0 && energyFaultCount < 0xFE && pos + energyFaultCount * 4 <= endMark) {
                bHasAlarm = true;
                result.append(",\"energyStorageFaults\":[");
                for (int i = 0; i < energyFaultCount; i++) {
                    long faultCode = ((long) (data[pos + i * 4] & 0xFF) << 24) | ((data[pos + i * 4 + 1] & 0xFF) << 16) |
                            ((data[pos + i * 4 + 2] & 0xFF) << 8) | (data[pos + i * 4 + 3] & 0xFF);
                    if (i > 0) result.append(",");
                    result.append(String.format("\"%d\"", faultCode));
                }
                result.append("]");
                pos += energyFaultCount * 4;
            }
        }

        // 解析驱动电机故障
        if (pos + 1 <= endMark) {
            int motorFaultCount = data[pos] & 0xFF;
            pos++;
            if (motorFaultCount > 0 && motorFaultCount < 0xFE && pos + motorFaultCount * 4 <= endMark) {
                bHasAlarm = true;
                result.append(",\"motorFaults\":[");
                for (int i = 0; i < motorFaultCount; i++) {
                    long faultCode = ((long) (data[pos + i * 4] & 0xFF) << 24) | ((data[pos + i * 4 + 1] & 0xFF) << 16) |
                            ((data[pos + i * 4 + 2] & 0xFF) << 8) | (data[pos + i * 4 + 3] & 0xFF);
                    if (i > 0) result.append(",");
                    result.append(String.format("\"%d\"", faultCode));
                }
                result.append("]");
                pos += motorFaultCount * 4;
            }
        }

        // 解析发动机故障
        if (pos + 1 <= endMark) {
            int engineFaultCount = data[pos] & 0xFF;
            pos++;
            if (engineFaultCount > 0 && engineFaultCount < 0xFE && pos + engineFaultCount * 4 <= endMark) {
                bHasAlarm = true;
                result.append(",\"engineFaults\":[");
                for (int i = 0; i < engineFaultCount; i++) {
                    long faultCode = ((long) (data[pos + i * 4] & 0xFF) << 24) | ((data[pos + i * 4 + 1] & 0xFF) << 16) |
                            ((data[pos + i * 4 + 2] & 0xFF) << 8) | (data[pos + i * 4 + 3] & 0xFF);
                    if (i > 0) result.append(",");
                    result.append(String.format("\"%d\"", faultCode));
                }
                result.append("]");
                pos += engineFaultCount * 4;
            }
        }

        // 解析其他故障
        if (pos + 1 <= endMark) {
            int otherFaultCount = data[pos] & 0xFF;
            pos++;
            if (otherFaultCount > 0 && otherFaultCount < 0xFE && pos + otherFaultCount * 4 <= endMark) {
                bHasAlarm = true;
                result.append(",\"otherFaults\":[");
                for (int i = 0; i < otherFaultCount; i++) {
                    long faultCode = ((long) (data[pos + i * 4] & 0xFF) << 24) | ((data[pos + i * 4 + 1] & 0xFF) << 16) |
                            ((data[pos + i * 4 + 2] & 0xFF) << 8) | (data[pos + i * 4 + 3] & 0xFF);
                    if (i > 0) result.append(",");
                    result.append(String.format("\"%d\"", faultCode));
                }
                result.append("]");
                pos += otherFaultCount * 4;
            }
        }

        result.append("},");

        if(bHasAlarm){
            //TODO: 报错报警数据到mysql
        }
        return pos;
    }

    /**
     * 解析通用报警标志位，返回对应的报警类型字符串列表
     */
    private List<String> parseGeneralAlarm(long generalAlarm) {
        List<String> alarmTypes = new ArrayList<>();

        // 按位检查每个报警标志
        for (int bit = 0; bit <= 18; bit++) {
            if ((generalAlarm & (1L << bit)) != 0) {
                switch (bit) {
                    case 0:
                        alarmTypes.add("温度差异报警");
                        break;
                    case 1:
                        alarmTypes.add("电池高温报警");
                        break;
                    case 2:
                        alarmTypes.add("车载储能装置类型过压报警");
                        break;
                    case 3:
                        alarmTypes.add("车载储能装置类型欠压报警");
                        break;
                    case 4:
                        alarmTypes.add("SOC低报警");
                        break;
                    case 5:
                        alarmTypes.add("单体电池过压报警");
                        break;
                    case 6:
                        alarmTypes.add("单体电池欠压报警");
                        break;
                    case 7:
                        alarmTypes.add("SOC过高报警");
                        break;
                    case 8:
                        alarmTypes.add("SOC跳变报警");
                        break;
                    case 9:
                        alarmTypes.add("可充电储能系统不匹配报警");
                        break;
                    case 10:
                        alarmTypes.add("电池单体一致性差报警");
                        break;
                    case 11:
                        alarmTypes.add("绝缘报警");
                        break;
                    case 12:
                        alarmTypes.add("DC-DC温度报警");
                        break;
                    case 13:
                        alarmTypes.add("制动系统报警");
                        break;
                    case 14:
                        alarmTypes.add("DC-DC状态报警");
                        break;
                    case 15:
                        alarmTypes.add("驱动电机控制器温度报警");
                        break;
                    case 16:
                        alarmTypes.add("高压互锁状态报警");
                        break;
                    case 17:
                        alarmTypes.add("驱动电机温度报警");
                        break;
                    case 18:
                        alarmTypes.add("车载储能装置类型过充报警");
                        break;
                    default:
                        // 19~31位预留
                        break;
                }
            }
        }
        return alarmTypes;
    }

    /**
     * 解析GB32960实时数据单元
     */
    private String parseRealTimeData(byte[] data, int offset, int length) {
        StringBuilder result = new StringBuilder();
        int pos = offset;

        // 数据采集时间 (6 bytes)
        if (pos + 6 <= offset + length) {
            int year = 2000 + (data[pos] & 0xFF);
            int month = data[pos + 1] & 0xFF;
            int day = data[pos + 2] & 0xFF;
            int hour = data[pos + 3] & 0xFF;
            int minute = data[pos + 4] & 0xFF;
            int second = data[pos + 5] & 0xFF;
            result.append(String.format("\"time\":\"%04d-%02d-%02d %02d:%02d:%02d\",",
                    year, month, day, hour, minute, second));
            pos += 6;
        }

        // 整车数据 (24 bytes)
        if (pos + 24 <= offset + length) {
            int vehicleStatus = data[pos] & 0xFF; // 车辆状态
            int chargeStatus = data[pos + 1] & 0xFF; // 充电状态
            int mode = data[pos + 2] & 0xFF; // 运行模式
            float speed = ((data[pos + 3] & 0xFF) * 256 + (data[pos + 4] & 0xFF)) / 10.0f; // 车速(km/h)
            long mileage = ((data[pos + 5] & 0xFF) << 24) | ((data[pos + 6] & 0xFF) << 16) |
                    ((data[pos + 7] & 0xFF) << 8) | (data[pos + 8] & 0xFF); // 累计里程(km)
            int voltage = ((data[pos + 9] & 0xFF) << 8) | (data[pos + 10] & 0xFF); // 总电压(0.1V)
            int current = ((data[pos + 11] & 0xFF) << 8) | (data[pos + 12] & 0xFF); // 总电流(0.1A-4000)
            int soc = data[pos + 13] & 0xFF; // SOC(%)
            int dcStatus = data[pos + 14] & 0xFF; // DC-DC状态
            int gear = data[pos + 15] & 0xFF; // 挡位信息
            int resistance = ((data[pos + 16] & 0xFF) << 8) | (data[pos + 17] & 0xFF); // 绝缘电阻(kΩ)
            int accelerator = data[pos + 18] & 0xFF; // 加速踏板行程值(%)
            int brake = data[pos + 19] & 0xFF; // 制动踏板状态
            // 剩余4字节未使用或保留字段
            int reserved1 = data[pos + 20] & 0xFF;
            int reserved2 = data[pos + 21] & 0xFF;
            int reserved3 = data[pos + 22] & 0xFF;
            int reserved4 = data[pos + 23] & 0xFF;

            result.append(String.format("\"vehicleData\":{"));
            result.append(String.format("\"vehicleStatus\":%d,", vehicleStatus));
            result.append(String.format("\"chargeStatus\":%d,", chargeStatus));
            result.append(String.format("\"mode\":%d,", mode));
            result.append(String.format("\"speed\":%.1f,", speed));
            result.append(String.format("\"mileage\":%d,", mileage));
            result.append(String.format("\"voltage\":%d,", voltage));
            result.append(String.format("\"current\":%d,", current));
            result.append(String.format("\"soc\":%d,", soc));
            result.append(String.format("\"dcStatus\":%d,", dcStatus));
            result.append(String.format("\"gear\":%d,", gear));
            result.append(String.format("\"resistance\":%d,", resistance));
            result.append(String.format("\"accelerator\":%d,", accelerator));
            result.append(String.format("\"brake\":%d", brake));
            // 可选择是否包含保留字段
            // result.append(String.format(",\"reserved1\":%d,\"reserved2\":%d,\"reserved3\":%d,\"reserved4\":%d", reserved1, reserved2, reserved3, reserved4));
            result.append("},");
            pos += 24;
        }

        // 驱动电机数据
        if (pos + 1 <= offset + length) {
            int motorCount = data[pos] & 0xFF; // 驱动电机个数
            result.append(String.format("\"motorCount\":%d,", motorCount));
            pos += 1;

            // 解析各驱动电机信息 (每台电机12字节)
            if (motorCount > 0 && pos + (motorCount * 12) <= offset + length) {
                result.append("\"motors\":[");
                for (int i = 0; i < motorCount; i++) {
                    int motorSeq = data[pos] & 0xFF; // 电机序号
                    int motorStatus = data[pos + 1] & 0xFF; // 电机状态
                    int controllerTemp = data[pos + 2] & 0xFF; // 电机控制器温度(°C-40)
                    int speed = ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF); // 电机转速(r/min)
                    int torque = ((data[pos + 5] & 0xFF) << 8) | (data[pos + 6] & 0xFF); // 电机转矩(0.1Nm-2000)
                    int motorTemp = data[pos + 7] & 0xFF; // 电机温度(°C-40)
                    int controllerVoltage = ((data[pos + 8] & 0xFF) << 8) | (data[pos + 9] & 0xFF); // 电机控制器输入电压(0.1V)
                    int controllerCurrent = ((data[pos + 10] & 0xFF) << 8) | (data[pos + 11] & 0xFF); // 电机控制器直流母线电流(0.1A-1000)

                    if (i > 0) result.append(",");
                    result.append("{");
                    result.append(String.format("\"motorSeq\":%d,", motorSeq));
                    result.append(String.format("\"motorStatus\":%d,", motorStatus));
                    result.append(String.format("\"controllerTemp\":%d,", controllerTemp));
                    result.append(String.format("\"speed\":%d,", speed));
                    result.append(String.format("\"torque\":%d,", torque));
                    result.append(String.format("\"motorTemp\":%d,", motorTemp));
                    result.append(String.format("\"controllerVoltage\":%d,", controllerVoltage));
                    result.append(String.format("\"controllerCurrent\":%d", controllerCurrent));
                    result.append("}");

                    pos += 12;
                }
                result.append("],");
            } else {
                pos += motorCount * 12; // 跳过电机数据
            }
        }

        // 燃料电池数据 (可变长度)
        if (pos + 11 <= offset + length) { // 至少需要9字节基础数据
            int fuelVoltage = ((data[pos] & 0xFF) << 8) | (data[pos + 1] & 0xFF); // 燃料电池电压(0.1V)
            int fuelCurrent = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF); // 燃料电池电流(0.1A)
            int fuelConsumption = ((data[pos + 4] & 0xFF) << 8) | (data[pos + 5] & 0xFF); // 燃料消耗率(0.01L/h)
            int probeCount = data[pos + 6] & 0xFF; // 燃料电池探针总数
            int maxTemp = ((data[pos + 7] & 0xFF) << 8) | (data[pos + 8] & 0xFF); // 氢系统最高温度(0.1K-40)
            int maxPressure = ((data[pos + 9] & 0xFF) << 8) | (data[pos + 10] & 0xFF); // 氢系统最高压力(0.1MPa)

            result.append(String.format("\"fuelCellData\":{"));
            result.append(String.format("\"fuelVoltage\":%d,", fuelVoltage));
            result.append(String.format("\"fuelCurrent\":%d,", fuelCurrent));
            result.append(String.format("\"fuelConsumption\":%d,", fuelConsumption));
            result.append(String.format("\"probeCount\":%d,", probeCount));
            result.append(String.format("\"maxTemp\":%d,", maxTemp));
            result.append(String.format("\"maxPressure\":%d", maxPressure));

            pos += 11; // 基础数据11字节

            // 处理探针温度数据
            if (probeCount > 0 && pos + probeCount <= offset + length) {
                result.append(",\"probeTemps\":[");
                for (int i = 0; i < probeCount; i++) {
                    if (i > 0) result.append(",");
                    result.append(String.format("%d", data[pos + i] & 0xFF)); // 探针温度(°C-40)
                }
                result.append("]");
                pos += probeCount;
            }
            result.append("},");
        }

        // 发动机数据
        if (pos + 5 <= offset + length) {
            int engineStatus = data[pos] & 0xFF; // 发动机状态
            int crankshaftSpeed = ((data[pos + 1] & 0xFF) << 8) | (data[pos + 2] & 0xFF); // 曲轴转速(r/min)
            int fuelConsumption = ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF); // 燃料消耗率(0.01L/h)

            result.append(String.format("\"engineData\":{"));
            result.append(String.format("\"engineStatus\":%d,", engineStatus));
            result.append(String.format("\"crankshaftSpeed\":%d,", crankshaftSpeed));
            result.append(String.format("\"fuelConsumption\":%d", fuelConsumption));
            result.append("},");
            pos += 5;
        }

        // 定位状态数据
        if (pos + 9 <= offset + length) {
            int locationStatus = data[pos] & 0xFF; // 定位状态
            int longitude = ((data[pos + 1] & 0xFF) << 24) | ((data[pos + 2] & 0xFF) << 16) |
                    ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF); // 经度(百万分之一度)
            int latitude = ((data[pos + 5] & 0xFF) << 24) | ((data[pos + 6] & 0xFF) << 16) |
                    ((data[pos + 7] & 0xFF) << 8) | (data[pos + 8] & 0xFF); // 纬度(百万分之一度)

            result.append(String.format("\"locationData\":{"));
            result.append(String.format("\"locationStatus\":%d,", locationStatus));
            result.append(String.format("\"longitude\":%d,", longitude));
            result.append(String.format("\"latitude\":%d", latitude));
            result.append("},");
            pos += 9;
        }

        // 极值数据
        if (pos + 14 <= offset + length) {
            int maxVoltageSys = data[pos] & 0xFF; // 最高电压电池子系统号
            int maxVoltageCell = data[pos + 1] & 0xFF; // 最高电压电池单体代号
            int maxVoltage = ((data[pos + 2] & 0xFF) << 8) | (data[pos + 3] & 0xFF); // 电池单体电压最高值(0.001V)
            int minVoltageSys = data[pos + 4] & 0xFF; // 最低电压电池子系统号
            int minVoltageCell = data[pos + 5] & 0xFF; // 最低电压电池单体代号
            int minVoltage = ((data[pos + 6] & 0xFF) << 8) | (data[pos + 7] & 0xFF); // 电池单体电压最低值(0.001V)
            int maxTempSys = data[pos + 8] & 0xFF; // 最高温度子系统号
            int maxTempProbe = data[pos + 9] & 0xFF; // 最高温度探针序号
            int maxTemp = data[pos + 10] & 0xFF; // 最高温度值(°C-40)
            int minTempSys = data[pos + 11] & 0xFF; // 最低温度子系统号
            int minTempProbe = data[pos + 12] & 0xFF; // 最低温度探针序号
            int minTemp = data[pos + 13] & 0xFF; // 最低温度值(°C-40)

            result.append(String.format("\"extremeData\":{"));
            result.append(String.format("\"maxVoltageSys\":%d,", maxVoltageSys));
            result.append(String.format("\"maxVoltageCell\":%d,", maxVoltageCell));
            result.append(String.format("\"maxVoltage\":%d,", maxVoltage));
            result.append(String.format("\"minVoltageSys\":%d,", minVoltageSys));
            result.append(String.format("\"minVoltageCell\":%d,", minVoltageCell));
            result.append(String.format("\"minVoltage\":%d,", minVoltage));
            result.append(String.format("\"maxTempSys\":%d,", maxTempSys));
            result.append(String.format("\"maxTempProbe\":%d,", maxTempProbe));
            result.append(String.format("\"maxTemp\":%d,", maxTemp));
            result.append(String.format("\"minTempSys\":%d,", minTempSys));
            result.append(String.format("\"minTempProbe\":%d,", minTempProbe));
            result.append(String.format("\"minTemp\":%d", minTemp));
            result.append("},");
            pos += 14;
        }

        // 报警数据
        if (pos + 5 <= offset + length) {
            int maxAlarmLevel = data[pos] & 0xFF; // 最高报警等级
            int generalAlarm = ((data[pos + 1] & 0xFF) << 24) | ((data[pos + 2] & 0xFF) << 16) |
                    ((data[pos + 3] & 0xFF) << 8) | (data[pos + 4] & 0xFF); // 通用报警标志

            result.append(String.format("\"alarmData\":{"));
            result.append(String.format("\"maxAlarmLevel\":%d,", maxAlarmLevel));
            result.append(String.format("\"generalAlarm\":%d", generalAlarm));
            // 还有更多报警字段，但需要检查数据长度
            result.append("}");
            pos += 5;
        }

        return "{" + result.toString().replaceAll(",$", "") + "}";
    }

    /**
     * 处理补发数据（修复：按标准标识为补发，处理3日内历史数据）
     */
    private void handleResendData(byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的补发数据上报 - VIN: {}", vin);
            return;
        }

        try {
            // 复用实时数据解析逻辑（补发数据格式与实时一致）
            StringBuilder parsedData = new StringBuilder();
            parsedData.append("{");

            int pos = offset;

            if (pos + 6 > offset + length) {
                logger.error("补发数据缺少6字节采集时间，丢弃 - VIN: {}", vin);
                return;
            }
            int collectYear = 2000 + (data[pos] & 0xFF);
            int collectMonth = data[pos + 1] & 0xFF;
            int collectDay = data[pos + 2] & 0xFF;
            int collectHour = data[pos + 3] & 0xFF;
            int collectMinute = data[pos + 4] & 0xFF;
            int collectSecond = data[pos + 5] & 0xFF;
            parsedData.append(String.format("\"collectTime\":\"%04d-%02d-%02d %02d:%02d:%02d\",",
                    collectYear, collectMonth, collectDay, collectHour, collectMinute, collectSecond));
            LocalDateTime collectTime = LocalDateTime.of(collectYear, collectMonth, collectDay, collectHour, collectMinute,
                    collectSecond);
            pos += 6; // 跳过已解析的6字节时间

            while (pos < offset + length) {
                if (pos + 1 > offset + length) break;
                byte infoType = data[pos];
                pos++;
                logger.info("补发数据类型:{}", infoType);
                switch (infoType) {
                    case 0x01:
                        VehicleData vehicleData = new VehicleData();
                        vehicleData.setVin(vin);
                        vehicleData.setCollectTime(collectTime);
                        boolean bParsed = parseVehicleData(data, pos, offset + length, parsedData, vehicleData);
                        if(bParsed) {
                            pos += 20;
                            databaseService.saveVehicleDataWithTimeCheck(vehicleData);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x02:
                        List<MotorData> motorDataList = new ArrayList<>();
                        pos = parseMotorData(data, pos, offset + length, parsedData, motorDataList);
                        for (MotorData motorData : motorDataList){
                            motorData.setVin(vin);
                            motorData.setCollectTime(collectTime);
                        }
                        databaseService.saveMotorDataWithTimeCheck(motorDataList);
                        break;
                    case 0x03:
                        FuelCellData fuelCellData = new FuelCellData();
                        fuelCellData.setVin(vin);
                        fuelCellData.setCollectTime(collectTime);
                        int oldPos = pos;
                        pos = parseFuelCellData(data, pos, offset + length, parsedData, fuelCellData);
                        if(pos > oldPos) {
                            databaseService.saveFuelCellDataWithTimeCheck(fuelCellData);
                        }
                        break;
                    case 0x04:
                        EngineData engineData = new EngineData();
                        engineData.setVin(vin);
                        engineData.setCollectTime(collectTime);
                        boolean bEngineParsed = parseEngineData(data, pos, offset + length, parsedData, engineData);
                        if(bEngineParsed){
                            pos += 5;
                            databaseService.saveEngineDataWithTimeCheck(engineData);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x05:
                        LocationData locationData = new LocationData();
                        locationData.setVin(vin);
                        locationData.setCollectTime(collectTime);
                        boolean bLocationParsed = parseLocationData(data, pos, offset + length, parsedData, locationData);
                        if(bLocationParsed) {
                            pos += 9;
                            databaseService.saveLocationDataWithTimeCheck(locationData);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x06:
                        ExtremeData extremeData = new ExtremeData();
                        extremeData.setVin(vin);
                        extremeData.setCollectTime(collectTime);
                        boolean bExtremeParsed = parseExtremeData(data, pos, offset + length, parsedData, extremeData);
                        if(bExtremeParsed) {
                            pos += 14;
                            databaseService.saveExtremeData(extremeData, true);
                        } else {
                            pos += 1;
                        }
                        break;
                    case 0x07:
                        pos = parseAlarmData(data, pos, offset + length, parsedData);
                        break;
                    default:
                        logger.warn("未知信息类型标志: 0x{}，跳过", String.format("%02X", infoType));
                        pos++;
                }
            }

            String dataJson = parsedData.toString().replaceAll(",$", "") + "}";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formattedTime = LocalDateTime.now().format(formatter);

            // 构造补发数据JSON（标识dataType为resend）
            String jsonData = String.format(
                    "{\"vin\":\"%s\",\"timestamp\":\"%s\",\"dataType\":\"resend\",\"data\":%s}",
                    vin, formattedTime, dataJson
            );

            // 日志与存储（按标准存储3日内数据）
            logger.info("处理补发数据 - VIN: {}, 数据长度: {}字节", vin, length);
            loggerDebug.info("补发数据解析结果: {}", jsonData);
            // TODO
//            databaseService.saveResendData(vin, jsonData, formattedTime); // 需在DatabaseService实现补发数据存储方法
            dataQueue.offer(jsonData);

        } catch (Exception e) {
            logger.error("处理补发数据异常 - VIN: {}", vin, e);
        }
    }

    /**
     * 处理车辆登出（按标准解析登出时间与流水号）
     */
    private void handleVehicleLogout(ChannelHandlerContext ctx, byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的车辆登出请求 - VIN: {}", vin);
            return;
        }

        // 标准车辆登出数据单元长度：6（时间）+2（流水号）=8字节
        if (length < 8) {
            logger.error("车辆登出数据长度不足（8字节），实际: {}", length);
            return;
        }

        // 解析登出信息
        int logoutTimeYear = 2000 + (data[offset] & 0xFF);
        int logoutTimeMonth = data[offset + 1] & 0xFF;
        int logoutTimeDay = data[offset + 2] & 0xFF;
        int logoutTimeHour = data[offset + 3] & 0xFF;
        int logoutTimeMinute = data[offset + 4] & 0xFF;
        int logoutTimeSecond = data[offset + 5] & 0xFF;
        int serialNo = ((data[offset + 6] & 0xFF) << 8) | (data[offset + 7] & 0xFF); // 登出流水号（与登入一致）

        logger.info("车辆登出 - VIN: {}, 时间: {}-{}-{} {}:{}:{}, 流水号: {}",
                vin, logoutTimeYear, logoutTimeMonth, logoutTimeDay, logoutTimeHour, logoutTimeMinute, logoutTimeSecond, serialNo);

        // 发送登出应答（按标准构造）
        byte[] response = new byte[24 + 8 + 1]; // 头24 + 数据8 + 校验1
        response[0] = START_DELIMITER_1;
        response[1] = START_DELIMITER_2;
        response[2] = 0x04; // 命令标识=车辆登出
        response[3] = RESPONSE_SUCCESS;
        setRspVin(vin, response);
        response[21] = ENCRYPTION_NONE;
        response[22] = 0x00; // 数据长度高字节（8）
        response[23] = 0x08; // 数据长度低字节
        System.arraycopy(data, offset, response, 24, 8); // 复用登出时间与流水号
        response[32] = calculateBcc(response, 2, 32);
        ctx.writeAndFlush(Unpooled.copiedBuffer(response));

        databaseService.saveVehicleLogout(vin, serialNo, LocalDateTime.of(logoutTimeYear, logoutTimeMonth, logoutTimeDay,
                logoutTimeHour, logoutTimeMinute, logoutTimeSecond));
    }

    /**
     * 处理平台登出（按标准重置认证状态）
     */
    private void handlePlatformLogout(ChannelHandlerContext ctx, byte[] data, int offset, int length) {
        // 标准平台登出数据单元长度：6（时间）+2（流水号）=8字节
        if (length < 8) {
            logger.error("平台登出数据长度不足（8字节），实际: {}", length);
            return;
        }

        // 解析登出信息
        int logoutTimeYear = 2000 + (data[offset] & 0xFF);
        int logoutTimeMonth = data[offset + 1] & 0xFF;
        int logoutTimeDay = data[offset + 2] & 0xFF;
        int logoutTimeHour = data[offset + 3] & 0xFF;
        int logoutTimeMinute = data[offset + 4] & 0xFF;
        int logoutTimeSecond = data[offset + 5] & 0xFF;
        int serialNo = ((data[offset + 6] & 0xFF) << 8) | (data[offset + 7] & 0xFF);

        logger.info("平台登出 - 时间: {}-{}-{} {}:{}:{}, 流水号: {}",
                logoutTimeYear, logoutTimeMonth, logoutTimeDay, logoutTimeHour, logoutTimeMinute, logoutTimeSecond, serialNo);

        // 重置认证状态
        authenticated = false;

        // 发送登出应答
        byte[] response = new byte[24 + 8 + 1];
        response[0] = START_DELIMITER_1;
        response[1] = START_DELIMITER_2;
        response[2] = 0x06; // 命令标识=平台登出
        response[3] = RESPONSE_SUCCESS;
        response[21] = ENCRYPTION_NONE;
        response[22] = 0x00;
        response[23] = 0x08;
        System.arraycopy(data, offset, response, 24, 8);
        response[32] = calculateBcc(response, 2, 32);
        ctx.writeAndFlush(Unpooled.copiedBuffer(response));
    }


    /**
     * 处理心跳（按附录B标准，响应空数据单元）
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, String vin) {
        if (!authenticated) {
            logger.error("未认证的心跳请求 - VIN: {}", vin);
            return;
        }

        // 心跳响应：数据单元长度=0
        byte[] response = new byte[24 + 0 + 1];
        response[0] = START_DELIMITER_1;
        response[1] = START_DELIMITER_2;
        response[2] = 0x07; // 命令标识=心跳
        response[3] = RESPONSE_SUCCESS;
        setRspVin(vin, response);
        response[21] = ENCRYPTION_NONE;
        response[22] = 0x00; // 数据长度=0
        response[23] = 0x00;
        response[24] = calculateBcc(response, 2, 24); // 数据单元为空，校验到索引23

        ctx.writeAndFlush(Unpooled.copiedBuffer(response));
        logger.debug("回复心跳 - VIN: {}", vin);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.warn("客户端{}读空闲超时，断开连接", ctx.channel().remoteAddress());
                ctx.close();
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理消息时发生异常", cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("客户端连接断开: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    /**
     * 字节数组转十六进制字符串(指定范围)
     */
    private String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            if(i <  offset + length - 1) {
                sb.append(String.format("%02X ", bytes[i]));
            } else{
                sb.append(String.format("%02X", bytes[i]));
            }
        }
        return sb.toString();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        isRunning.set(false);
        if (mqttSenderThread != null) {
            mqttSenderThread.interrupt();
        }

//        if (mqttClient != null && mqttClient.isConnected()) {
//            mqttClient.disconnect();
//            mqttClient.close();
//        }

        super.handlerRemoved(ctx);
    }
}
