package com.example;

import com.example.pojo.AyersSetting;
import com.example.pojo.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class NettyClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private AyersSetting setting;
    private MessageHandle messageHandle;
    private Channel channel;


    public NettyClient(AyersSetting setting) {
        this.setting = setting;
        messageHandle = new MessageHandle();
        open();
    }


    public ChannelFuture connect() {
        EventLoopGroup group = new NioEventLoopGroup(4);
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new MessageCodec())
                .handler(messageHandle);
        return bootstrap.connect(HOST, PORT);
    }

    public void open() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ChannelFuture future = connect();
            channel = future.channel();
            future.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    System.out.println("Connection established");
                    f.channel().pipeline().fireUserEventTriggered(new Object()); // 发送认证消息
                } else {
                    System.err.println("Connection failed: " + f.cause());
                }
            });
            future.channel().closeFuture().sync(); // 阻塞等待连接关闭
        } catch (Exception e) {
            System.err.println("Exception caught: " + e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendMsg(Object obj) {
        channel.write(obj);
    }
}
