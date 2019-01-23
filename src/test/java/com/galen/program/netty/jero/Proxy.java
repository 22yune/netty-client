package com.galen.program.netty.jero;

/**
 * 服务代理，请求路由端点。本身也是服务代理管理服务。
 * <p>
 * Created by baogen.zhang on 2018/10/19
 *
 * @author baogen.zhang
 * @date 2018/10/19
 */
public interface Proxy {
    /**
     * 运行服务代理服务
     * 实现类应提供服务代理管理服务。
     *
     * @param requestAddress
     */
    void run(String requestAddress);
}
