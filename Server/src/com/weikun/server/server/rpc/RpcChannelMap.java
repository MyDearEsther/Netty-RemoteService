package com.weikun.server.server.rpc;

import com.weikun.server.common.LogUtil;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linweikun
 *  Channel记录
 * */
public class RpcChannelMap {
    private Map<String, Channel> map =new HashMap<>();

    public void add(String account, Channel channel) {
        map.put(account, channel);
        LogUtil.i("client connected: "+channel.remoteAddress());
    }

    public Channel get(String clientId) {
        return map.get(clientId);
    }

    public void remove(Channel channel) {
        for (Map.Entry entry : map.entrySet()) {
            if (entry.getValue() == channel) {
                String account = (String) entry.getKey();
                map.remove(account);
                LogUtil.i("client disconnected "+channel.remoteAddress());
                break;
            }
        }
    }

    public int size(){
        return map.size();
    }
}