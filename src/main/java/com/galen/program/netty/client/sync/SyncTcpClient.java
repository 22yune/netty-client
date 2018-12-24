package com.galen.program.netty.client.sync;


import com.galen.program.netty.client.TcpClientConfig;
import io.netty.channel.ChannelHandler;

/**
 * 同步 请求-回复 通信客户端
 * @param <V>
 */
public interface SyncTcpClient<V> {

    void open(TcpClientConfig config) throws IllegalStateException;

    void close() throws IllegalStateException;

    /**
     * @see #send(Object, long)
     * @param msg
     * @return
     * @throws Exception
     */
    V send(Object msg) throws Exception;

    /**
     * 发送请求消息，并等待回复消息收到后返回。或者超时抛出异常返回。
     * TcpClientConfig中要添加completeHandler，这里才能识别出回复消息received msg。
     * @param msg 请求消息
     * @param time 超时时间，时间单位为毫秒
     * @return
     * @throws Exception
     */
    V send(Object msg, long time) throws Exception; //client

    /**
     * 完成时（如接受到消息时）通知的ChannelHandler。

    ChannelHandler completeHandler();*/
}
