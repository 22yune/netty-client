package com.galen.program.netty.jero.rpc;

/**
 * Created by baogen.zhang on 2018/10/19
 *
 * @author baogen.zhang
 * @date 2018/10/19
 */
public interface TypeClient<P, R> {

    /**
     * 服务号 对应类，功能号对应到方法  客户端应该可以由服务端生成
     **/

    R request(P p, int funCode);

    int funCode();

    byte[] encode(P p);

    R decode(byte[] r);
}
