package com.galen.program.netty.jero.rpc;

/**
 * Created by baogen.zhang on 2018/10/19
 *
 * @author baogen.zhang
 * @date 2018/10/19
 */
public interface TypeService<P, R> {
    R service(P p);

    byte[] encode(R r);

    P decode(byte[] p);

    int funCode();
}
