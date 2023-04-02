package com.example;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;


public class MessageCodec extends ChannelInitializer<SocketChannel> {

    private static final int MAX_FRAME_LENGTH = 65535;
    private static final int LENGTH_FIELD_OFFSET = 4;
    private static final int LENGTH_FIELD_LENGTH = 4;

    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final int WRITE_TIMEOUT_SECONDS = 10;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH));
        pipeline.addLast(new LengthFieldPrepender(LENGTH_FIELD_LENGTH));
        pipeline.addLast(new AuthHandler());
        pipeline.addLast(new ReadTimeoutHandler(READ_TIMEOUT_SECONDS));
        pipeline.addLast(new WriteTimeoutHandler(WRITE_TIMEOUT_SECONDS));
        pipeline.addLast(new XmlMessageDecoder());
        pipeline.addLast(new XmlMessageEncoder());
        pipeline.addLast(new HeartbeatHandler());
        pipeline.addLast(new ReconnectHandler());
        pipeline.addLast(new AuthSuccessHandler());
        pipeline.addLast(new ResendHandler());
    }

    /**
     * xml编码
     */
    class XmlMessageDecoder extends ByteToMessageDecoder {
        private final Charset CHARSET = Charset.forName("UTF-8");

        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
            int frameLength = byteBuf.readableBytes();
            byte[] bytes = new byte[frameLength];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//            Message message = JAXB.unmarshal(is, Message.class);
//            list.add(message);
        }
    }

    /**
     * xml解码
     */
    class XmlMessageEncoder extends MessageToByteEncoder {

        protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JAXB.marshal(o, os);
            byte[] bytes = os.toByteArray();
            byteBuf.writeBytes(bytes);
        }
    }

    /**
     * 登录
     */
    class AuthHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            authenticate(ctx);
        }

        private void authenticate(ChannelHandlerContext ctx) {
//            Message auth = new Message();
//            auth.setHeader(new Header(0, MessageType.AUTH, 0));
//            AuthBody authBody = new AuthBody(AUTH_USERNAME, AUTH_PASSWORD);
//            auth.setBody(authBody);
//            ctx.writeAndFlush(auth);
        }
    }

    /**
     *
     */
    class AuthSuccessHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            if (msg instanceof Message) {
//                Message message = (Message) msg;
//                if (message.getHeader().getType() == MessageType.AUTH_SUCCESS) {
//                    // 认证成功，继续进行心跳检测
//                    super.channelRead(ctx, msg);
//                    return;
//                }
//            }
            ctx.close(); // 关闭连接，认证失败
        }
    }

    /**
     * 心跳检测
     */
    class HeartbeatHandler extends ChannelInboundHandlerAdapter {
        private static final int HEARTBEAT_INTERVAL = 30; // 心跳间隔（秒）

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                sendHeartbeat(ctx);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }

        private void sendHeartbeat(ChannelHandlerContext ctx) {
//        Message heartbeat = new Message();
//        heartbeat.setHeader(new Header(0, MessageType.HEARTBEAT, 0));
            ctx.writeAndFlush(null);
        }
    }

    /**
     * 重连
     */
    class ReconnectHandler extends ChannelInboundHandlerAdapter {

        private static final int MAX_RETRY_TIMES = 3; // 最大重试次数
        private int retryTimes = 0;

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (retryTimes < MAX_RETRY_TIMES) {
                retryTimes++;
                System.out.println("Connection lost, reconnecting...");
                Thread.sleep(1000 * retryTimes); // 按指数级别等待
//                ChannelFuture future = NettyClient.connect(); // 重新连接
//                future.addListener((ChannelFutureListener) f -> {
//                    if (f.isSuccess()) {
//                        System.out.println("Reconnect success");
//                        f.channel().pipeline().fireUserEventTriggered(new AuthEvent()); // 发送认证消息
//                    } else {
//                        System.out.println("Reconnect failed");
//                        f.channel().pipeline().fireChannelInactive(); // 触发断线事件
//                    }
//                });
            } else {
                System.out.println("Connection lost, retry times exceeded");
                super.channelInactive(ctx);
            }
        }


        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//            if (evt instanceof AuthEvent) {
//                authenticate(ctx);
//            } else {
//                super.userEventTriggered(ctx, evt);
//            }
        }

        private void authenticate(ChannelHandlerContext ctx) {
//            Message auth = new Message();
//            auth.setHeader(new Header(0, MessageType.AUTH, 0));
//            AuthBody authBody = new AuthBody(AUTH_USERNAME, AUTH_PASSWORD);
//            auth.setBody(authBody);
//            ctx.writeAndFlush(auth);
        }
    }


    /**
     * 消息重发
     */
    class ResendHandler extends ChannelInboundHandlerAdapter {

    }

    /**
     * 异常处理
     */
    class ExceptionHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.err.println("Exception caught: " + cause);
            cause.printStackTrace(System.err);
            ctx.close();
        }
    }
}

