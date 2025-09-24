package com.example.gb32960;

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
 * GB32960协议处理器
 */
public class Gb32960ProtocolHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Gb32960ProtocolHandler.class);
    private static final Logger loggerDebug = LoggerFactory.getLogger("logger.DEBUG_MSG");

    // GB32960协议常量
    private static final byte START_DELIMITER_1 = 0x23; // 起始符 #
    private static final byte START_DELIMITER_2 = 0x23; // 起始符 #
    private static final byte ENCRYPTION_NONE = 0x01; // 数据单元未加密
    private static final byte COMMAND_PLATFORM_LOGIN = 0x05; // 平台登录命令

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

    public Gb32960ProtocolHandler(Config config) {
        this.config = config;
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
        if (data.length < 26) {
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

        logger.info("接收到GB32960消息 - 命令: 0x{}, 应答: 0x{}, VIN: {}, 加密: 0x{}, 数据长度: {}",
                String.format("%02X", command), String.format("%02X", response),
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
            bcc ^= data[i];
        }
        return bcc;
    }

    /**
     * 处理平台登录
     */
    private void handlePlatformLogin(ChannelHandlerContext ctx, byte[] data, int offset, int length, String vin) {
        if (length < 17) {
            logger.error("平台登录数据长度不足");
            sendPlatformLoginResponse(ctx, (byte) 0x01, vin); // 失败
            return;
        }

        // 提取登录信息
        String username = new String(data, offset, 8, StandardCharsets.ISO_8859_1).trim();
        String password = new String(data, offset + 8, 8, StandardCharsets.ISO_8859_1).trim();
        String uniqueCode = new String(data, offset + 16, length - 16, StandardCharsets.ISO_8859_1).trim();

        String expectedUsername = config.getString("gb32960.platform.username");
        String expectedPassword = config.getString("gb32960.platform.password");
        String expectedUniqueCode = config.getString("gb32960.platform.unique-code");

        logger.info("平台登录请求 - 用户名: {}, 密码: {}, 唯一码: {}", username, password, uniqueCode);

        // 验证登录信息
        if (expectedUsername.equals(username) &&
            expectedPassword.equals(password) &&
            expectedUniqueCode.equals(uniqueCode)) {
            authenticated = true;
            logger.info("平台登录成功");
            sendPlatformLoginResponse(ctx, (byte) 0x00, vin); // 成功
        } else {
            logger.error("平台登录失败 - 用户名或密码错误");
            authenticated = false;
            sendPlatformLoginResponse(ctx, (byte) 0x01, vin); // 失败
        }
    }

    /**
     * 发送平台登录响应
     */
    private void sendPlatformLoginResponse(ChannelHandlerContext ctx, byte result, String vin) {
        byte[] response = new byte[26]; // 25字节数据 + 1字节校验码
        response[0] = START_DELIMITER_1; // 起始符1
        response[1] = START_DELIMITER_2; // 起始符2
        response[2] = 0x05; // 命令标识(平台登录)
        response[3] = 0x01; // 应答标识
        System.arraycopy(String.format("%-17s", vin != null ? vin : "").getBytes(StandardCharsets.ISO_8859_1), 0, response, 4, 17); // VIN
        response[21] = ENCRYPTION_NONE; // 加密方式
        response[22] = 0x00; // 数据单元长度高字节
        response[23] = 0x01; // 数据单元长度低字节
        response[24] = result; // 执行结果

        // 计算并添加校验码 (从命令标识到数据单元末尾的所有字节异或)
        response[25] = calculateBcc(response, 2, 25); // 从索引2(命令标识)到索引24(数据单元末尾)

        ctx.writeAndFlush(Unpooled.copiedBuffer(response));
        loggerDebug.info("发送平台登录响应: {}", bytesToHex(response));
    }

    /**
     * 处理车辆登录
     */
    private void handleVehicleLogin(ChannelHandlerContext ctx, byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的车辆登录请求");
            return;
        }

        if (length < 22) {
            logger.error("车辆登录数据长度不足");
            return;
        }

        // 解析车辆登录信息
        int timestamp = ((data[offset] & 0xFF) << 24) |
                ((data[offset + 1] & 0xFF) << 16) |
                ((data[offset + 2] & 0xFF) << 8) |
                (data[offset + 3] & 0xFF);

        byte loginResult = 0x00; // 成功

        // 构造响应消息
        byte[] response = new byte[30];
        response[0] = START_DELIMITER_1; // 起始符1
        response[1] = START_DELIMITER_2; // 起始符2
        response[2] = 0x01; // 命令标识(车辆登录)
        response[3] = 0x01; // 应答标识
        System.arraycopy(String.format("%-17s", vin).getBytes(StandardCharsets.ISO_8859_1), 0, response, 4, 17);
        response[21] = ENCRYPTION_NONE;
        response[22] = 0x00;
        response[23] = 0x02;
        response[24] = loginResult;
        response[25] = (byte) (timestamp >> 24);
        response[26] = (byte) (timestamp >> 16);
        response[27] = (byte) (timestamp >> 8);
        response[28] = (byte) timestamp;

        // 计算并添加校验码
        response[29] = calculateBcc(response, 2, 29);

        ctx.writeAndFlush(Unpooled.copiedBuffer(response));
        logger.info("车辆登录成功: {}", vin);
    }

    /**
     * 处理实时数据
     */
    private void handleRealTimeData(byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的实时数据上报");
            return;
        }

        try {
            // 构造JSON数据
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"vin\":\"").append(vin).append("\",");
            jsonBuilder.append("\"timestamp\":\"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",");
            jsonBuilder.append("\"dataType\":\"realtime\",");
            jsonBuilder.append("\"data\":\"").append(bytesToHex(data, offset, length)).append("\"");
            jsonBuilder.append("}");

            // 添加到发送队列
//            dataQueue.offer(jsonBuilder.toString());
            logger.debug("处理实时数据: {} 字节", length);
            loggerDebug.info("处理实时数据: {}", jsonBuilder.toString());
        } catch (Exception e) {
            logger.error("处理实时数据异常", e);
        }
    }

    /**
     * 处理补发数据
     */
    private void handleResendData(byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的补发数据上报");
            return;
        }

        try {
            // 构造JSON数据
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"vin\":\"").append(vin).append("\",");
            jsonBuilder.append("\"timestamp\":\"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",");
            jsonBuilder.append("\"dataType\":\"resend\",");
            jsonBuilder.append("\"data\":\"").append(bytesToHex(data, offset, length)).append("\"");
            jsonBuilder.append("}");

            // 添加到发送队列
//            dataQueue.offer(jsonBuilder.toString());
            logger.debug("处理补发数据: {} 字节", length);
            loggerDebug.info("处理补发数据: {}", jsonBuilder.toString());
        } catch (Exception e) {
            logger.error("处理补发数据异常", e);
        }
    }

    /**
     * 处理车辆登出
     */
    private void handleVehicleLogout(ChannelHandlerContext ctx, byte[] data, int offset, int length, String vin) {
        if (!authenticated) {
            logger.error("未认证的车辆登出请求");
            return;
        }

        logger.info("车辆登出: {}", vin);
        // 可以在这里添加车辆登出的处理逻辑
    }

    /**
     * 处理平台登出
     */
    private void handlePlatformLogout(ChannelHandlerContext ctx, byte[] data, int offset, int length) {
        logger.info("平台登出");
        authenticated = false;
        // 可以在这里添加平台登出的处理逻辑
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, String vin) {
        if (!authenticated) {
            logger.error("未认证的心跳请求");
            return;
        }

        // 回复心跳
        byte[] response = new byte[24]; // 增加1字节以容纳第二个起始符
        response[0] = START_DELIMITER_1; // 起始符1
        response[1] = START_DELIMITER_2; // 起始符2
        response[2] = 0x07; // 命令标识(心跳)
        response[3] = 0x01; // 应答标识
        System.arraycopy(String.format("%-17s", vin != null ? vin : "").getBytes(StandardCharsets.ISO_8859_1), 0, response, 4, 17);
        response[21] = ENCRYPTION_NONE;
        response[22] = 0x00;
        response[23] = 0x00;

        ctx.writeAndFlush(Unpooled.copiedBuffer(response));
        logger.debug("回复心跳消息");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                logger.error("客户端{}读空闲超时", ctx.channel().remoteAddress());
//                ctx.close();
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
