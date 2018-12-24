package com.galen.program.netty.client.sync;

import com.galen.program.netty.client.SimpleReusePromise;
import com.galen.program.netty.client.TcpClientConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 同步 请求-回复 通信客户端
 * @param <V>
 */
public class DefaultSyncTcpClient<V> implements SyncTcpClient<V> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultSyncTcpClient.class.getSimpleName());

    private EventLoopGroup workerGroup ;

    private SimpleReusePromise<V> promise;
    private Channel channel;

    private static final int CREATE = 0;
    private static final int OPENED = 1;
    private static final int CLOSED = -1;
    private volatile int state ;

    public boolean isOpen(){
        return state == OPENED;
    }

    public boolean isActive(){
        return isOpen() && channel != null && channel.isActive();
    }

    public DefaultSyncTcpClient(EventLoopGroup workerGroup){
        state = CREATE;
        this.workerGroup = workerGroup;
    }
    public DefaultSyncTcpClient(EventLoopGroup workerGroup,TcpClientConfig config){
        this(workerGroup);
        open(config);
    }

    @Override
    public synchronized void open(TcpClientConfig config) throws IllegalStateException{
        if(state != CREATE){
            throw new IllegalStateException("Channel has opened or closed!");
        }
        createChannel(config);
        if(channel != null){
            state = OPENED;
        }
    }

    @Override
    public V send(Object msg) throws Exception{
        return send(msg,-1);
    }

    @Override
    public synchronized V send(Object msg,long time) throws Exception{
        if(state != OPENED){
            throw new IllegalStateException("Channel have not opened!");
        }
        try {
            Promise newPromise  = new DefaultPromise<V>(channel.eventLoop());

            if(!promise.repeat(newPromise)){
                throw new RuntimeException("[code] promise repeat error.");
            }
            ChannelFuture f = channel.writeAndFlush(msg);
            f.addListener(new GenericFutureListener<Future<? super Void>>() {
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(future.isSuccess()){
                        logger.info(channel.toString() + " : send success");
                    }else {
                        logger.info(channel.toString() + " : send error " + future.cause().toString());
                        promise.tryFailure(future.cause());
                    }
                }
            });
            if(time == -1) {
                return promise.get();
            } else {
                return promise.get(time,TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            throw e;
        }
    }
    @Override
    public synchronized void close() throws IllegalStateException{
        if(state != OPENED){
            throw new IllegalStateException("Channel have not opened!");
        }
        state = CLOSED;
        channel.close();
    }

    private synchronized void createChannel(final TcpClientConfig tcpClientConfig){
        try {
            final List<TcpClientConfig.ChannelHandlerFactory> handlerFactories = tcpClientConfig.getHandlerFactories();

            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);

            Map<ChannelOption<?>, Object> options = tcpClientConfig.options();
            synchronized (options) {
                for (Map.Entry<ChannelOption<?>, Object> e: options.entrySet()) {
                    b.option((ChannelOption<Object>) e.getKey(), e.getValue());
                }
            }

            final Map<AttributeKey<?>, Object> attrs = tcpClientConfig.attrs();
            synchronized (attrs) {
                for (Map.Entry<AttributeKey<?>, Object> e: attrs.entrySet()) {
                    b.attr((AttributeKey<Object>) e.getKey(),e.getValue());
                }
            }

            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(getSyncHandle());
                    if(handlerFactories != null){
                        for(TcpClientConfig.ChannelHandlerFactory factory : handlerFactories){
                            ch.pipeline().addLast(factory.newChannelHandler());
                        }
                    }
                }
            });
            // 启动客户端
            ChannelFuture f = b.connect(tcpClientConfig.getIp(), tcpClientConfig.getPort()).sync();

            f.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(isOpen()){
                        createChannel(tcpClientConfig);
                    }
                }
            });

            channel =  f.channel();
        } catch (InterruptedException e) {
            logger.error("",e);
        } finally {

        }
    }

    private ChannelHandler getSyncHandle(){
        promise = new SimpleReusePromise<V>();
        return new ChannelHandlerAdapter(){
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                if(evt instanceof DefaultSyncTcpClient.CompleteEvent){
                    if(((CompleteEvent) evt).isSuccess()) {
                        promise.trySuccess((V) ((CompleteEvent) evt).getMessage());
                    }
                    else {
                        promise.tryFailure(((CompleteEvent) evt).getCause());
                    }
                }
                ctx.fireUserEventTriggered(evt);
            }
        };
    }

    /**
     * 使用收到的回复消息或发生的异常等触发请求已完成事件的ChannelHandler。
     */
    @ChannelHandler.Sharable
    public static class SyncClientCompleteHandler<T> extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if(logger.isDebugEnabled()){
                logger.debug("收到消息" + msg.toString());
            }
            ctx.pipeline().fireUserEventTriggered(new CompleteEvent((T) msg));
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("同步请求发生异常！" ,cause);
            ctx.pipeline().fireUserEventTriggered(new CompleteEvent(cause));
            ctx.close();
        }
    }

    private static class CompleteEvent<T> {
        private Throwable cause;
        private T message;
        private boolean success;

        public boolean isSuccess() {
            return success;
        }

        public T getMessage() {
            return message;
        }

        public CompleteEvent(T message) {
            success = true;
            this.message = message;
        }

        public Throwable getCause() {
            return cause;
        }

        public CompleteEvent(Throwable cause) {
            success = false;
            this.cause = cause;
        }
    }
}
