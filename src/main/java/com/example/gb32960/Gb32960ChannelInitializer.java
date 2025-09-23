package com.example.gb32960;

import com.typesafe.config.Config;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * GB32960协议Channel初始化器
 */
public class Gb32960ChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Config config;

    public Gb32960ChannelInitializer(Config config) {
        this.config = config;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                // 空闲检测：30秒无读操作则触发
                .addLast("idleStateHandler", new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                // GB32960协议解码器，处理TCP粘包和半包问题
                .addLast("gb32960Decoder", new Gb32960Decoder())
                // 字节数组编码器
                .addLast("byteArrayEncoder", new ByteArrayEncoder())
                // GB32960协议处理器
                .addLast("gb32960Handler", new Gb32960ProtocolHandler(config));
    }
}
