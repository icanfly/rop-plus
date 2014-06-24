package rop;

/**
 *    抽象拦截器，实现类仅需覆盖特定的方法即可。
 */

public abstract class AbstractInterceptor implements Interceptor {

    public void beforeService(RopRequestContext ropRequestContext) {
    }


    public void beforeResponse(RopRequestContext ropRequestContext) {
    }

    @Override
    public boolean isMatch(RopRequestContext ropRequestContext) {
        return true;
    }

    /**
     * 放在拦截器链的最后
     *
     * @return
     */
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}

