package com.galen.program.netty.client;

import com.galen.program.netty.client.sync.DefaultSyncTcpClient;
import com.galen.program.netty.client.sync.DefaultSyncTcpPoolClient;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SyncPoolClient {

    private static DefaultSyncTcpPoolClient<String> poolClient;
    @BeforeClass
    public static void before(){
        DefaultTcpClientConfig channelConfig = new DefaultTcpClientConfig();
        channelConfig.setIp("localhost");
        channelConfig.setPort(8066);

        //1、添加TCP粘包处理器
       // channelConfig.addHandler(new LengthFieldBasedFrameDecoder());
        //2、添加编解码处理器
        channelConfig.addHandler(StringEncoder.class);
        channelConfig.addHandler(StringDecoder.class);
        //2.B、 添加可能的心跳处理器


        //3、添加固定的完成事件处理器
        channelConfig.addHandler(new DefaultSyncTcpClient.SyncClientCompleteHandler<String>());
        //4、打开通道，接下来就可以发送消息了，使用完成后要调用close关闭。


        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();

        poolClient = new DefaultSyncTcpPoolClient<String>(poolConfig,channelConfig);
    }
    @Test
    public void send1(){
        String send = "send1";
        String a = null;
        try {
            a = poolClient.send(send);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(send.equals(a));
    }
    @Test
    public void send2(){
        String send = "send2";
        String a = null;
        try {
            a = poolClient.send(send);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(send.equals(a));
    }
    @Test
    public void send3(){
        for(int i = 0; i < 100; i++){
            final String send = "send3"+i;
            ExecutorService  executorService = Executors.newFixedThreadPool(7);
            executorService.submit(new Runnable() {
                public void run() {
                    String a = null;
                    try {
                        System.out.println("====" + send + "=====");
                        try {
                            Thread.sleep(new Random().nextInt(10000));
                        } catch (InterruptedException e) {
                            e.printStackTrace(System.out);
                        }
                        a = poolClient.send(send);
                        System.out.println("====" + send + "====="+ a + "======");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Assert.assertTrue(send.equals(a));
                }
            });
            executorService.shutdown();
            try {
                executorService.awaitTermination(30,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @AfterClass
    public static void after(){
        poolClient.close();
    }


}
