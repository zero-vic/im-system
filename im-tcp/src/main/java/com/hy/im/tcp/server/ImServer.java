package com.hy.im.tcp.server;

import com.hy.im.codec.config.BootstrapConfig;
import com.hy.im.codec.decoder.MessageDecoder;
import com.hy.im.codec.encoder.MessageEncoder;
import com.hy.im.tcp.handler.HeartBeatHandler;
import com.hy.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName ImServer
 * description: im tcp server
 * yao create 2023年06月28日
 * version: 1.0
 */
public class ImServer {
    private final static Logger log = LoggerFactory.getLogger(ImServer.class);

    BootstrapConfig.TcpConfig config;
    NioEventLoopGroup boss;
    NioEventLoopGroup worker;
    ServerBootstrap serverBootstrap;

    public ImServer(BootstrapConfig.TcpConfig config){
        this.config = config;
        boss = new NioEventLoopGroup(config.getBossThreadSize());
        worker = new NioEventLoopGroup(config.getWorkThreadSize());
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                // 服务端可连接队列大小
                .option(ChannelOption.SO_BACKLOG,10240)
                // 允许重复使用本地地址和端口
                .option(ChannelOption.SO_REUSEADDR,true)
                // 是否禁用nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.TCP_NODELAY,true)
                // 保活开关2h没有数据服务端会发送心跳包
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new MessageDecoder());
                        pipeline.addLast(new MessageEncoder());
//                        pipeline.addLast(new IdleStateHandler(0,0,1));
                        pipeline.addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        pipeline.addLast(new NettyServerHandler(config.getBrokerId()));
                    }
                });
    }
    public void start(){
        this.serverBootstrap.bind(config.getTcpPort());
        log.info("im tcp server starter");
    }

}
