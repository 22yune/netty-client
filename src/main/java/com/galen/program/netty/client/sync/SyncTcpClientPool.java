package com.galen.program.netty.client.sync;

import com.galen.program.netty.client.TcpClientConfig;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.util.logging.Logger;

/**
 * @see NioSocketChannel 对象池
 */
public class SyncTcpClientPool<V> extends GenericKeyedObjectPool<TcpClientConfig, DefaultSyncTcpClient<V>> {
    private static Logger logger = Logger.getLogger(SyncTcpClientPool.class.getSimpleName());
    private static final int DEFAULT_EVENT_LOOP_THREADS;

    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
    }

    public SyncTcpClientPool() {
        super(new ChannelFactory<V>(new NioEventLoopGroup()));
    }

    public SyncTcpClientPool(GenericKeyedObjectPoolConfig config) {
        super(new ChannelFactory<V>(new NioEventLoopGroup(Math.max(config.getMaxTotal(), DEFAULT_EVENT_LOOP_THREADS))), config);
    }

    @Override
    public void close() {
        super.close();
        ((ChannelFactory) this.getFactory()).getWorkerGroup().shutdownGracefully();
    }


    private static class ChannelFactory<V> extends BaseKeyedPooledObjectFactory<TcpClientConfig, DefaultSyncTcpClient<V>> {
        private EventLoopGroup workerGroup;

        public EventLoopGroup getWorkerGroup() {
            return workerGroup;
        }

        public ChannelFactory(EventLoopGroup workerGroup) {
            this.workerGroup = workerGroup;
        }

        @Override
        public DefaultSyncTcpClient<V> create(TcpClientConfig key) throws Exception {
            return new DefaultSyncTcpClient<V>(workerGroup, key);
        }

        @Override
        public PooledObject<DefaultSyncTcpClient<V>> wrap(DefaultSyncTcpClient<V> value) {
            return new DefaultPooledObject<DefaultSyncTcpClient<V>>(value);
        }

        @Override
        public void destroyObject(TcpClientConfig key, PooledObject<DefaultSyncTcpClient<V>> p) throws Exception {
            DefaultSyncTcpClient<V> channel = p.getObject();
            try {
                channel.close();
            } catch (IllegalStateException e) {

            }
        }

        @Override
        public boolean validateObject(TcpClientConfig key, PooledObject<DefaultSyncTcpClient<V>> p) {
            DefaultSyncTcpClient<V> channel = p.getObject();
            if (channel.isActive()) {
                return true;
            }
            return false;
        }
    }
}
