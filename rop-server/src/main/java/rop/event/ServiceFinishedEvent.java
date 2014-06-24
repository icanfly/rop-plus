package rop.event;

import rop.RopRequestContext;

/**
 * <pre>
 *     服务执行完成事件
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class ServiceFinishedEvent extends RopEvent {

    private RopRequestContext ropRequestContext;

    public ServiceFinishedEvent(Object source, RopRequestContext ropRequestContext) {
        super(source, ropRequestContext.getRopContext());
        this.ropRequestContext = ropRequestContext;
    }

    public long getServiceBeginTime() {
        return ropRequestContext.getServiceBeginTime();
    }

    public long getServiceEndTime() {
        return ropRequestContext.getServiceEndTime();
    }

    public RopRequestContext getRopRequestContext() {
        return ropRequestContext;
    }
}

