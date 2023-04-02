package com.example;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

public class MessageHandle extends ChannelInboundHandlerAdapter {

    private List<String> receivedMessages;
    private final ReentrantLock lock = new ReentrantLock();


    public MessageHandle() {
        this.receivedMessages = new ArrayList<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        lock.lock();
        try {
            String receivedMessage = (String) msg;
            receivedMessages.add(receivedMessage);
        }finally {
            lock.unlock();
        }
    }
}
