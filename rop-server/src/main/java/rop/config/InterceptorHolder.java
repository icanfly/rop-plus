package rop.config;

import rop.Interceptor;

/**
 * <pre>
 *     服务拦截器
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class InterceptorHolder {

    private Interceptor interceptor;

    public InterceptorHolder(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }
}

