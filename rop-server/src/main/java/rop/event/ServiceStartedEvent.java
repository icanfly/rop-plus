package rop.event;

import rop.RopRequestContext;

/**
 * <pre>
 *     服务开始事件
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class ServiceStartedEvent extends RopEvent {

    private RopRequestContext ropRequestContext;

    public ServiceStartedEvent(Object source, RopRequestContext ropRequestContext) {
        super(source, ropRequestContext.getRopContext());
        this.ropRequestContext = ropRequestContext;
    }

    public RopRequestContext getRopRequestContext() {
        return ropRequestContext;
    }

    public long getServiceBeginTime() {
        return ropRequestContext.getServiceBeginTime();
    }
}

