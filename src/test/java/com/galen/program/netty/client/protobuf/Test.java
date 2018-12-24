package com.galen.program.netty.client.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

public class Test {
    public static byte[] encode(Resq.SearchRequest resq){
        return resq.toByteArray();
    }
    public static Resq.SearchRequest decode(byte[] bytes) throws InvalidProtocolBufferException {
        return Resq.SearchRequest.parseFrom(bytes);
    }
    public static Resq.SearchRequest build(){
        Resq.SearchRequest.Builder builder = Resq.SearchRequest.newBuilder();
        //TODO 设置值
        builder.setReq(builder.build());
        return builder.build();
    }
}
