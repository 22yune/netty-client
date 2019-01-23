package com.galen.program.netty.jero.rpc;

import com.galen.program.netty.jero.Service;

/**
 * Created by baogen.zhang on 2018/10/19
 *
 * @author baogen.zhang
 * @date 2018/10/19
 */
public class AbstractRPCService implements Service {
    private TypeService<?, ?> services;

    public void open() {

    }

    @Override
    public byte[] service(byte[] p) {
        return new byte[0];
    }


    public void close() {

    }
}
