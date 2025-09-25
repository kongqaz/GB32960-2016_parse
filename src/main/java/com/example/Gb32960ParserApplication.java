package com.example;

import com.example.gb32960.Gb32960ChannelInitializer;
import com.example.service.HttpService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GB32960协议解析器主程序
 */
public class Gb32960ParserApplication {
    private static final Logger logger = LoggerFactory.getLogger(Gb32960ParserApplication.class);

    private final Config config;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private HttpService httpService;

    public Gb32960ParserApplication() {
        this.config = ConfigFactory.load();
    }

    public void start() throws InterruptedException {
        String host = config.getString("gb32960.server.host");
        int port = config.getInt("gb32960.server.port");
        int bossThreadCount = config.getInt("gb32960.server.boss-thread-count");
        int workerThreadCount = config.getInt("gb32960.server.worker-thread-count");

        bossGroup = new NioEventLoopGroup(bossThreadCount);
        workerGroup = new NioEventLoopGroup(workerThreadCount);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new Gb32960ChannelInitializer(config))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.info("GB32960协议解析器启动成功，监听地址: {}:{}", host, port);

            // 启动HTTP服务
            httpService = new HttpService(config);
            int httpPort = config.hasPath("gb32960.http.port") ? config.getInt("gb32960.http.port") : 8080;
            httpService.start(httpPort);
            logger.info("HTTP服务启动成功，监听地址: {}:{}", host, httpPort);

            // 等待服务器socket关闭
            future.channel().closeFuture().sync();
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        logger.info("正在关闭GB32960协议解析器...");
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("GB32960协议解析器已关闭");
        httpService.stop();
        logger.info("httpService已关闭");
    }

    public static void main(String[] args) {
        Gb32960ParserApplication application = new Gb32960ParserApplication();

        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(application::shutdown));

        try {
            application.start();
        } catch (InterruptedException e) {
            logger.error("服务器启动被中断", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("服务器启动失败", e);
        }
    }
}
