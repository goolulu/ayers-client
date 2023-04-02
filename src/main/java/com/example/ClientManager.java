package com.example;

import com.example.pojo.AyersSetting;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientManager {

    private final ConcurrentMap<AyersSetting, NettyClient> connections = new ConcurrentHashMap<>();

    public NettyClient createClient(AyersSetting setting) {

        NettyClient client = connections.get(setting);
        if (client == null) {
            client = new NettyClient(setting);
        }
        return client;
    }
}
