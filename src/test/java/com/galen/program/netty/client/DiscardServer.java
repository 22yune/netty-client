package com.galen.program.netty.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Random;

/**
 * 丢弃任何进入的数据
 */
public class DiscardServer {
    private int port;
    public DiscardServer(int port) {
        this.port = port;
    }
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(8); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup(9);
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128) // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
// 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)
// 等待服务器 socket 关闭 。
// 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
    /**
     * 处理服务端 channel.
     */
    public class DiscardServerHandler extends ChannelHandlerAdapter { // (1)
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) { // (2)
            ByteBuf in = (ByteBuf) msg;
            final String a = in.toString(io.netty.util.CharsetUtil.US_ASCII);
            final Channel channel = ctx.channel();
            channel.eventLoop().submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(a);
                    try {
                        Thread.sleep(new Random().nextInt(10000));
                    } catch (InterruptedException e) {
                        e.printStackTrace(System.out);
                    }
                    ChannelFuture f = channel.writeAndFlush(msg);
                    f.addListener(new GenericFutureListener<Future<? super Void>>() {
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            if(future.isSuccess()){
                                System.out.println("response success");
                            }
                        }
                    });
                }
            });

            // 默默地丢弃收到的数据
           // ((ByteBuf) msg).release(); // (3)
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
            // 当出现异常就关闭连接
            cause.printStackTrace(System.out);
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8066;
        }
        new DiscardServer(port).run();
    }
}