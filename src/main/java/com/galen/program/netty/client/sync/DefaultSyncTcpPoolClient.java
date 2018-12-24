package com.galen.program.netty.client.sync;

import com.galen.program.netty.client.TcpClientConfig;
import io.netty.channel.*;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 同步 请求-回复 通信客户端
 * @param <V>
 */
public class DefaultSyncTcpPoolClient<V>  {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSyncTcpPoolClient.class.getSimpleName());

    private TcpClientConfig tcpClientConfig;
    private SyncTcpClientPool<V> tcpClientPool;

    public DefaultSyncTcpPoolClient(TcpClientConfig config){
        this(new GenericKeyedObjectPoolConfig(),config);
    }
    public DefaultSyncTcpPoolClient(GenericKeyedObjectPoolConfig poolConfig,TcpClientConfig config){
        poolConfig.setTestOnBorrow(true);
        tcpClientPool = new SyncTcpClientPool<V>(poolConfig);
        tcpClientConfig = config;
    }


    public V send(Object msg) throws Exception{
        return send(msg,-1);
    }

    public V send(Object msg,long time) throws Exception{
        DefaultSyncTcpClient<V> client = tcpClientPool.borrowObject(tcpClientConfig);
        V result = client.send(msg, time);
        tcpClientPool.returnObject(tcpClientConfig,client);
        return result;
    }

    public void close() {
        tcpClientPool.close();
    }

}
