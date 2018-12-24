package com.galen.program.netty.jero;

import java.util.concurrent.Future;

/**
 * Created by baogen.zhang on 2018/10/19
 *
 * @author baogen.zhang
 * @date 2018/10/19
 */
public interface Client {
    void open();
    byte[] request(byte[] p);
    void close();
}
