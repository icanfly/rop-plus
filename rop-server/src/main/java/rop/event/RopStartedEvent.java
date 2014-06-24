package rop.event;

import rop.RopContext;

/**
 * <pre>
 *   在Rop框架初始化后产生的事件
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class RopStartedEvent extends RopEvent {

    public RopStartedEvent(Object source, RopContext ropContext) {
        super(source, ropContext);
    }

}

